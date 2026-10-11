<?php
declare(strict_types=1);

namespace App\Config;

use PDO;
use PDOException;
use App\Utils\Response;

class Database
{
    private static ?PDO $instance = null;

    public static function getConnection(): PDO
    {
        if (self::$instance === null) {
            $driver = Config::dbDriver();
            if (!in_array($driver, ['pgsql', 'mysql'], true)) {
                Response::error('Unsupported database driver. Set DB_DRIVER to pgsql or mysql.', 500);
            }

            $options = [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false,
            ];

            try {
                $url = Config::databaseUrl();
                if ($url !== '') {
                    if ($driver !== 'pgsql') {
                        Response::error('DATABASE_URL is supported for PostgreSQL. Set DB_DRIVER=mysql to use MySQL variables.', 500);
                    }
                    [$dsn, $username, $password] = self::postgresSettingsFromUrl($url);
                } else {
                    $dsn = self::dsnFromEnvironment($driver);
                    $username = Config::dbUser();
                    $password = Config::dbPass();
                }

                if ($driver === 'mysql' && !Config::hasExternalDatabaseConfig()) {
                    self::ensureDatabaseExists();
                }

                self::$instance = new PDO($dsn, $username, $password, $options);
                if ($driver === 'pgsql') {
                    self::ensurePostgresSchema(self::$instance);
                } else {
                    self::ensureMySqlSchema(self::$instance);
                }
            } catch (PDOException $e) {
                error_log('Marketplace database connection failed: ' . $e->getMessage());
                Response::error(
                    'Cannot connect to the database. Check the Render PostgreSQL DATABASE_URL or configure the database connection variables for a reachable database.',
                    500,
                    ['code' => 'DB_CONNECTION_ERROR']
                );
            }
        }

        return self::$instance;
    }

    /**
     * @return array{string, string, string}
     */
    private static function postgresSettingsFromUrl(string $url): array
    {
        $parts = parse_url($url);
        if (!is_array($parts) || !isset($parts['host'], $parts['path'])) {
            throw new \InvalidArgumentException('DATABASE_URL is not a valid PostgreSQL connection URL.');
        }

        $database = ltrim($parts['path'], '/');
        if ($database === '') {
            throw new \InvalidArgumentException('DATABASE_URL does not include a database name.');
        }

        parse_str($parts['query'] ?? '', $query);
        $sslMode = preg_replace('/[^a-zA-Z0-9_-]/', '', (string)($query['sslmode'] ?? 'prefer'));
        $dsn = sprintf(
            'pgsql:host=%s;port=%d;dbname=%s;sslmode=%s',
            $parts['host'],
            isset($parts['port']) ? (int)$parts['port'] : 5432,
            rawurldecode($database),
            $sslMode !== '' ? $sslMode : 'prefer'
        );

        return [
            $dsn,
            isset($parts['user']) ? rawurldecode($parts['user']) : '',
            isset($parts['pass']) ? rawurldecode($parts['pass']) : ''
        ];
    }

    private static function dsnFromEnvironment(string $driver): string
    {
        if ($driver === 'pgsql') {
            return sprintf(
                'pgsql:host=%s;port=%d;dbname=%s;sslmode=%s',
                Config::dbHost(),
                Config::dbPort(),
                Config::dbName(),
                getenv('DB_SSLMODE') ?: 'prefer'
            );
        }

        return sprintf(
            'mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4',
            Config::dbHost(),
            Config::dbPort(),
            Config::dbName()
        );
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

    private static function ensurePostgresSchema(PDO $pdo): void
    {
        self::applySqlFile($pdo, dirname(__DIR__, 2) . '/database/schema.postgresql.sql');

        $categoryCount = (int)$pdo->query('SELECT COUNT(*) FROM categories')->fetchColumn();
        $userCount = (int)$pdo->query('SELECT COUNT(*) FROM users')->fetchColumn();
        if ($categoryCount === 0 || $userCount === 0) {
            self::applySqlFile($pdo, dirname(__DIR__, 2) . '/database/seed.postgresql.sql');
        }
    }

    private static function ensureMySqlSchema(PDO $pdo): void
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
