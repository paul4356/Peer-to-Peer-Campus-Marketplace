<?php
declare(strict_types=1);

namespace App\Controllers;

use App\Config\Database;
use App\Utils\Response;

class CategoryController
{
    public function index(): void
    {
        $pdo = Database::getConnection();
        $sql = '
            SELECT c.id, c.name, c.slug, c.description, c.icon_name, 
                   COUNT(CASE WHEN l.status = \'available\' THEN 1 END) AS active_listings_count
            FROM categories c
            LEFT JOIN listings l ON l.category_id = c.id
            GROUP BY c.id
            ORDER BY c.id ASC
        ';
        $stmt = $pdo->query($sql);
        $categories = $stmt->fetchAll();

        Response::json(['categories' => $categories], 200, 'Categories retrieved successfully.');
    }
}
