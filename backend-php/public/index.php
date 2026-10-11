<?php
declare(strict_types=1);

// Enable error reporting in development but format neatly as JSON
error_reporting(E_ALL);
ini_set('display_errors', '0');

// Set JSON headers and CORS headers
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With, X-API-Token');

// Handle pre-flight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

// PSR-4 Autoloader
spl_autoload_register(function (string $class) {
    $prefix = 'App\\';
    $baseDir = dirname(__DIR__) . '/';

    if (strncmp($prefix, $class, strlen($prefix)) !== 0) {
        return;
    }

    $relativeClass = substr($class, strlen($prefix));
    
    // Map Config directly to config/
    if (str_starts_with($relativeClass, 'Config\\')) {
        $file = $baseDir . 'config/' . substr($relativeClass, 7) . '.php';
    } else {
        $file = $baseDir . 'src/' . str_replace('\\', '/', $relativeClass) . '.php';
    }

    if (file_exists($file)) {
        require_once $file;
    }
});

use App\Controllers\AuthController;
use App\Controllers\ProfileController;
use App\Controllers\CategoryController;
use App\Controllers\ListingController;
use App\Controllers\WishlistController;
use App\Controllers\SellerController;
use App\Controllers\AdminController;
use App\Controllers\ReportController;
use App\Utils\Response;

// Global exception handler
set_exception_handler(function (Throwable $e) {
    error_log('Marketplace API error: ' . $e->getMessage());
    Response::error('An unexpected server error occurred. Check the service logs for details.', 500);
});

// Parse request URI and HTTP method
$requestUri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$method = $_SERVER['REQUEST_METHOD'];

// Parse JSON body if present
$rawBody = file_get_contents('php://input');
$body = [];
if (!empty($rawBody)) {
    $decoded = json_decode($rawBody, true);
    if (is_array($decoded)) {
        $body = $decoded;
    }
}
// Merge with $_POST for form-data compatibility
if (!empty($_POST)) {
    $body = array_merge($body, $_POST);
}

// Route normalization (strip trailing slash and optional prefix)
$path = rtrim($requestUri, '/');
if (str_starts_with($path, '/backend-php/public')) {
    $path = substr($path, strlen('/backend-php/public'));
}
if ($path === '') {
    $path = '/';
}

// -----------------------------------------------------------------------------
// Router Dispatcher
// -----------------------------------------------------------------------------

// 1. Health check
if ($path === '/api/health' && $method === 'GET') {
    Response::json([
        'status' => 'OK',
        'service' => 'Peer-to-Peer Campus Marketplace API',
        'version' => '1.0.0',
        'timestamp' => date('c'),
        'database' => 'Connected'
    ], 200, 'Health check passed.');
}

// 2. Authentication routes
if ($path === '/api/auth/register' && $method === 'POST') {
    (new AuthController())->register($body);
}

if ($path === '/api/auth/login' && $method === 'POST') {
    (new AuthController())->login($body);
}

if ($path === '/api/auth/logout' && $method === 'POST') {
    (new AuthController())->logout();
}

if ($path === '/api/auth/me' && $method === 'GET') {
    (new AuthController())->me();
}

// 3. Profile update
if ($path === '/api/profile' && ($method === 'PUT' || $method === 'POST')) {
    (new ProfileController())->update($body);
}

// 4. Categories
if ($path === '/api/categories' && $method === 'GET') {
    (new CategoryController())->index();
}

// 5. Listings
if ($path === '/api/listings' && $method === 'GET') {
    (new ListingController())->index();
}

if ($path === '/api/listings' && $method === 'POST') {
    (new ListingController())->create($body);
}

if (preg_match('#^/api/listings/(\d+)$#', $path, $matches)) {
    $id = (int)$matches[1];
    if ($method === 'GET') {
        (new ListingController())->show($id);
    } elseif ($method === 'PUT' || $method === 'POST') {
        (new ListingController())->update($id, $body);
    } elseif ($method === 'DELETE') {
        (new ListingController())->delete($id);
    }
}

if (preg_match('#^/api/listings/(\d+)/status$#', $path, $matches) && ($method === 'PATCH' || $method === 'POST')) {
    (new ListingController())->updateStatus((int)$matches[1], $body);
}

// 6. User-specific listing endpoints
if ($path === '/api/me/listings' && $method === 'GET') {
    (new ListingController())->myListings();
}

// 7. Wishlist endpoints
if ($path === '/api/me/wishlist' && $method === 'GET') {
    (new WishlistController())->index();
}

if (preg_match('#^/api/me/wishlist/(\d+)$#', $path, $matches)) {
    $listingId = (int)$matches[1];
    if ($method === 'POST') {
        (new WishlistController())->add($listingId);
    } elseif ($method === 'DELETE') {
        (new WishlistController())->remove($listingId);
    }
}

// 8. Seller Dashboard
if ($path === '/api/seller/dashboard' && $method === 'GET') {
    (new SellerController())->dashboard();
}

// 9. Reports
if ($path === '/api/reports' && $method === 'POST') {
    (new ReportController())->create($body);
}

// 10. Admin Endpoints
if ($path === '/api/admin/dashboard' && $method === 'GET') {
    (new AdminController())->dashboard();
}

if ($path === '/api/admin/verifications' && $method === 'GET') {
    (new AdminController())->verifications();
}

if (preg_match('#^/api/admin/users/(\d+)/verification$#', $path, $matches) && ($method === 'PATCH' || $method === 'POST')) {
    (new AdminController())->updateVerification((int)$matches[1], $body);
}

if ($path === '/api/admin/users' && $method === 'GET') {
    (new AdminController())->users();
}

if ($path === '/api/admin/reports' && $method === 'GET') {
    (new AdminController())->reports();
}

if ($path === '/api/admin/audit-logs' && $method === 'GET') {
    (new AdminController())->auditLogs();
}

// Fallback for unmatched routes
Response::error("Route '{$path}' [{$method}] not found.", 404);
