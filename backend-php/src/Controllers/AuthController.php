<?php
declare(strict_types=1);

namespace App\Controllers;

use App\Config\Database;
use App\Middleware\AuthMiddleware;
use App\Utils\Response;
use App\Utils\Validator;
use PDO;

class AuthController
{
    public function register(array $data): void
    {
        $validator = new Validator();
        $validator->requireFields($data, ['student_id', 'full_name', 'email', 'password'])
                  ->validateEmail($data['email'] ?? '')
                  ->validateMinLength($data['password'] ?? '', 6, 'password')
                  ->validateMinLength($data['student_id'] ?? '', 3, 'student_id')
                  ->validateMinLength($data['full_name'] ?? '', 2, 'full_name');

        if ($validator->hasErrors()) {
            Response::error($validator->getFirstError(), 422, $validator->getErrors());
        }

        $email = trim(strtolower((string)$data['email']));
        $studentId = trim((string)$data['student_id']);
        $fullName = trim((string)$data['full_name']);
        $password = (string)$data['password'];
        $phone = trim((string)($data['phone'] ?? ''));
        $department = trim((string)($data['department'] ?? 'General Engineering'));
        $yearOfStudy = isset($data['year_of_study']) ? (int)$data['year_of_study'] : 1;

        $pdo = Database::getConnection();

        // Check duplicate email
        $stmt = $pdo->prepare('SELECT id FROM users WHERE email = :email LIMIT 1');
        $stmt->execute(['email' => $email]);
        if ($stmt->fetch()) {
            Response::error('An account with this email address already exists.', 409);
        }

        // Check duplicate student_id
        $stmt = $pdo->prepare('SELECT id FROM users WHERE student_id = :student_id LIMIT 1');
        $stmt->execute(['student_id' => $studentId]);
        if ($stmt->fetch()) {
            Response::error('This Student ID / Roll Number is already registered.', 409);
        }

        // Generate secure password hash and session API token
        $passwordHash = password_hash($password, PASSWORD_BCRYPT);
        $token = bin2hex(random_bytes(32));

        $stmt = $pdo->prepare('
            INSERT INTO users 
            (student_id, full_name, email, password_hash, phone, department, year_of_study, role, verification_status, api_token)
            VALUES 
            (:student_id, :full_name, :email, :password_hash, :phone, :department, :year_of_study, "student", "pending", :token)
        ');

        $stmt->execute([
            'student_id' => $studentId,
            'full_name' => $fullName,
            'email' => $email,
            'password_hash' => $passwordHash,
            'phone' => $phone,
            'department' => $department,
            'year_of_study' => $yearOfStudy,
            'token' => $token
        ]);

        $userId = (int)$pdo->lastInsertId();

        Response::json([
            'user' => [
                'id' => $userId,
                'student_id' => $studentId,
                'full_name' => $fullName,
                'email' => $email,
                'phone' => $phone,
                'department' => $department,
                'year_of_study' => $yearOfStudy,
                'role' => 'student',
                'verification_status' => 'pending'
            ],
            'token' => $token,
            'message' => 'Registration successful! Your account is pending administrator verification.'
        ], 201, 'Student account registered successfully.');
    }

    public function login(array $data): void
    {
        $validator = new Validator();
        $validator->requireFields($data, ['email', 'password'])
                  ->validateEmail($data['email'] ?? '');

        if ($validator->hasErrors()) {
            Response::error($validator->getFirstError(), 422, $validator->getErrors());
        }

        $email = trim(strtolower((string)$data['email']));
        $password = (string)$data['password'];

        $pdo = Database::getConnection();
        $stmt = $pdo->prepare('
            SELECT id, student_id, full_name, email, password_hash, phone, department, 
                   year_of_study, role, verification_status
            FROM users 
            WHERE email = :email 
            LIMIT 1
        ');
        $stmt->execute(['email' => $email]);
        $user = $stmt->fetch();

        if (!$user || !password_verify($password, $user['password_hash'])) {
            Response::error('Invalid email or password. Please try again.', 401);
        }

        // Generate fresh session token
        $token = bin2hex(random_bytes(32));
        $updateStmt = $pdo->prepare('UPDATE users SET api_token = :token WHERE id = :id');
        $updateStmt->execute(['token' => $token, 'id' => $user['id']]);

        unset($user['password_hash']);

        Response::json([
            'user' => $user,
            'token' => $token,
            'message' => 'Login successful!'
        ], 200, 'User logged in successfully.');
    }

    public function logout(): void
    {
        $user = AuthMiddleware::authenticate();
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare('UPDATE users SET api_token = NULL WHERE id = :id');
        $stmt->execute(['id' => $user['id']]);

        Response::json(null, 200, 'Logged out successfully.');
    }

    public function me(): void
    {
        $user = AuthMiddleware::authenticate();
        Response::json(['user' => $user], 200, 'Current user profile');
    }
}
