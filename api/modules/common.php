<?php
declare(strict_types=1);

namespace CarePlan\Modules;

use PDO;
use InvalidArgumentException;

function nullableInt(mixed $value): ?int
{
    return $value === null || $value === '' ? null : (int) $value;
}

function nullableDate(mixed $value): ?string
{
    return $value === null || $value === '' ? null : (string) $value;
}

function readModule(PDO $pdo, string $table, string $columns, int $projectId, array $integers = [], array $decimals = [], array $booleans = []): array
{
    $statement = $pdo->prepare("SELECT $columns FROM `$table` WHERE `project_id` = ? ORDER BY `id`");
    $statement->execute([$projectId]);
    $rows = $statement->fetchAll();
    foreach ($rows as &$row) {
        foreach ($integers as $column) {
            $row[$column] = $row[$column] === null ? null : (int) $row[$column];
        }
        foreach ($decimals as $column) {
            $row[$column] = (float) $row[$column];
        }
        foreach ($booleans as $column) {
            $row[$column] = (bool) $row[$column];
        }
    }
    unset($row);
    return $rows;
}

function insertRows(PDO $pdo, string $table, array $columns, array $records, callable $map): void
{
    if (!$records) {
        return;
    }
    $columnSql = implode(', ', array_map(static fn(string $column): string => "`$column`", $columns));
    $placeholders = implode(', ', array_fill(0, count($columns), '?'));
    $statement = $pdo->prepare("INSERT INTO `$table` ($columnSql) VALUES ($placeholders)");
    foreach ($records as $record) {
        $statement->execute($map($record));
    }
}

function orderWbsItems(array $items): array
{
    $remaining = array_values($items);
    $ordered = [];
    $availableIds = [];
    while ($remaining) {
        $progress = false;
        foreach ($remaining as $index => $item) {
            $parentId = nullableInt($item['parent_id'] ?? null);
            if ($parentId === null || isset($availableIds[$parentId])) {
                $ordered[] = $item;
                $availableIds[(int) $item['id']] = true;
                array_splice($remaining, $index, 1);
                $progress = true;
                break;
            }
        }
        if (!$progress) {
            throw new InvalidArgumentException('WBS parent links contain a missing item or a cycle.');
        }
    }
    return $ordered;
}

function validateWorkspace(mixed $state): void
{
    $arrays = ['stakeholders', 'team', 'requirements', 'wbs', 'changes', 'activities', 'milestones', 'defects', 'quality', 'risks'];
    if (!is_array($state) || ($state['schemaVersion'] ?? null) !== 1
        || !is_array($state['project'] ?? null) || !is_array($state['plan'] ?? null)) {
        throw new InvalidArgumentException('The submitted workspace format is invalid.');
    }
    foreach ($arrays as $key) {
        if (!isset($state[$key]) || !is_array($state[$key])) {
            throw new InvalidArgumentException("The $key module data is missing or invalid.");
        }
    }
    if (trim((string) ($state['project']['name'] ?? '')) === '') {
        throw new InvalidArgumentException('Project name is required.');
    }
    $approvedRequirements = [];
    foreach ($state['requirements'] as $requirement) {
        if (($requirement['status'] ?? '') === 'Approved') {
            $approvedRequirements[(int) ($requirement['id'] ?? 0)] = true;
        }
    }
    foreach (array_merge($state['wbs'], $state['activities']) as $work) {
        $requirementId = nullableInt($work['requirement_id'] ?? null);
        if ($requirementId !== null && !isset($approvedRequirements[$requirementId])) {
            throw new InvalidArgumentException('WBS items and activities may only reference approved requirements.');
        }
    }
}
