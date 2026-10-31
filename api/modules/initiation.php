<?php
declare(strict_types=1);

namespace CarePlan\Modules\Initiation;

use PDO;
use function CarePlan\Modules\insertRows;
use function CarePlan\Modules\nullableInt;
use function CarePlan\Modules\readModule;

function load(PDO $pdo, int $projectId): array
{
    return [
        'stakeholders' => readModule($pdo, 'stakeholder', '`id`, `name`, `role`, `interest`', $projectId, ['id']),
        'team' => readModule($pdo, 'team_member', '`id`, `name`, `role`', $projectId, ['id']),
        'milestones' => readModule($pdo, 'milestone', '`id`, `name`, `target_date`, `status`, `responsible_id`, `activity_id`', $projectId, ['id', 'responsible_id', 'activity_id']),
    ];
}

function clearMilestones(PDO $pdo, int $projectId): void
{
    $statement = $pdo->prepare('DELETE FROM `milestone` WHERE `project_id` = ?');
    $statement->execute([$projectId]);
}

function clearPeople(PDO $pdo, int $projectId): void
{
    foreach (['stakeholder', 'team_member'] as $table) {
        $statement = $pdo->prepare("DELETE FROM `$table` WHERE `project_id` = ?");
        $statement->execute([$projectId]);
    }
}

function savePeople(PDO $pdo, int $projectId, array $state): void
{
    insertRows($pdo, 'stakeholder', ['id', 'project_id', 'name', 'role', 'interest'], $state['stakeholders'], static fn(array $r): array => [
        (int) $r['id'], $projectId, $r['name'], $r['role'], $r['interest'] ?? '',
    ]);
    insertRows($pdo, 'team_member', ['id', 'project_id', 'name', 'role'], $state['team'], static fn(array $r): array => [
        (int) $r['id'], $projectId, $r['name'], $r['role'],
    ]);
}

function saveMilestones(PDO $pdo, int $projectId, array $records): void
{
    insertRows($pdo, 'milestone', ['id', 'project_id', 'name', 'target_date', 'status', 'responsible_id', 'activity_id'], $records, static fn(array $r): array => [
        (int) $r['id'], $projectId, $r['name'], $r['target_date'], $r['status'] ?? 'Pending',
        nullableInt($r['responsible_id'] ?? null), nullableInt($r['activity_id'] ?? null),
    ]);
}
