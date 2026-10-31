CREATE DATABASE IF NOT EXISTS `spms_db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
USE `spms_db`;

CREATE TABLE IF NOT EXISTS `project` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NOT NULL,
  `objectives` TEXT NULL,
  `scope_boundaries` TEXT NULL,
  `approved_budget` DECIMAL(14,2) NOT NULL,
  `start_date` DATE NOT NULL,
  `deadline` DATE NOT NULL,
  `status` ENUM('Initiated','Planning','Executing','Monitoring','Closed') NOT NULL DEFAULT 'Initiated',
  `status_date` DATE NOT NULL,
  `cost_threshold` DECIMAL(4,2) NOT NULL DEFAULT 0.90,
  `schedule_threshold` DECIMAL(4,2) NOT NULL DEFAULT 0.90,
  `closure_date` DATE NULL,
  `closure_notes` TEXT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `project_plan` (
  `project_id` INT NOT NULL,
  `scope_summary` TEXT NULL,
  `schedule_summary` TEXT NULL,
  `cost_summary` TEXT NULL,
  `quality_summary` TEXT NULL,
  `version` INT NOT NULL DEFAULT 1,
  `last_updated` DATE NULL,
  PRIMARY KEY (`project_id`),
  CONSTRAINT `project_plan_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `stakeholder` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `role` VARCHAR(100) NOT NULL,
  `interest` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  KEY `stakeholder_project_idx` (`project_id`),
  CONSTRAINT `stakeholder_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `team_member` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `role` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `team_member_project_idx` (`project_id`),
  CONSTRAINT `team_member_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `requirement` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `code` VARCHAR(20) NOT NULL,
  `title` VARCHAR(150) NOT NULL,
  `description` TEXT NULL,
  `priority` ENUM('Critical','High','Medium','Low') NOT NULL,
  `status` ENUM('Proposed','Approved','Rejected') NOT NULL DEFAULT 'Proposed',
  PRIMARY KEY (`id`),
  UNIQUE KEY `requirement_project_code_uq` (`project_id`,`code`),
  CONSTRAINT `requirement_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `wbs_item` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `parent_id` INT NULL,
  `code` VARCHAR(20) NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `item_type` ENUM('Deliverable','Work Package') NOT NULL,
  `requirement_id` INT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `wbs_project_code_uq` (`project_id`,`code`),
  KEY `wbs_parent_idx` (`parent_id`),
  KEY `wbs_requirement_idx` (`requirement_id`),
  CONSTRAINT `wbs_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `wbs_parent_fk` FOREIGN KEY (`parent_id`) REFERENCES `wbs_item` (`id`) ON DELETE CASCADE,
  CONSTRAINT `wbs_requirement_fk` FOREIGN KEY (`requirement_id`) REFERENCES `requirement` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `activity` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `code` VARCHAR(20) NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `wbs_id` INT NULL,
  `assignee_id` INT NULL,
  `requirement_id` INT NULL,
  `duration_days` INT NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `baseline_start` DATE NOT NULL,
  `baseline_end` DATE NOT NULL,
  `percent_complete` INT NOT NULL DEFAULT 0,
  `planned_cost` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  `actual_cost` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  `status` ENUM('Not Started','In Progress','Completed') NOT NULL DEFAULT 'Not Started',
  PRIMARY KEY (`id`),
  UNIQUE KEY `activity_project_code_uq` (`project_id`,`code`),
  KEY `activity_wbs_idx` (`wbs_id`),
  KEY `activity_assignee_idx` (`assignee_id`),
  KEY `activity_requirement_idx` (`requirement_id`),
  CONSTRAINT `activity_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `activity_wbs_fk` FOREIGN KEY (`wbs_id`) REFERENCES `wbs_item` (`id`),
  CONSTRAINT `activity_assignee_fk` FOREIGN KEY (`assignee_id`) REFERENCES `team_member` (`id`),
  CONSTRAINT `activity_requirement_fk` FOREIGN KEY (`requirement_id`) REFERENCES `requirement` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `activity_dependency` (
  `activity_id` INT NOT NULL,
  `predecessor_id` INT NOT NULL,
  PRIMARY KEY (`activity_id`,`predecessor_id`),
  KEY `activity_dependency_predecessor_idx` (`predecessor_id`),
  CONSTRAINT `activity_dependency_activity_fk` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE,
  CONSTRAINT `activity_dependency_predecessor_fk` FOREIGN KEY (`predecessor_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `change_request` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `code` VARCHAR(20) NOT NULL,
  `title` VARCHAR(150) NOT NULL,
  `justification` TEXT NOT NULL,
  `requirement_id` INT NULL,
  `cost_impact` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  `schedule_impact_days` INT NOT NULL DEFAULT 0,
  `status` ENUM('Pending','Approved','Rejected') NOT NULL DEFAULT 'Pending',
  `requested_date` DATE NOT NULL,
  `decision_date` DATE NULL,
  `applied` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `change_request_project_code_uq` (`project_id`,`code`),
  KEY `change_request_requirement_idx` (`requirement_id`),
  CONSTRAINT `change_request_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `change_request_requirement_fk` FOREIGN KEY (`requirement_id`) REFERENCES `requirement` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `milestone` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `target_date` DATE NOT NULL,
  `status` ENUM('Pending','Achieved','Missed') NOT NULL DEFAULT 'Pending',
  `responsible_id` INT NULL,
  `activity_id` INT NULL,
  PRIMARY KEY (`id`),
  KEY `milestone_project_idx` (`project_id`),
  KEY `milestone_owner_idx` (`responsible_id`),
  KEY `milestone_activity_idx` (`activity_id`),
  CONSTRAINT `milestone_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `milestone_owner_fk` FOREIGN KEY (`responsible_id`) REFERENCES `team_member` (`id`),
  CONSTRAINT `milestone_activity_fk` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `defect` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `code` VARCHAR(20) NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `severity` ENUM('Critical','High','Medium','Low') NOT NULL,
  `status` ENUM('Open','In Progress','Resolved','Closed') NOT NULL DEFAULT 'Open',
  `assigned_to` INT NULL,
  `activity_id` INT NULL,
  `requirement_id` INT NULL,
  `reported_date` DATE NOT NULL,
  `resolved_date` DATE NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `defect_project_code_uq` (`project_id`,`code`),
  KEY `defect_assignee_idx` (`assigned_to`),
  KEY `defect_activity_idx` (`activity_id`),
  KEY `defect_requirement_idx` (`requirement_id`),
  CONSTRAINT `defect_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `defect_assignee_fk` FOREIGN KEY (`assigned_to`) REFERENCES `team_member` (`id`),
  CONSTRAINT `defect_activity_fk` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`),
  CONSTRAINT `defect_requirement_fk` FOREIGN KEY (`requirement_id`) REFERENCES `requirement` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `quality_check` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `wbs_id` INT NOT NULL,
  `item` VARCHAR(255) NOT NULL,
  `result` ENUM('Pending','Pass','Fail') NOT NULL DEFAULT 'Pending',
  `inspector_id` INT NULL,
  `inspection_date` DATE NULL,
  `notes` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  KEY `quality_project_idx` (`project_id`),
  KEY `quality_wbs_idx` (`wbs_id`),
  KEY `quality_inspector_idx` (`inspector_id`),
  CONSTRAINT `quality_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `quality_wbs_fk` FOREIGN KEY (`wbs_id`) REFERENCES `wbs_item` (`id`),
  CONSTRAINT `quality_inspector_fk` FOREIGN KEY (`inspector_id`) REFERENCES `team_member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `risk` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `code` VARCHAR(20) NOT NULL,
  `description` VARCHAR(255) NOT NULL,
  `likelihood` INT NOT NULL,
  `impact` INT NOT NULL,
  `preventive_action` VARCHAR(255) NULL,
  `corrective_action` VARCHAR(255) NULL,
  `owner_id` INT NULL,
  `status` ENUM('Open','Mitigated','Occurred','Closed') NOT NULL DEFAULT 'Open',
  PRIMARY KEY (`id`),
  UNIQUE KEY `risk_project_code_uq` (`project_id`,`code`),
  KEY `risk_owner_idx` (`owner_id`),
  CONSTRAINT `risk_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `risk_owner_fk` FOREIGN KEY (`owner_id`) REFERENCES `team_member` (`id`),
  CONSTRAINT `risk_likelihood_ck` CHECK (`likelihood` BETWEEN 1 AND 5),
  CONSTRAINT `risk_impact_ck` CHECK (`impact` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
