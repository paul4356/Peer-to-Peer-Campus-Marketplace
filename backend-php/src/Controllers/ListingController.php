<?php
declare(strict_types=1);

namespace App\Controllers;

use App\Config\Database;
use App\Config\Config;
use App\Middleware\AuthMiddleware;
use App\Utils\Response;
use App\Utils\Validator;
use PDO;

class ListingController
{
    public function index(): void
    {
        $pdo = Database::getConnection();

        $query = isset($_GET['q']) ? trim((string)$_GET['q']) : '';
        $categoryId = isset($_GET['category_id']) && is_numeric($_GET['category_id']) ? (int)$_GET['category_id'] : null;
        $condition = isset($_GET['condition']) ? trim((string)$_GET['condition']) : '';
        $minPrice = isset($_GET['min_price']) && is_numeric($_GET['min_price']) ? (float)$_GET['min_price'] : null;
        $maxPrice = isset($_GET['max_price']) && is_numeric($_GET['max_price']) ? (float)$_GET['max_price'] : null;
        $sort = isset($_GET['sort']) ? trim((string)$_GET['sort']) : 'newest';
        $status = isset($_GET['status']) ? trim((string)$_GET['status']) : 'available';
        $page = max(1, isset($_GET['page']) ? (int)$_GET['page'] : 1);
        $limit = min(50, max(1, isset($_GET['limit']) ? (int)$_GET['limit'] : 24));
        $offset = ($page - 1) * $limit;

        $where = [];
        $params = [];

        if ($status !== 'all') {
            $where[] = 'l.status = :status';
            $params['status'] = $status;
        }

        if ($query !== '') {
            $where[] = '(l.title LIKE :query_title OR l.description LIKE :query_desc)';
            $params['query_title'] = "%{$query}%";
            $params['query_desc'] = "%{$query}%";
        }

        if ($categoryId !== null && $categoryId > 0) {
            $where[] = 'l.category_id = :category_id';
            $params['category_id'] = $categoryId;
        }

        if ($condition !== '' && in_array($condition, ['New', 'Like New', 'Good', 'Acceptable'], true)) {
            $where[] = 'l.condition_state = :condition_state';
            $params['condition_state'] = $condition;
        }

        if ($minPrice !== null && $minPrice >= 0) {
            $where[] = 'l.price >= :min_price';
            $params['min_price'] = $minPrice;
        }

        if ($maxPrice !== null && $maxPrice > 0) {
            $where[] = 'l.price <= :max_price';
            $params['max_price'] = $maxPrice;
        }

        $whereClause = !empty($where) ? 'WHERE ' . implode(' AND ', $where) : '';

        // Order clause
        $orderClause = match ($sort) {
            'price_asc' => 'ORDER BY l.price ASC',
            'price_desc' => 'ORDER BY l.price DESC',
            'views_desc' => 'ORDER BY l.views_count DESC, l.created_at DESC',
            default => 'ORDER BY l.created_at DESC'
        };

        // Total count
        $countSql = "SELECT COUNT(*) FROM listings l {$whereClause}";
        $countStmt = $pdo->prepare($countSql);
        $countStmt->execute($params);
        $totalListings = (int)$countStmt->fetchColumn();

        // Fetch data
        $sql = "
            SELECT 
                l.id, l.title, l.description, l.price, l.condition_state, l.image_url, 
                l.status, l.views_count, l.contact_preference, l.created_at, l.updated_at,
                c.id AS category_id, c.name AS category_name, c.slug AS category_slug,
                u.id AS seller_id, u.full_name AS seller_name, u.email AS seller_email, 
                u.phone AS seller_phone, u.department AS seller_department, u.verification_status AS seller_status
            FROM listings l
            JOIN categories c ON c.id = l.category_id
            JOIN users u ON u.id = l.seller_id
            {$whereClause}
            {$orderClause}
            LIMIT :limit OFFSET :offset
        ";

        $stmt = $pdo->prepare($sql);
        foreach ($params as $k => $v) {
            $stmt->bindValue(":{$k}", $v);
        }
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();
        $rows = $stmt->fetchAll();

        $listings = array_map(function ($row) {
            return [
                'id' => (int)$row['id'],
                'title' => $row['title'],
                'description' => $row['description'],
                'price' => (float)$row['price'],
                'condition_state' => $row['condition_state'],
                'image_url' => $row['image_url'],
                'status' => $row['status'],
                'views_count' => (int)$row['views_count'],
                'contact_preference' => $row['contact_preference'],
                'created_at' => $row['created_at'],
                'category' => [
                    'id' => (int)$row['category_id'],
                    'name' => $row['category_name'],
                    'slug' => $row['category_slug']
                ],
                'seller' => [
                    'id' => (int)$row['seller_id'],
                    'name' => $row['seller_name'],
                    'email' => $row['seller_email'],
                    'phone' => $row['seller_phone'],
                    'department' => $row['seller_department'],
                    'verification_status' => $row['seller_status']
                ]
            ];
        }, $rows);

        Response::json([
            'listings' => $listings,
            'pagination' => [
                'total' => $totalListings,
                'page' => $page,
                'limit' => $limit,
                'pages' => ceil($totalListings / $limit)
            ]
        ], 200, 'Listings fetched successfully.');
    }

    public function show(int $id): void
    {
        $pdo = Database::getConnection();

        // Increment view count
        $incStmt = $pdo->prepare('UPDATE listings SET views_count = views_count + 1 WHERE id = :id');
        $incStmt->execute(['id' => $id]);

        $sql = '
            SELECT 
                l.id, l.title, l.description, l.price, l.condition_state, l.image_url, 
                l.status, l.views_count, l.contact_preference, l.created_at, l.updated_at,
                c.id AS category_id, c.name AS category_name, c.slug AS category_slug,
                u.id AS seller_id, u.full_name AS seller_name, u.email AS seller_email, 
                u.phone AS seller_phone, u.department AS seller_department, u.verification_status AS seller_status
            FROM listings l
            JOIN categories c ON c.id = l.category_id
            JOIN users u ON u.id = l.seller_id
            WHERE l.id = :id
            LIMIT 1
        ';

        $stmt = $pdo->prepare($sql);
        $stmt->execute(['id' => $id]);
        $row = $stmt->fetch();

        if (!$row) {
            Response::error('Listing not found.', 404);
        }

        $listing = [
            'id' => (int)$row['id'],
            'title' => $row['title'],
            'description' => $row['description'],
            'price' => (float)$row['price'],
            'condition_state' => $row['condition_state'],
            'image_url' => $row['image_url'],
            'status' => $row['status'],
            'views_count' => (int)$row['views_count'],
            'contact_preference' => $row['contact_preference'],
            'created_at' => $row['created_at'],
            'updated_at' => $row['updated_at'],
            'category' => [
                'id' => (int)$row['category_id'],
                'name' => $row['category_name'],
                'slug' => $row['category_slug']
            ],
            'seller' => [
                'id' => (int)$row['seller_id'],
                'name' => $row['seller_name'],
                'email' => $row['seller_email'],
                'phone' => $row['seller_phone'],
                'department' => $row['seller_department'],
                'verification_status' => $row['seller_status']
            ]
        ];

        Response::json(['listing' => $listing], 200, 'Listing details retrieved.');
    }

    public function create(array $data): void
    {
        // Enforce verified student check
        $user = AuthMiddleware::requireVerifiedStudent();

        $validator = new Validator();
        $validator->requireFields($data, ['title', 'description', 'price', 'category_id', 'condition_state'])
                  ->validatePrice($data['price'] ?? 0)
                  ->validateMinLength($data['title'] ?? '', 4, 'title')
                  ->validateMinLength($data['description'] ?? '', 10, 'description');

        if ($validator->hasErrors()) {
            Response::error($validator->getFirstError(), 422, $validator->getErrors());
        }

        $pdo = Database::getConnection();

        // Check valid category
        $catStmt = $pdo->prepare('SELECT id FROM categories WHERE id = :id LIMIT 1');
        $catStmt->execute(['id' => (int)$data['category_id']]);
        if (!$catStmt->fetch()) {
            Response::error('Selected category is invalid.', 422);
        }

        $stmt = $pdo->prepare('
            INSERT INTO listings 
            (seller_id, category_id, title, description, price, condition_state, image_url, status, contact_preference)
            VALUES 
            (:seller_id, :category_id, :title, :description, :price, :condition_state, :image_url, \'available\', :contact_preference)
            ' . (Config::dbDriver() === 'pgsql' ? 'RETURNING id' : '') . '
        ');

        $imageUrl = trim((string)($data['image_url'] ?? 'assets/items/default_item.png'));
        if ($imageUrl === '') {
            $imageUrl = 'assets/items/default_item.png';
        }

        $stmt->execute([
            'seller_id' => $user['id'],
            'category_id' => (int)$data['category_id'],
            'title' => trim((string)$data['title']),
            'description' => trim((string)$data['description']),
            'price' => (float)$data['price'],
            'condition_state' => trim((string)$data['condition_state']),
            'image_url' => $imageUrl,
            'contact_preference' => trim((string)($data['contact_preference'] ?? 'chat'))
        ]);

        $newId = Config::dbDriver() === 'pgsql'
            ? (int)$stmt->fetchColumn()
            : (int)$pdo->lastInsertId();

        Response::json(['id' => $newId, 'message' => 'Listing published successfully!'], 201, 'Listing created.');
    }

    public function update(int $id, array $data): void
    {
        $user = AuthMiddleware::authenticate();
        $pdo = Database::getConnection();

        // Verify listing exists and user owns it or is admin
        $chk = $pdo->prepare('SELECT id, seller_id FROM listings WHERE id = :id LIMIT 1');
        $chk->execute(['id' => $id]);
        $listing = $chk->fetch();

        if (!$listing) {
            Response::error('Listing not found.', 404);
        }

        if ((int)$listing['seller_id'] !== (int)$user['id'] && $user['role'] !== 'admin') {
            Response::error('Forbidden: You can only edit your own listings.', 403);
        }

        $validator = new Validator();
        $validator->requireFields($data, ['title', 'description', 'price', 'category_id', 'condition_state'])
                  ->validatePrice($data['price'] ?? 0)
                  ->validateMinLength($data['title'] ?? '', 4, 'title');

        if ($validator->hasErrors()) {
            Response::error($validator->getFirstError(), 422, $validator->getErrors());
        }

        $stmt = $pdo->prepare('
            UPDATE listings 
            SET title = :title, 
                description = :description, 
                price = :price, 
                category_id = :category_id, 
                condition_state = :condition_state,
                image_url = :image_url,
                contact_preference = :contact_preference,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = :id
        ');

        $imageUrl = trim((string)($data['image_url'] ?? 'assets/items/default_item.png'));
        if ($imageUrl === '') {
            $imageUrl = 'assets/items/default_item.png';
        }

        $stmt->execute([
            'title' => trim((string)$data['title']),
            'description' => trim((string)$data['description']),
            'price' => (float)$data['price'],
            'category_id' => (int)$data['category_id'],
            'condition_state' => trim((string)$data['condition_state']),
            'image_url' => $imageUrl,
            'contact_preference' => trim((string)($data['contact_preference'] ?? 'chat')),
            'id' => $id
        ]);

        Response::json(['id' => $id, 'message' => 'Listing updated successfully.'], 200, 'Listing updated.');
    }

    public function delete(int $id): void
    {
        $user = AuthMiddleware::authenticate();
        $pdo = Database::getConnection();

        $chk = $pdo->prepare('SELECT id, seller_id FROM listings WHERE id = :id LIMIT 1');
        $chk->execute(['id' => $id]);
        $listing = $chk->fetch();

        if (!$listing) {
            Response::error('Listing not found.', 404);
        }

        if ((int)$listing['seller_id'] !== (int)$user['id'] && $user['role'] !== 'admin') {
            Response::error('Forbidden: You can only delete your own listings.', 403);
        }

        $del = $pdo->prepare('DELETE FROM listings WHERE id = :id');
        $del->execute(['id' => $id]);

        Response::json(null, 200, 'Listing deleted successfully.');
    }

    public function updateStatus(int $id, array $data): void
    {
        $user = AuthMiddleware::authenticate();
        $status = trim((string)($data['status'] ?? ''));

        if (!in_array($status, ['available', 'sold', 'reserved', 'inactive'], true)) {
            Response::error('Invalid status value. Allowed: available, sold, reserved, inactive.', 422);
        }

        $pdo = Database::getConnection();
        $chk = $pdo->prepare('SELECT id, seller_id FROM listings WHERE id = :id LIMIT 1');
        $chk->execute(['id' => $id]);
        $listing = $chk->fetch();

        if (!$listing) {
            Response::error('Listing not found.', 404);
        }

        if ((int)$listing['seller_id'] !== (int)$user['id'] && $user['role'] !== 'admin') {
            Response::error('Forbidden: You can only update the status of your own listings.', 403);
        }

        $stmt = $pdo->prepare('UPDATE listings SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE id = :id');
        $stmt->execute(['status' => $status, 'id' => $id]);

        Response::json(['id' => $id, 'status' => $status], 200, 'Listing status updated to ' . $status . '.');
    }

    public function myListings(): void
    {
        $user = AuthMiddleware::authenticate();
        $pdo = Database::getConnection();

        $sql = '
            SELECT 
                l.id, l.title, l.description, l.price, l.condition_state, l.image_url, 
                l.status, l.views_count, l.created_at,
                c.id AS category_id, c.name AS category_name
            FROM listings l
            JOIN categories c ON c.id = l.category_id
            WHERE l.seller_id = :seller_id
            ORDER BY l.created_at DESC
        ';

        $stmt = $pdo->prepare($sql);
        $stmt->execute(['seller_id' => $user['id']]);
        $rows = $stmt->fetchAll();

        Response::json(['listings' => $rows], 200, 'User listings retrieved.');
    }
}
