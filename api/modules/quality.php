<?php
declare(strict_types=1);

namespace CarePlan\Modules\Quality;

use PDO;
use function CarePlan\Modules\insertRows;
use function CarePlan\Modules\nullableDate;
use function CarePlan\Modules\nullableInt;
use function CarePlan\Modules\readModule;

function load(PDO $pdo, int $projectId): array
{
    return [
        'defects' => readModule($pdo, 'defect', '`id`, `code`, `severity`, `title`, `status`, `assigned_to`, `activity_id`, `requirement_id`, `reported_date`, `resolved_date`', $projectId, ['id', 'assigned_to', 'activity_id', 'requirement_id']),
        'quality' => readModule($pdo, 'quality_check', '`id`, `wbs_id`, `item`, `result`, `inspector_id`, `inspection_date`, `notes`', $projectId, ['id', 'wbs_id', 'inspector_id']),
        'risks' => readModule($pdo, 'risk', '`id`, `code`, `description`, `likelihood`, `impact`, `preventive_action`, `corrective_action`, `owner_id`, `status`', $projectId, ['id', 'likelihood', 'impact', 'owner_id']),
    ];
}

function clear(PDO $pdo, int $projectId): void
{
    foreach (['quality_check', 'risk', 'defect'] as $table) {
        $statement = $pdo->prepare("DELETE FROM `$table` WHERE `project_id` = ?");
        $statement->execute([$projectId]);
    }
}

function save(PDO $pdo, int $projectId, array $state): void
{
    insertRows($pdo, 'defect', [
        'id', 'project_id', 'code', 'title', 'severity', 'status', 'assigned_to',
        'activity_id', 'requirement_id', 'reported_date', 'resolved_date',
    ], $state['defects'], static fn(array $r): array => [
        (int) $r['id'], $projectId, $r['code'], $r['title'], $r['severity'] ?? 'Medium',
        $r['status'] ?? 'Open', nullableInt($r['assigned_to'] ?? null),
        nullableInt($r['activity_id'] ?? null), nullableInt($r['requirement_id'] ?? null),
        $r['reported_date'], nullableDate($r['resolved_date'] ?? null),
    ]);
    insertRows($pdo, 'quality_check', [
        'id', 'project_id', 'wbs_id', 'item', 'result', 'inspector_id', 'inspection_date', 'notes',
    ], $state['quality'], static fn(array $r): array => [
        (int) $r['id'], $projectId, (int) $r['wbs_id'], $r['item'], $r['result'] ?? 'Pending',
        nullableInt($r['inspector_id'] ?? null), nullableDate($r['inspection_date'] ?? null), $r['notes'] ?? '',
    ]);
    insertRows($pdo, 'risk', [
        'id', 'project_id', 'code', 'description', 'likelihood', 'impact',
        'preventive_action', 'corrective_action', 'owner_id', 'status',
    ], $state['risks'], static fn(array $r): array => [
        (int) $r['id'], $projectId, $r['code'], $r['description'], (int) $r['likelihood'],
        (int) $r['impact'], $r['preventive_action'] ?? '', $r['corrective_action'] ?? '',
        nullableInt($r['owner_id'] ?? null), $r['status'] ?? 'Open',
    ]);
}
