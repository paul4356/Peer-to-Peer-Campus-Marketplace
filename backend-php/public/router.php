<?php
declare(strict_types=1);

// Built-in PHP server router
$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$file = __DIR__ . $uri;

if ($uri !== '/' && file_exists($file) && !is_dir($file)) {
    return false; // serve the requested resource as is
}

require_once __DIR__ . '/index.php';
