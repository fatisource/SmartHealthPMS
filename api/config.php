<?php
declare(strict_types=1);

$dbSettings = [
    'host' => getenv('CAREPLAN_DB_HOST') ?: '127.0.0.1',
    'name' => getenv('CAREPLAN_DB_NAME') ?: 'spms_db',
    'username' => getenv('CAREPLAN_DB_USER') ?: 'root',
    'password' => getenv('CAREPLAN_DB_PASS') ?: '',
];

$localConfigPath = __DIR__ . '/config.local.php';
if (is_file($localConfigPath)) {
    $localSettings = require $localConfigPath;
    if (!is_array($localSettings)) {
        throw new RuntimeException('api/config.local.php must return a database settings array.');
    }
    $dbSettings = array_replace($dbSettings, $localSettings);
}

function careplanDatabase(): PDO
{
    global $dbSettings;
    $dsn = 'mysql:host=' . $dbSettings['host'] . ';dbname=' . $dbSettings['name'] . ';charset=utf8mb4';
    return new PDO($dsn, $dbSettings['username'], $dbSettings['password'], [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
}

function careplanRespond(int $status, array $body): never
{
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-store');
    echo json_encode($body, JSON_UNESCAPED_UNICODE | JSON_INVALID_UTF8_SUBSTITUTE);
    exit;
}

function careplanRequireLocalRequest(): void
{
    $address = $_SERVER['REMOTE_ADDR'] ?? '';
    if (!in_array($address, ['127.0.0.1', '::1'], true)) {
        careplanRespond(403, ['error' => 'This local XAMPP API only accepts connections from this computer.']);
    }
}
