<?php
declare(strict_types=1);

namespace App\Controllers;

use App\Config\Database;
use App\Middleware\AuthMiddleware;
use App\Utils\Response;
use PDO;

class SellerController
{
    public function dashboard(): void
    {
        $user = AuthMiddleware::authenticate();
        $pdo = Database::getConnection();

        // Seller statistics
        $statsStmt = $pdo->prepare('
            SELECT 
                COUNT(*) AS total_listings,
                COUNT(CASE WHEN status = "available" THEN 1 END) AS active_listings,
                COUNT(CASE WHEN status = "sold" THEN 1 END) AS sold_listings,
                COUNT(CASE WHEN status = "reserved" THEN 1 END) AS reserved_listings,
                COALESCE(SUM(CASE WHEN status = "sold" THEN price ELSE 0 END), 0) AS total_revenue,
                COALESCE(SUM(views_count), 0) AS total_views
            FROM listings
            WHERE seller_id = :seller_id
        ');
        $statsStmt->execute(['seller_id' => $user['id']]);
        $stats = $statsStmt->fetch();

        // Recent listings
        $listingsStmt = $pdo->prepare('
            SELECT l.id, l.title, l.price, l.condition_state, l.status, l.views_count, l.created_at,
                   c.name AS category_name
            FROM listings l
            JOIN categories c ON c.id = l.category_id
            WHERE l.seller_id = :seller_id
            ORDER BY l.created_at DESC
        ');
        $listingsStmt->execute(['seller_id' => $user['id']]);
        $listings = $listingsStmt->fetchAll();

        Response::json([
            'stats' => [
                'total_listings' => (int)$stats['total_listings'],
                'active_listings' => (int)$stats['active_listings'],
                'sold_listings' => (int)$stats['sold_listings'],
                'reserved_listings' => (int)$stats['reserved_listings'],
                'total_revenue' => (float)$stats['total_revenue'],
                'total_views' => (int)$stats['total_views']
            ],
            'listings' => $listings
        ], 200, 'Seller dashboard stats retrieved.');
    }
}
