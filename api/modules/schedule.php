<?php
declare(strict_types=1);

namespace CarePlan\Modules\Schedule;

use PDO;
use function CarePlan\Modules\insertRows;
use function CarePlan\Modules\nullableInt;
use function CarePlan\Modules\nullableDate;
use function CarePlan\Modules\readModule;

function load(PDO $pdo, int $projectId): array
{
    $activities = readModule($pdo, 'activity', '`id`, `code`, `name`, `wbs_id`, `assignee_id`, `requirement_id`, `duration_days`, `start_date`, `end_date`, `baseline_start`, `baseline_end`, `percent_complete`, `planned_cost`, `actual_cost`, `status`', $projectId, ['id', 'wbs_id', 'assignee_id', 'requirement_id', 'duration_days', 'percent_complete'], ['planned_cost', 'actual_cost']);
    $statement = $pdo->prepare('SELECT d.`activity_id`, d.`predecessor_id` FROM `activity_dependency` d INNER JOIN `activity` a ON a.`id` = d.`activity_id` WHERE a.`project_id` = ? ORDER BY d.`activity_id`, d.`predecessor_id`');
    $statement->execute([$projectId]);
    $dependencies = [];
    foreach ($statement->fetchAll() as $link) {
        $dependencies[(int) $link['activity_id']][] = (int) $link['predecessor_id'];
    }
    foreach ($activities as &$activity) {
        $activity['predecessors'] = $dependencies[$activity['id']] ?? [];
    }
    unset($activity);
    return ['activities' => $activities];
}

function clear(PDO $pdo, int $projectId): void
{
    $statement = $pdo->prepare('DELETE d FROM `activity_dependency` d INNER JOIN `activity` a ON a.`id` = d.`activity_id` WHERE a.`project_id` = ?');
    $statement->execute([$projectId]);
    $statement = $pdo->prepare('DELETE FROM `activity` WHERE `project_id` = ?');
    $statement->execute([$projectId]);
}

function save(PDO $pdo, int $projectId, array $records, string $projectStart): void
{
    insertRows($pdo, 'activity', [
        'id', 'project_id', 'code', 'name', 'wbs_id', 'assignee_id', 'requirement_id',
        'duration_days', 'start_date', 'end_date', 'baseline_start', 'baseline_end',
        'percent_complete', 'planned_cost', 'actual_cost', 'status',
    ], $records, static function (array $r) use ($projectId, $projectStart): array {
        $start = $r['start_date'] ?: ($r['baseline_start'] ?: $projectStart);
        $end = $r['end_date'] ?: ($r['baseline_end'] ?: $start);
        return [
            (int) $r['id'], $projectId, $r['code'], $r['name'], nullableInt($r['wbs_id'] ?? null),
            nullableInt($r['assignee_id'] ?? null), nullableInt($r['requirement_id'] ?? null),
            max(1, (int) ($r['duration_days'] ?? 1)), $start, $end,
            $r['baseline_start'] ?: $start, $r['baseline_end'] ?: $end,
            (int) ($r['percent_complete'] ?? 0), (float) ($r['planned_cost'] ?? 0),
            (float) ($r['actual_cost'] ?? 0), $r['status'] ?? 'Not Started',
        ];
    });

    $links = [];
    foreach ($records as $activity) {
        foreach ($activity['predecessors'] ?? [] as $predecessorId) {
            if ((int) $predecessorId === (int) $activity['id']) {
                throw new \InvalidArgumentException('An activity cannot depend on itself.');
            }
            $links[] = [(int) $activity['id'], (int) $predecessorId];
        }
    }
    insertRows($pdo, 'activity_dependency', ['activity_id', 'predecessor_id'], $links, static fn(array $r): array => $r);
}
