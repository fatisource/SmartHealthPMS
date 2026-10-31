<?php
declare(strict_types=1);

namespace CarePlan\Modules\Scope;

use PDO;
use function CarePlan\Modules\insertRows;
use function CarePlan\Modules\nullableInt;
use function CarePlan\Modules\nullableDate;
use function CarePlan\Modules\orderWbsItems;
use function CarePlan\Modules\readModule;

function load(PDO $pdo, int $projectId): array
{
    return [
        'requirements' => readModule($pdo, 'requirement', '`id`, `code`, `title`, `description`, `priority`, `status`', $projectId, ['id']),
        'wbs' => readModule($pdo, 'wbs_item', '`id`, `parent_id`, `code`, `name`, `item_type`, `requirement_id`', $projectId, ['id', 'parent_id', 'requirement_id']),
        'changes' => readModule($pdo, 'change_request', '`id`, `code`, `title`, `justification`, `requirement_id`, `cost_impact`, `schedule_impact_days`, `status`, `requested_date`, `decision_date`, `applied`', $projectId, ['id', 'requirement_id', 'schedule_impact_days'], ['cost_impact'], ['applied']),
    ];
}

function clear(PDO $pdo, int $projectId): void
{
    foreach (['change_request', 'wbs_item', 'requirement'] as $table) {
        $statement = $pdo->prepare("DELETE FROM `$table` WHERE `project_id` = ?");
        $statement->execute([$projectId]);
    }
}

function save(PDO $pdo, int $projectId, array $state): void
{
    insertRows($pdo, 'requirement', ['id', 'project_id', 'code', 'title', 'description', 'priority', 'status'], $state['requirements'], static fn(array $r): array => [
        (int) $r['id'], $projectId, $r['code'], $r['title'], $r['description'] ?? '',
        $r['priority'] ?? 'Medium', $r['status'] ?? 'Proposed',
    ]);
    insertRows($pdo, 'wbs_item', ['id', 'project_id', 'parent_id', 'code', 'name', 'item_type', 'requirement_id'], orderWbsItems($state['wbs']), static fn(array $r): array => [
        (int) $r['id'], $projectId, nullableInt($r['parent_id'] ?? null), $r['code'], $r['name'],
        $r['item_type'] ?? 'Work Package', nullableInt($r['requirement_id'] ?? null),
    ]);
    insertRows($pdo, 'change_request', [
        'id', 'project_id', 'code', 'title', 'justification', 'requirement_id',
        'cost_impact', 'schedule_impact_days', 'status', 'requested_date', 'decision_date', 'applied',
    ], $state['changes'], static fn(array $r): array => [
        (int) $r['id'], $projectId, $r['code'], $r['title'], $r['justification'] ?? '',
        nullableInt($r['requirement_id'] ?? null), (float) ($r['cost_impact'] ?? 0),
        max(0, (int) ($r['schedule_impact_days'] ?? 0)), $r['status'] ?? 'Pending',
        $r['requested_date'], nullableDate($r['decision_date'] ?? null), !empty($r['applied']) ? 1 : 0,
    ]);
}
