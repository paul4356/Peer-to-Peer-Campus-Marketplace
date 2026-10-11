<?php
declare(strict_types=1);

namespace App\Config;

use PDO;
use PDOException;

class Database
{
    private static ?PDO $instance = null;

    public static function getConnection(): PDO
    {
        if (self::$instance === null) {
            if (!Config::hasExternalDatabaseConfig()) {
                self::ensureDatabaseExists();
            }

            $dsn = sprintf(
                'mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4',
                Config::dbHost(),
                Config::dbPort(),
                Config::dbName()
            );

            $options = [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false,
                PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
            ];

            try {
                self::$instance = new PDO($dsn, Config::dbUser(), Config::dbPass(), $options);
                self::ensureRequiredSchema(self::$instance);
            } catch (PDOException $e) {
                // Secure handling: Do not leak DB credentials or stack trace to client
                http_response_code(500);
                echo json_encode([
                    'success' => false,
                    'error' => 'Database connection failed. Please ensure MySQL server is active in XAMPP.',
                    'code' => 'DB_CONNECTION_ERROR'
                ], JSON_PRETTY_PRINT);
                exit;
            }
        }

        return self::$instance;
    }

    private static function ensureDatabaseExists(): void
    {
        $dsn = sprintf('mysql:host=%s;port=%d;charset=utf8mb4', Config::dbHost(), Config::dbPort());
        $options = [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false
        ];

        $pdo = new PDO($dsn, Config::dbUser(), Config::dbPass(), $options);
        $pdo->exec(sprintf(
            'CREATE DATABASE IF NOT EXISTS `%s` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci',
            Config::dbName()
        ));
    }

    private static function ensureRequiredSchema(PDO $pdo): void
    {
        $userTableExists = $pdo->query("SHOW TABLES LIKE 'users'")->fetch();
        if ($userTableExists === false) {
            self::applySqlFile($pdo, dirname(__DIR__, 2) . '/database/schema.sql');
            self::applySqlFile($pdo, dirname(__DIR__, 2) . '/database/seed.sql');
            return;
        }

        $categoryCount = (int) $pdo->query('SELECT COUNT(*) FROM categories')->fetchColumn();
        $userCount = (int) $pdo->query('SELECT COUNT(*) FROM users')->fetchColumn();
        if ($categoryCount === 0 || $userCount === 0) {
            self::applySqlFile($pdo, dirname(__DIR__, 2) . '/database/seed.sql');
        }

        $requiredColumns = [
            'rejection_reason' => 'VARCHAR(255) DEFAULT NULL',
            'profile_image' => 'VARCHAR(255) DEFAULT NULL',
            'api_token' => 'VARCHAR(128) DEFAULT NULL UNIQUE',
            'token_expires_at' => 'DATETIME DEFAULT NULL',
            'updated_at' => 'TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP'
        ];

        foreach ($requiredColumns as $column => $definition) {
            $columnExists = $pdo->query(sprintf("SHOW COLUMNS FROM `users` LIKE '%s'", str_replace("'", "''", $column)))
                ->fetch();

            if ($columnExists === false) {
                $pdo->exec(sprintf('ALTER TABLE `users` ADD COLUMN `%s` %s', $column, $definition));
            }
        }
    }

    private static function applySqlFile(PDO $pdo, string $filePath): void
    {
        if (!is_file($filePath)) {
            return;
        }

        $sql = file_get_contents($filePath);
        if ($sql === false || trim($sql) === '') {
            return;
        }

        $statements = preg_split('/;\s*(?:\r?\n|$)/', $sql);
        foreach ($statements as $statement) {
            $statement = trim((string) $statement);
            $statement = preg_replace('/^\s*(?:--.*(?:\r?\n|$)\s*)+/', '', $statement);
            $statement = preg_replace('/\s*--.*$/m', '', $statement);
            $statement = preg_replace('/\/\*.*?\*\//s', '', $statement);
            $statement = trim($statement);

            if ($statement === '') {
                continue;
            }

            if (preg_match('/^CREATE\s+DATABASE\b/i', $statement) === 1) {
                continue;
            }

            $statement = preg_replace('/^USE\s+`?campus_marketplace`?;?/i', '', $statement);
            $statement = trim($statement);
            if ($statement === '') {
                continue;
            }

            $pdo->exec($statement . ';');
        }
    }
}
