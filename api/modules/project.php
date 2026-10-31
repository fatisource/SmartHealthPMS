<?php
declare(strict_types=1);

namespace CarePlan\Modules\Project;

use PDO;
use function CarePlan\Modules\nullableDate;

function load(PDO $pdo): ?array
{
    $project = $pdo->query('SELECT * FROM `project` ORDER BY `id` LIMIT 1')->fetch();
    if (!$project) {
        return null;
    }
    $project['id'] = (int) $project['id'];
    foreach (['approved_budget', 'cost_threshold', 'schedule_threshold'] as $field) {
        $project[$field] = (float) $project[$field];
    }
    $project['objectives'] = $project['objectives'] ?? '';
    $project['scope_boundaries'] = $project['scope_boundaries'] ?? '';
    $project['closure_notes'] = $project['closure_notes'] ?? '';

    $statement = $pdo->prepare('SELECT `scope_summary`, `schedule_summary`, `cost_summary`, `quality_summary`, `version`, `last_updated` FROM `project_plan` WHERE `project_id` = ?');
    $statement->execute([$project['id']]);
    $plan = $statement->fetch();
    if (!$plan) {
        throw new \RuntimeException('The project plan is missing from the database.');
    }
    $plan['version'] = (int) $plan['version'];
    foreach (['scope_summary', 'schedule_summary', 'cost_summary', 'quality_summary'] as $field) {
        $plan[$field] = $plan[$field] ?? '';
    }
    return ['project' => $project, 'plan' => $plan];
}

function save(PDO $pdo, array $project, array $plan): void
{
    $projectId = (int) ($project['id'] ?? 1);
    if ($projectId < 1) {
        throw new \InvalidArgumentException('Project ID must be a positive integer.');
    }
    $exists = $pdo->prepare('SELECT 1 FROM `project` WHERE `id` = ?');
    $exists->execute([$projectId]);
    $values = [
        $project['name'], $project['objectives'] ?? '', $project['scope_boundaries'] ?? '',
        (float) ($project['approved_budget'] ?? 0), $project['start_date'], $project['deadline'],
        $project['status'] ?? 'Initiated', $project['status_date'] ?? $project['start_date'],
        (float) ($project['cost_threshold'] ?? 0.9), (float) ($project['schedule_threshold'] ?? 0.9),
        nullableDate($project['closure_date'] ?? null), $project['closure_notes'] ?? '',
    ];
    if ($exists->fetchColumn()) {
        $statement = $pdo->prepare('UPDATE `project` SET `name` = ?, `objectives` = ?, `scope_boundaries` = ?, `approved_budget` = ?, `start_date` = ?, `deadline` = ?, `status` = ?, `status_date` = ?, `cost_threshold` = ?, `schedule_threshold` = ?, `closure_date` = ?, `closure_notes` = ? WHERE `id` = ?');
        $statement->execute([...$values, $projectId]);
    } else {
        $statement = $pdo->prepare('INSERT INTO `project` (`id`, `name`, `objectives`, `scope_boundaries`, `approved_budget`, `start_date`, `deadline`, `status`, `status_date`, `cost_threshold`, `schedule_threshold`, `closure_date`, `closure_notes`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)');
        $statement->execute([$projectId, ...$values]);
    }

    $statement = $pdo->prepare('INSERT INTO `project_plan` (`project_id`, `scope_summary`, `schedule_summary`, `cost_summary`, `quality_summary`, `version`, `last_updated`) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `scope_summary` = VALUES(`scope_summary`), `schedule_summary` = VALUES(`schedule_summary`), `cost_summary` = VALUES(`cost_summary`), `quality_summary` = VALUES(`quality_summary`), `version` = VALUES(`version`), `last_updated` = VALUES(`last_updated`)');
    $statement->execute([
        $projectId, $plan['scope_summary'] ?? '', $plan['schedule_summary'] ?? '',
        $plan['cost_summary'] ?? '', $plan['quality_summary'] ?? '',
        max(1, (int) ($plan['version'] ?? 1)), $plan['last_updated'] ?? $project['start_date'],
    ]);
}
