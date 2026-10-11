<?php
declare(strict_types=1);

namespace App\Config;

class Config
{
    public const APP_NAME = 'Peer-to-Peer Campus Marketplace';
    public const API_VERSION = 'v1.0';
    public const TOKEN_EXPIRY_HOURS = 72;

    public static function dbDriver(): string
    {
        $configuredDriver = self::environmentValue(['DB_DRIVER'], '');
        if ($configuredDriver !== '') {
            return strtolower($configuredDriver);
        }

        if (self::environmentValue(['DATABASE_URL', 'PGHOST'], '') !== '') {
            return 'pgsql';
        }

        return 'mysql';
    }

    public static function databaseUrl(): string
    {
        return self::environmentValue(['DATABASE_URL'], '');
    }

    public static function dbHost(): string
    {
        return self::environmentValue(['DB_HOST', 'PGHOST', 'MYSQLHOST'], '127.0.0.1');
    }

    public static function dbPort(): int
    {
        $default = self::dbDriver() === 'pgsql' ? '5432' : '3306';
        return (int) self::environmentValue(['DB_PORT', 'PGPORT', 'MYSQLPORT'], $default);
    }

    public static function dbName(): string
    {
        return self::environmentValue(['DB_NAME', 'PGDATABASE', 'MYSQLDATABASE'], 'campus_marketplace');
    }

    public static function dbUser(): string
    {
        return self::environmentValue(['DB_USER', 'PGUSER', 'MYSQLUSER'], 'root');
    }

    public static function dbPass(): string
    {
        return self::environmentValue(['DB_PASS', 'PGPASSWORD', 'MYSQLPASSWORD'], '');
    }

    public static function hasExternalDatabaseConfig(): bool
    {
        foreach (['DB_HOST', 'MYSQLHOST'] as $name) {
            $value = getenv($name);
            if ($value !== false && $value !== '') {
                return true;
            }
        }

        return false;
    }

    /**
     * @param list<string> $names
     */
    private static function environmentValue(array $names, string $default): string
    {
        foreach ($names as $name) {
            $value = getenv($name);
            if ($value !== false && $value !== '') {
                return $value;
            }
        }

        return $default;
    }
}
