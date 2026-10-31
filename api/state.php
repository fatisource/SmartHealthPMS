<?php
declare(strict_types=1);

require_once __DIR__ . '/config.php';
require_once __DIR__ . '/modules/common.php';
require_once __DIR__ . '/modules/project.php';
require_once __DIR__ . '/modules/initiation.php';
require_once __DIR__ . '/modules/scope.php';
require_once __DIR__ . '/modules/schedule.php';
require_once __DIR__ . '/modules/quality.php';

careplanRequireLocalRequest();

function loadWorkspace(PDO $pdo): ?array
{
    $projectData = \CarePlan\Modules\Project\load($pdo);
    if (!$projectData) {
        return null;
    }
    $projectId = (int) $projectData['project']['id'];
    return array_merge(
        ['schemaVersion' => 1],
        $projectData,
        \CarePlan\Modules\Initiation\load($pdo, $projectId),
        \CarePlan\Modules\Scope\load($pdo, $projectId),
        \CarePlan\Modules\Schedule\load($pdo, $projectId),
        \CarePlan\Modules\Quality\load($pdo, $projectId),
    );
}

function saveWorkspace(PDO $pdo, array $state): void
{
    \CarePlan\Modules\validateWorkspace($state);
    $projectId = (int) ($state['project']['id'] ?? 1);
    $pdo->beginTransaction();
    try {
        \CarePlan\Modules\Quality\clear($pdo, $projectId);
        \CarePlan\Modules\Initiation\clearMilestones($pdo, $projectId);
        \CarePlan\Modules\Schedule\clear($pdo, $projectId);
        \CarePlan\Modules\Scope\clear($pdo, $projectId);
        \CarePlan\Modules\Initiation\clearPeople($pdo, $projectId);

        \CarePlan\Modules\Project\save($pdo, $state['project'], $state['plan']);
        \CarePlan\Modules\Initiation\savePeople($pdo, $projectId, $state);
        \CarePlan\Modules\Scope\save($pdo, $projectId, $state);
        \CarePlan\Modules\Schedule\save($pdo, $projectId, $state['activities'], $state['project']['start_date']);
        \CarePlan\Modules\Initiation\saveMilestones($pdo, $projectId, $state['milestones']);
        \CarePlan\Modules\Quality\save($pdo, $projectId, $state);
        $pdo->commit();
    } catch (Throwable $error) {
        if ($pdo->inTransaction()) {
            $pdo->rollBack();
        }
        throw $error;
    }
}

try {
    $pdo = careplanDatabase();
    if ($_SERVER['REQUEST_METHOD'] === 'GET') {
        careplanRespond(200, ['data' => loadWorkspace($pdo)]);
    }
    if ($_SERVER['REQUEST_METHOD'] !== 'PUT') {
        header('Allow: GET, PUT');
        careplanRespond(405, ['error' => 'Use GET to load or PUT to save the workspace.']);
    }
    $payload = json_decode(file_get_contents('php://input'), true, 512, JSON_THROW_ON_ERROR);
    saveWorkspace($pdo, $payload);
    careplanRespond(200, ['saved' => true]);
} catch (InvalidArgumentException $error) {
    careplanRespond(422, ['error' => $error->getMessage()]);
} catch (JsonException $error) {
    careplanRespond(400, ['error' => 'The request body must be valid JSON.']);
} catch (PDOException $error) {
    error_log('CarePlan database error: ' . $error->getMessage());
    careplanRespond(503, ['error' => 'Database unavailable or schema not installed. Confirm the existing spms_db tables and credentials in api/config.php.']);
} catch (Throwable $error) {
    error_log('CarePlan API error: ' . $error->getMessage());
    careplanRespond(500, ['error' => 'The workspace request failed. Check the PHP error log for details.']);
}
