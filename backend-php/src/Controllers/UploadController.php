<?php
declare(strict_types=1);

namespace App\Controllers;

use App\Middleware\AuthMiddleware;
use App\Utils\Response;

class UploadController
{
    private const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    public function listingImage(): void
    {
        AuthMiddleware::requireVerifiedStudent();

        $file = $_FILES['image'] ?? null;
        if (!is_array($file) || !isset($file['error'], $file['size'], $file['tmp_name'])) {
            Response::error('Please choose an item image to upload.', 422);
        }

        if ((int)$file['error'] !== UPLOAD_ERR_OK) {
            Response::error('The item image could not be uploaded.', 422);
        }

        if ((int)$file['size'] < 1 || (int)$file['size'] > self::MAX_IMAGE_SIZE) {
            Response::error('The item image must be no larger than 5 MB.', 422);
        }

        $temporaryPath = (string)$file['tmp_name'];
        if (!is_uploaded_file($temporaryPath)) {
            Response::error('The uploaded item image is invalid.', 422);
        }

        $mimeType = (new \finfo(FILEINFO_MIME_TYPE))->file($temporaryPath);
        $extensions = [
            'image/jpeg' => 'jpg',
            'image/png' => 'png',
            'image/webp' => 'webp'
        ];
        if (!is_string($mimeType) || !isset($extensions[$mimeType])) {
            Response::error('Only JPEG, PNG, and WebP images are supported.', 422);
        }

        $documentRoot = (string)($_SERVER['DOCUMENT_ROOT'] ?? '');
        if ($documentRoot === '') {
            Response::error('The public image directory is unavailable.', 500);
        }

        $uploadDirectory = rtrim($documentRoot, '/\\') . DIRECTORY_SEPARATOR . 'uploads' . DIRECTORY_SEPARATOR . 'items';
        if (!is_dir($uploadDirectory) && !mkdir($uploadDirectory, 0755, true) && !is_dir($uploadDirectory)) {
            Response::error('The image upload directory is unavailable.', 500);
        }

        $filename = bin2hex(random_bytes(16)) . '.' . $extensions[$mimeType];
        if (!move_uploaded_file($temporaryPath, $uploadDirectory . DIRECTORY_SEPARATOR . $filename)) {
            Response::error('The item image could not be saved.', 500);
        }

        Response::json(
            ['image_url' => 'uploads/items/' . $filename],
            201,
            'Item image uploaded successfully.'
        );
    }
}
