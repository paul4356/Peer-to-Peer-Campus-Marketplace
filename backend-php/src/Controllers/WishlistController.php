<?php
declare(strict_types=1);

namespace App\Controllers;

use App\Config\Database;
use App\Middleware\AuthMiddleware;
use App\Utils\Response;
use PDO;

class WishlistController
{
    public function index(): void
    {
        $user = AuthMiddleware::authenticate();
        $pdo = Database::getConnection();

        $sql = '
            SELECT 
                w.id AS wishlist_id, w.created_at AS saved_at,
                l.id, l.title, l.description, l.price, l.condition_state, l.image_url, 
                l.status, l.views_count, l.created_at,
                c.id AS category_id, c.name AS category_name,
                u.id AS seller_id, u.full_name AS seller_name, u.email AS seller_email, u.phone AS seller_phone
            FROM wishlist w
            JOIN listings l ON l.id = w.listing_id
            JOIN categories c ON c.id = l.category_id
            JOIN users u ON u.id = l.seller_id
            WHERE w.user_id = :user_id
            ORDER BY w.created_at DESC
        ';

        $stmt = $pdo->prepare($sql);
        $stmt->execute(['user_id' => $user['id']]);
        $rows = $stmt->fetchAll();

        Response::json(['wishlist' => $rows], 200, 'Wishlist items retrieved.');
    }

    public function add(int $listingId): void
    {
        $user = AuthMiddleware::authenticate();
        $pdo = Database::getConnection();

        // Verify listing exists
        $chk = $pdo->prepare('SELECT id FROM listings WHERE id = :id LIMIT 1');
        $chk->execute(['id' => $listingId]);
        if (!$chk->fetch()) {
            Response::error('Listing not found.', 404);
        }

        // Check if already in wishlist
        $exist = $pdo->prepare('SELECT id FROM wishlist WHERE user_id = :user_id AND listing_id = :listing_id LIMIT 1');
        $exist->execute(['user_id' => $user['id'], 'listing_id' => $listingId]);
        if ($exist->fetch()) {
            Response::json(['message' => 'Listing is already in your wishlist.'], 200, 'Already saved.');
        }

        $stmt = $pdo->prepare('INSERT INTO wishlist (user_id, listing_id) VALUES (:user_id, :listing_id)');
        $stmt->execute(['user_id' => $user['id'], 'listing_id' => $listingId]);

        Response::json(['message' => 'Item added to your wishlist.'], 201, 'Saved to wishlist.');
    }

    public function remove(int $listingId): void
    {
        $user = AuthMiddleware::authenticate();
        $pdo = Database::getConnection();

        $stmt = $pdo->prepare('DELETE FROM wishlist WHERE user_id = :user_id AND listing_id = :listing_id');
        $stmt->execute(['user_id' => $user['id'], 'listing_id' => $listingId]);

        Response::json(['message' => 'Item removed from your wishlist.'], 200, 'Removed from wishlist.');
    }
}
