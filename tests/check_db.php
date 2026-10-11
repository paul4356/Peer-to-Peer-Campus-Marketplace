<?php
require_once dirname(__DIR__) . '/backend-php/config/Config.php';
require_once dirname(__DIR__) . '/backend-php/config/Database.php';

$pdo = App\Config\Database::getConnection();
echo "Connected to: " . $pdo->getAttribute(PDO::ATTR_SERVER_VERSION) . "\n";
echo "Database: " . $pdo->query("SELECT DATABASE()")->fetchColumn() . "\n";

$cols = $pdo->query("SHOW COLUMNS FROM users")->fetchAll(PDO::FETCH_COLUMN);
echo "Columns: " . implode(', ', $cols) . "\n";
