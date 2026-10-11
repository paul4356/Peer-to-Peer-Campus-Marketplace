<?php
declare(strict_types=1);

namespace App\Config;

class Config
{
    public const DB_HOST = '127.0.0.1';
    public const DB_PORT = 3306;
    public const DB_NAME = 'campus_marketplace';
    public const DB_USER = 'root';
    public const DB_PASS = '';
    public const APP_NAME = 'Peer-to-Peer Campus Marketplace';
    public const API_VERSION = 'v1.0';
    public const TOKEN_EXPIRY_HOURS = 72;
}
