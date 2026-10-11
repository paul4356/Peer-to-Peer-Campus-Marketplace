<?php
declare(strict_types=1);

namespace App\Controllers;

use App\Config\Database;
use App\Middleware\AuthMiddleware;
use App\Utils\Response;
use App\Utils\Validator;

class ProfileController
{
    public function update(array $data): void
    {
        $user = AuthMiddleware::authenticate();
        $pdo = Database::getConnection();

        $fullName = isset($data['full_name']) ? trim((string)$data['full_name']) : $user['full_name'];
        $phone = isset($data['phone']) ? trim((string)$data['phone']) : $user['phone'];
        $department = isset($data['department']) ? trim((string)$data['department']) : $user['department'];
        $yearOfStudy = isset($data['year_of_study']) ? (int)$data['year_of_study'] : $user['year_of_study'];

        $fields = [
            'full_name' => $fullName,
            'phone' => $phone,
            'department' => $department,
            'year_of_study' => $yearOfStudy,
            'id' => $user['id']
        ];

        $sql = 'UPDATE users SET full_name = :full_name, phone = :phone, department = :department, year_of_study = :year_of_study';

        // Password change if provided
        if (!empty($data['password'])) {
            if (mb_strlen((string)$data['password']) < 6) {
                Response::error('New password must be at least 6 characters.', 422);
            }
            $sql .= ', password_hash = :password_hash';
            $fields['password_hash'] = password_hash((string)$data['password'], PASSWORD_BCRYPT);
        }

        $sql .= ' WHERE id = :id';

        $stmt = $pdo->prepare($sql);
        $stmt->execute($fields);

        // Fetch refreshed user
        $refreshStmt = $pdo->prepare('SELECT id, student_id, full_name, email, phone, department, year_of_study, role, verification_status FROM users WHERE id = :id');
        $refreshStmt->execute(['id' => $user['id']]);
        $updatedUser = $refreshStmt->fetch();

        Response::json(['user' => $updatedUser], 200, 'Profile updated successfully.');
    }
}
