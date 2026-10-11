<?php
declare(strict_types=1);

/**
 * Peer-to-Peer Campus Marketplace
 * Comprehensive Automated API Smoke Test Suite
 * Tests all authentication, authorization, student verification, listings,
 * wishlist, moderation, and admin workflows over HTTP.
 */

$baseUrl = 'http://127.0.0.1:8000';
$passed = 0;
$failed = 0;

function request(string $method, string $path, ?array $data = null, ?string $token = null): array
{
    global $baseUrl;
    $url = $baseUrl . $path;
    $ch = curl_init($url);
    
    $headers = [
        'Accept: application/json',
        'Content-Type: application/json'
    ];
    if ($token) {
        $headers[] = "Authorization: Bearer {$token}";
    }

    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

    if ($data !== null) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    }

    $response = curl_exec($ch);
    $statusCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    $json = json_decode((string)$response, true);
    return ['status' => $statusCode, 'body' => $json];
}

function test(string $name, callable $fn): void
{
    global $passed, $failed;
    echo "Running: [{$name}] ... ";
    try {
        $fn();
        echo "PASS\n";
        $passed++;
    } catch (Throwable $e) {
        echo "FAIL\n  --> " . $e->getMessage() . "\n";
        $failed++;
    }
}

function assertEq($actual, $expected, string $msg = ''): void
{
    if ($actual !== $expected) {
        throw new Exception("Assertion Failed: Expected '{$expected}', got '{$actual}'. {$msg}");
    }
}

function assertTrue($condition, string $msg = ''): void
{
    if (!$condition) {
        throw new Exception("Assertion Failed: Expected true. {$msg}");
    }
}

echo "====================================================================\n";
echo "   PEER-TO-PEER CAMPUS MARKETPLACE - API AUTOMATED TEST SUITE       \n";
echo "====================================================================\n\n";

// 1. Health check
test('1. Health check returns 200 OK', function () {
    $res = request('GET', '/api/health');
    assertEq($res['status'], 200);
    assertTrue($res['body']['success'] === true);
});

// 2. Categories
test('2. Retrieve categories list', function () {
    $res = request('GET', '/api/categories');
    assertEq($res['status'], 200);
    assertTrue(count($res['body']['data']['categories']) >= 7);
});

// 3. Login with valid student credentials
$studentToken = null;
test('3. Student Login (Atharva Gokhale)', function () use (&$studentToken) {
    $res = request('POST', '/api/auth/login', [
        'email' => 'atharva@campus.edu',
        'password' => 'Student@123'
    ]);
    assertEq($res['status'], 200);
    assertTrue(!empty($res['body']['data']['token']));
    assertEq($res['body']['data']['user']['verification_status'], 'approved');
    $studentToken = $res['body']['data']['token'];
});

// 4. Login with invalid credentials
test('4. Reject login with invalid password', function () {
    $res = request('POST', '/api/auth/login', [
        'email' => 'atharva@campus.edu',
        'password' => 'WrongPassword!'
    ]);
    assertEq($res['status'], 401);
    assertTrue($res['body']['success'] === false);
});

// 5. Admin Login
$adminToken = null;
test('5. Admin Login (Prof. Sharma)', function () use (&$adminToken) {
    $res = request('POST', '/api/auth/login', [
        'email' => 'admin@campus.edu',
        'password' => 'Admin@123'
    ]);
    assertEq($res['status'], 200);
    assertEq($res['body']['data']['user']['role'], 'admin');
    $adminToken = $res['body']['data']['token'];
});

// 6. Register a new student (starts as pending)
$newStudentToken = null;
$newStudentId = null;
$randomRoll = '2026TEST' . rand(100, 999);
$testEmail = 'newtest' . rand(100, 999) . '@campus.edu';
test('6. Student Registration & Pending Status', function () use (&$newStudentToken, &$newStudentId, $randomRoll, $testEmail) {
    $res = request('POST', '/api/auth/register', [
        'student_id' => $randomRoll,
        'full_name' => 'Demo Test Student',
        'email' => $testEmail,
        'password' => 'Password@123',
        'phone' => '+91 99999 88888',
        'department' => 'Civil Engineering',
        'year_of_study' => 2
    ]);
    assertEq($res['status'], 201);
    assertEq($res['body']['data']['user']['verification_status'], 'pending');
    $newStudentToken = $res['body']['data']['token'];
    $newStudentId = $res['body']['data']['user']['id'];
});

// 7. Prevent duplicate email registration
test('7. Prevent duplicate email registration', function () use ($testEmail) {
    $res = request('POST', '/api/auth/register', [
        'student_id' => '2026DIFF' . rand(100, 999),
        'full_name' => 'Duplicate Attempt',
        'email' => $testEmail,
        'password' => 'Password@123'
    ]);
    assertEq($res['status'], 409);
});

// 8. Pending student cannot create listings
test('8. Enforce Pending Student restriction on listing creation', function () use ($newStudentToken) {
    $res = request('POST', '/api/listings', [
        'title' => 'Illegal Listing By Pending Student',
        'description' => 'Should be blocked until verified',
        'price' => 500,
        'category_id' => 1,
        'condition_state' => 'Good'
    ], $newStudentToken);
    assertEq($res['status'], 403, 'Must return 403 Forbidden');
});

// 9. Admin approves student verification
test('9. Admin approves pending student', function () use ($adminToken, $newStudentId) {
    $res = request('PATCH', "/api/admin/users/{$newStudentId}/verification", [
        'status' => 'approved'
    ], $adminToken);
    assertEq($res['status'], 200);
    assertEq($res['body']['data']['verification_status'], 'approved');
});

// 10. Newly approved student can now create a listing
$createdListingId = null;
test('10. Newly approved student can create listing', function () use ($newStudentToken, &$createdListingId) {
    $res = request('POST', '/api/listings', [
        'title' => 'Scientific Mini Drafter in Leather Case',
        'description' => 'Precision drafting arm with 360 degree protractor head. Ideal for first year graphics.',
        'price' => 320.00,
        'category_id' => 7,
        'condition_state' => 'Like New',
        'image_url' => 'assets/items/drafter.png'
    ], $newStudentToken);
    assertEq($res['status'], 201);
    assertTrue(isset($res['body']['data']['id']));
    $createdListingId = $res['body']['data']['id'];
});

// 11. Search and filter listings
test('11. Filter listings by keyword and category', function () {
    $res = request('GET', '/api/listings?q=Drafter&category_id=7');
    assertEq($res['status'], 200);
    assertTrue(count($res['body']['data']['listings']) >= 1);
});

// 12. Student ownership check on edit
test('12. Prevent other student from editing someone else listing', function () use ($studentToken, $createdListingId) {
    $res = request('PUT', "/api/listings/{$createdListingId}", [
        'title' => 'Malicious title change',
        'description' => 'Hacked description',
        'price' => 1.0,
        'category_id' => 7,
        'condition_state' => 'Good'
    ], $studentToken); // Atharva is NOT the owner of the demo student's listing
    assertEq($res['status'], 403, 'Must return 403 Forbidden for non-owner');
});

// 13. Owner edits own listing
test('13. Owner successfully updates own listing', function () use ($newStudentToken, $createdListingId) {
    $res = request('PUT', "/api/listings/{$createdListingId}", [
        'title' => 'Scientific Mini Drafter (Updated Price)',
        'description' => 'Precision drafting arm with 360 degree protractor head. Used for only 1 semester.',
        'price' => 290.00,
        'category_id' => 7,
        'condition_state' => 'Like New'
    ], $newStudentToken);
    assertEq($res['status'], 200);
});

// 14. Wishlist: Add item and list
test('14. Wishlist save and retrieve', function () use ($studentToken, $createdListingId) {
    // Add to wishlist
    $addRes = request('POST', "/api/me/wishlist/{$createdListingId}", null, $studentToken);
    assertTrue($addRes['status'] === 200 || $addRes['status'] === 201);

    // Retrieve wishlist
    $getRes = request('GET', '/api/me/wishlist', null, $studentToken);
    assertEq($getRes['status'], 200);
    $found = false;
    foreach ($getRes['body']['data']['wishlist'] as $item) {
        if ((int)$item['id'] === (int)$createdListingId) {
            $found = true;
            break;
        }
    }
    assertTrue($found, 'Listing must exist in wishlist');
});

// 15. Wishlist: Remove item
test('15. Wishlist remove item', function () use ($studentToken, $createdListingId) {
    $res = request('DELETE', "/api/me/wishlist/{$createdListingId}", null, $studentToken);
    assertEq($res['status'], 200);
});

// 16. Admin Dashboard access and Student restriction
test('16. Student cannot access Admin Dashboard (RBAC check)', function () use ($studentToken) {
    $res = request('GET', '/api/admin/dashboard', null, $studentToken);
    assertEq($res['status'], 403);
});

test('17. Admin successfully accesses Admin Dashboard', function () use ($adminToken) {
    $res = request('GET', '/api/admin/dashboard', null, $adminToken);
    assertEq($res['status'], 200);
    assertTrue(isset($res['body']['data']['metrics']['total_users']));
});

// 18. Clean up test listing
test('18. Owner deletes listing', function () use ($newStudentToken, $createdListingId) {
    $res = request('DELETE', "/api/listings/{$createdListingId}", null, $newStudentToken);
    assertEq($res['status'], 200);
});

echo "\n--------------------------------------------------------------------\n";
echo "SUMMARY: Passed: {$passed} | Failed: {$failed}\n";
echo "--------------------------------------------------------------------\n";

if ($failed > 0) {
    exit(1);
}
