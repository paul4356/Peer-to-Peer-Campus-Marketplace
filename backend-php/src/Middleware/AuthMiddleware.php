<?php
declare(strict_types=1);

namespace App\Middleware;

use App\Config\Database;
use App\Utils\Response;
use PDO;

class AuthMiddleware
{
    public static function authenticate(): array
    {
        $token = self::extractToken();

        if (!$token) {
            Response::error('Authentication token is missing. Please log in.', 401);
        }

        $pdo = Database::getConnection();
        $stmt = $pdo->prepare('
            SELECT id, student_id, full_name, email, phone, department, year_of_study, 
                   role, verification_status, created_at
            FROM users 
            WHERE api_token = :token
            LIMIT 1
        ');
        $stmt->execute(['token' => $token]);
        $user = $stmt->fetch();

        if (!$user) {
            Response::error('Invalid or expired session token. Please log in again.', 401);
        }

        return $user;
    }

    public static function requireVerifiedStudent(): array
    {
        $user = self::authenticate();

        if ($user['role'] !== 'admin' && $user['verification_status'] !== 'approved') {
            Response::error(
                'Account verification required. Your student ID is currently in "' . 
                $user['verification_status'] . '" status. An administrator must approve your verification before you can post or modify listings.',
                403
            );
        }

        return $user;
    }

    public static function requireAdmin(): array
    {
        $user = self::authenticate();

        if ($user['role'] !== 'admin') {
            Response::error('Access denied. Administrator privileges are required.', 403);
        }

        return $user;
    }

    private static function extractToken(): ?string
    {
        $headers = getallheaders();
        $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

        if (preg_match('/Bearer\s(\S+)/', $authHeader, $matches)) {
            return $matches[1];
        }

        if (isset($headers['X-API-Token'])) {
            return $headers['X-API-Token'];
        }

        if (isset($_GET['token']) && is_string($_GET['token'])) {
            return $_GET['token'];
        }

        return null;
    }
}
