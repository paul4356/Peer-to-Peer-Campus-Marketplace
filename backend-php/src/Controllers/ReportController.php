<?php
declare(strict_types=1);

namespace App\Controllers;

use App\Config\Database;
use App\Middleware\AuthMiddleware;
use App\Utils\Response;
use App\Utils\Validator;

class ReportController
{
    public function create(array $data): void
    {
        $user = AuthMiddleware::requireVerifiedStudent();

        $validator = new Validator();
        $validator->requireFields($data, ['listing_id', 'reason'])
                  ->validateMinLength($data['reason'] ?? '', 3, 'reason');

        if ($validator->hasErrors()) {
            Response::error($validator->getFirstError(), 422, $validator->getErrors());
        }

        $listingId = (int)$data['listing_id'];
        $reason = trim((string)$data['reason']);
        $details = trim((string)($data['details'] ?? ''));

        $pdo = Database::getConnection();

        // Check listing exists
        $chk = $pdo->prepare('SELECT id FROM listings WHERE id = :id LIMIT 1');
        $chk->execute(['id' => $listingId]);
        if (!$chk->fetch()) {
            Response::error('Listing not found.', 404);
        }

        $stmt = $pdo->prepare('
            INSERT INTO reports (reporter_id, listing_id, reason, details, status)
            VALUES (:reporter_id, :listing_id, :reason, :details, \'pending\')
        ');
        $stmt->execute([
            'reporter_id' => $user['id'],
            'listing_id' => $listingId,
            'reason' => $reason,
            'details' => $details
        ]);

        Response::json(['message' => 'Report submitted for administrator review.'], 201, 'Report created.');
    }
}
