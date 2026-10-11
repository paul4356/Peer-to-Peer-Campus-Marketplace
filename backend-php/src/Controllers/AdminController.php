<?php
declare(strict_types=1);

namespace App\Controllers;

use App\Config\Database;
use App\Middleware\AuthMiddleware;
use App\Utils\Response;
use PDO;

class AdminController
{
    public function dashboard(): void
    {
        $admin = AuthMiddleware::requireAdmin();
        $pdo = Database::getConnection();

        // High level metrics
        $usersStats = $pdo->query('
            SELECT 
                COUNT(*) AS total_users,
                COUNT(CASE WHEN role = \'student\' AND verification_status = \'approved\' THEN 1 END) AS approved_students,
                COUNT(CASE WHEN role = \'student\' AND verification_status = \'pending\' THEN 1 END) AS pending_verifications,
                COUNT(CASE WHEN role = \'student\' AND verification_status = \'rejected\' THEN 1 END) AS rejected_verifications
            FROM users
        ')->fetch();

        $listingsStats = $pdo->query('
            SELECT 
                COUNT(*) AS total_listings,
                COUNT(CASE WHEN status = \'available\' THEN 1 END) AS available_listings,
                COUNT(CASE WHEN status = \'sold\' THEN 1 END) AS sold_listings,
                COALESCE(SUM(views_count), 0) AS total_views
            FROM listings
        ')->fetch();

        $reportsStats = $pdo->query('
            SELECT 
                COUNT(*) AS total_reports,
                COUNT(CASE WHEN status = \'pending\' THEN 1 END) AS pending_reports
            FROM reports
        ')->fetch();

        Response::json([
            'metrics' => [
                'total_users' => (int)$usersStats['total_users'],
                'approved_students' => (int)$usersStats['approved_students'],
                'pending_verifications' => (int)$usersStats['pending_verifications'],
                'rejected_verifications' => (int)$usersStats['rejected_verifications'],
                'total_listings' => (int)$listingsStats['total_listings'],
                'available_listings' => (int)$listingsStats['available_listings'],
                'sold_listings' => (int)$listingsStats['sold_listings'],
                'total_views' => (int)$listingsStats['total_views'],
                'total_reports' => (int)$reportsStats['total_reports'],
                'pending_reports' => (int)$reportsStats['pending_reports']
            ]
        ], 200, 'Admin dashboard metrics retrieved.');
    }

    public function verifications(): void
    {
        AuthMiddleware::requireAdmin();
        $pdo = Database::getConnection();

        $stmt = $pdo->query('
            SELECT id, student_id, full_name, email, phone, department, year_of_study, 
                   verification_status, created_at
            FROM users
            WHERE role = \'student\' AND verification_status = \'pending\'
            ORDER BY created_at ASC
        ');
        $rows = $stmt->fetchAll();

        Response::json(['verifications' => $rows], 200, 'Pending student verification list.');
    }

    public function updateVerification(int $userId, array $data): void
    {
        $admin = AuthMiddleware::requireAdmin();
        $status = trim((string)($data['status'] ?? ''));
        $reason = trim((string)($data['reason'] ?? ''));

        if (!in_array($status, ['approved', 'rejected'], true)) {
            Response::error('Status must be either "approved" or "rejected".', 422);
        }

        $pdo = Database::getConnection();

        // Check user exists
        $userStmt = $pdo->prepare("SELECT id, full_name, student_id, verification_status FROM users WHERE id = :id AND role = 'student' LIMIT 1");
        $userStmt->execute(['id' => $userId]);
        $targetUser = $userStmt->fetch();

        if (!$targetUser) {
            Response::error('Student user not found.', 404);
        }

        $stmt = $pdo->prepare('
            UPDATE users 
            SET verification_status = :status, 
                rejection_reason = :reason,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = :id
        ');
        $stmt->execute([
            'status' => $status,
            'reason' => $status === 'rejected' ? $reason : null,
            'id' => $userId
        ]);

        // Audit log entry
        $audit = $pdo->prepare('
            INSERT INTO admin_audit_logs (admin_id, action, target_type, target_id, details, ip_address)
            VALUES (:admin_id, :action, \'user\', :target_id, :details, :ip)
        ');
        $actionName = $status === 'approved' ? 'APPROVE_STUDENT' : 'REJECT_STUDENT';
        $details = "Admin updated status of {$targetUser['full_name']} ({$targetUser['student_id']}) to {$status}." . ($reason ? " Reason: {$reason}" : '');
        $ip = $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';

        $audit->execute([
            'admin_id' => $admin['id'],
            'action' => $actionName,
            'target_id' => $userId,
            'details' => $details,
            'ip' => $ip
        ]);

        Response::json([
            'user_id' => $userId,
            'verification_status' => $status,
            'message' => "Student verification updated to {$status}."
        ], 200, "Student {$status} successfully.");
    }

    public function users(): void
    {
        AuthMiddleware::requireAdmin();
        $pdo = Database::getConnection();

        $query = isset($_GET['q']) ? trim((string)$_GET['q']) : '';
        $role = isset($_GET['role']) ? trim((string)$_GET['role']) : '';
        $status = isset($_GET['status']) ? trim((string)$_GET['status']) : '';

        $where = [];
        $params = [];

        if ($query !== '') {
            $where[] = '(full_name LIKE :full_name_query OR email LIKE :email_query OR student_id LIKE :student_id_query)';
            $params['full_name_query'] = "%{$query}%";
            $params['email_query'] = "%{$query}%";
            $params['student_id_query'] = "%{$query}%";
        }

        if ($role !== '') {
            $where[] = 'role = :role';
            $params['role'] = $role;
        }

        if ($status !== '') {
            $where[] = 'verification_status = :status';
            $params['status'] = $status;
        }

        $whereClause = !empty($where) ? 'WHERE ' . implode(' AND ', $where) : '';

        $sql = "
            SELECT id, student_id, full_name, email, phone, department, year_of_study, 
                   role, verification_status, created_at
            FROM users
            {$whereClause}
            ORDER BY id ASC
        ";

        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $users = $stmt->fetchAll();

        Response::json(['users' => $users], 200, 'User list retrieved.');
    }

    public function reports(): void
    {
        AuthMiddleware::requireAdmin();
        $pdo = Database::getConnection();

        $sql = '
            SELECT 
                r.id, r.reason, r.details, r.status, r.created_at, r.resolved_at,
                l.id AS listing_id, l.title AS listing_title, l.price AS listing_price, l.status AS listing_status,
                u.id AS reporter_id, u.full_name AS reporter_name, u.email AS reporter_email,
                s.id AS seller_id, s.full_name AS seller_name, s.email AS seller_email
            FROM reports r
            JOIN listings l ON l.id = r.listing_id
            JOIN users u ON u.id = r.reporter_id
            JOIN users s ON s.id = l.seller_id
            ORDER BY r.created_at DESC
        ';

        $stmt = $pdo->query($sql);
        $reports = $stmt->fetchAll();

        Response::json(['reports' => $reports], 200, 'Listing reports retrieved.');
    }

    public function auditLogs(): void
    {
        AuthMiddleware::requireAdmin();
        $pdo = Database::getConnection();

        $sql = '
            SELECT 
                a.id, a.action, a.target_type, a.target_id, a.details, a.ip_address, a.created_at,
                u.full_name AS admin_name, u.email AS admin_email
            FROM admin_audit_logs a
            JOIN users u ON u.id = a.admin_id
            ORDER BY a.created_at DESC
            LIMIT 50
        ';

        $stmt = $pdo->query($sql);
        $logs = $stmt->fetchAll();

        Response::json(['audit_logs' => $logs], 200, 'Audit logs retrieved.');
    }
}
