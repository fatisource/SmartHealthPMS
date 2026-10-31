-- Smart Healthcare Project Management System
-- Import into database: spms_db (XAMPP MySQL / MariaDB)
-- Safe to re-import: it drops and recreates all tables.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS risk;
DROP TABLE IF EXISTS quality_check;
DROP TABLE IF EXISTS defect;
DROP TABLE IF EXISTS milestone;
DROP TABLE IF EXISTS activity_dependency;
DROP TABLE IF EXISTS activity;
DROP TABLE IF EXISTS change_request;
DROP TABLE IF EXISTS wbs_item;
DROP TABLE IF EXISTS requirement;
DROP TABLE IF EXISTS project_plan;
DROP TABLE IF EXISTS team_member;
DROP TABLE IF EXISTS stakeholder;
DROP TABLE IF EXISTS project;

-- ======================= SCHEMA =======================

CREATE TABLE project (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    objectives TEXT,
    scope_boundaries TEXT,
    approved_budget DECIMAL(14,2) NOT NULL,
    start_date DATE NOT NULL,
    deadline DATE NOT NULL,
    status ENUM('Initiated','Planning','Executing','Monitoring','Closed') NOT NULL DEFAULT 'Initiated',
    status_date DATE NOT NULL,
    cost_threshold DECIMAL(4,2) NOT NULL DEFAULT 0.90,
    schedule_threshold DECIMAL(4,2) NOT NULL DEFAULT 0.90,
    closure_date DATE NULL,
    closure_notes TEXT,
    CHECK (approved_budget > 0),
    CHECK (deadline > start_date)
) ENGINE=InnoDB;

CREATE TABLE stakeholder (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL,
    interest VARCHAR(255),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE team_member (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE project_plan (
    project_id INT PRIMARY KEY,
    scope_summary TEXT,
    schedule_summary TEXT,
    cost_summary TEXT,
    quality_summary TEXT,
    version INT NOT NULL DEFAULT 1,
    last_updated DATE NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE requirement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    code VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    priority ENUM('Critical','High','Medium','Low') NOT NULL,
    status ENUM('Proposed','Approved','Rejected') NOT NULL DEFAULT 'Proposed',
    UNIQUE (project_id, code),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE wbs_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    parent_id INT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    item_type ENUM('Deliverable','Work Package') NOT NULL,
    requirement_id INT NULL,
    UNIQUE (project_id, code),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES wbs_item(id) ON DELETE CASCADE,
    FOREIGN KEY (requirement_id) REFERENCES requirement(id)
) ENGINE=InnoDB;

CREATE TABLE change_request (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    code VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    justification TEXT NOT NULL,
    requirement_id INT NULL,
    cost_impact DECIMAL(14,2) NOT NULL DEFAULT 0,
    schedule_impact_days INT NOT NULL DEFAULT 0,
    status ENUM('Pending','Approved','Rejected') NOT NULL DEFAULT 'Pending',
    requested_date DATE NOT NULL,
    decision_date DATE NULL,
    applied TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE (project_id, code),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (requirement_id) REFERENCES requirement(id)
) ENGINE=InnoDB;

CREATE TABLE activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    wbs_id INT NULL,
    assignee_id INT NULL,
    requirement_id INT NULL,
    duration_days INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    baseline_start DATE NOT NULL,
    baseline_end DATE NOT NULL,
    percent_complete INT NOT NULL DEFAULT 0,
    planned_cost DECIMAL(14,2) NOT NULL DEFAULT 0,
    actual_cost DECIMAL(14,2) NOT NULL DEFAULT 0,
    status ENUM('Not Started','In Progress','Completed') NOT NULL DEFAULT 'Not Started',
    UNIQUE (project_id, code),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (wbs_id) REFERENCES wbs_item(id),
    FOREIGN KEY (assignee_id) REFERENCES team_member(id),
    FOREIGN KEY (requirement_id) REFERENCES requirement(id),
    CHECK (duration_days > 0),
    CHECK (end_date >= start_date),
    CHECK (percent_complete BETWEEN 0 AND 100),
    CHECK (planned_cost >= 0),
    CHECK (actual_cost >= 0)
) ENGINE=InnoDB;

CREATE TABLE activity_dependency (
    activity_id INT NOT NULL,
    predecessor_id INT NOT NULL,
    PRIMARY KEY (activity_id, predecessor_id),
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
    FOREIGN KEY (predecessor_id) REFERENCES activity(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE milestone (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    target_date DATE NOT NULL,
    status ENUM('Pending','Achieved','Missed') NOT NULL DEFAULT 'Pending',
    responsible_id INT NULL,
    activity_id INT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (responsible_id) REFERENCES team_member(id),
    FOREIGN KEY (activity_id) REFERENCES activity(id)
) ENGINE=InnoDB;

CREATE TABLE defect (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    code VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    severity ENUM('Critical','High','Medium','Low') NOT NULL,
    status ENUM('Open','In Progress','Resolved','Closed') NOT NULL DEFAULT 'Open',
    assigned_to INT NULL,
    activity_id INT NULL,
    requirement_id INT NULL,
    reported_date DATE NOT NULL,
    resolved_date DATE NULL,
    UNIQUE (project_id, code),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES team_member(id),
    FOREIGN KEY (activity_id) REFERENCES activity(id),
    FOREIGN KEY (requirement_id) REFERENCES requirement(id)
) ENGINE=InnoDB;

CREATE TABLE quality_check (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    wbs_id INT NOT NULL,
    item VARCHAR(255) NOT NULL,
    result ENUM('Pending','Pass','Fail') NOT NULL DEFAULT 'Pending',
    inspector_id INT NULL,
    inspection_date DATE NULL,
    notes VARCHAR(255),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (wbs_id) REFERENCES wbs_item(id),
    FOREIGN KEY (inspector_id) REFERENCES team_member(id)
) ENGINE=InnoDB;

CREATE TABLE risk (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    code VARCHAR(20) NOT NULL,
    description VARCHAR(255) NOT NULL,
    likelihood INT NOT NULL,
    impact INT NOT NULL,
    preventive_action VARCHAR(255),
    corrective_action VARCHAR(255),
    owner_id INT NULL,
    status ENUM('Open','Mitigated','Occurred','Closed') NOT NULL DEFAULT 'Open',
    UNIQUE (project_id, code),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES team_member(id),
    CHECK (likelihood BETWEEN 1 AND 5),
    CHECK (impact BETWEEN 1 AND 5)
) ENGINE=InnoDB;

-- ===================== SAMPLE DATA =====================

INSERT INTO project (id, name, objectives, scope_boundaries, approved_budget, start_date, deadline, status, status_date, cost_threshold, schedule_threshold) VALUES
(1, 'Smart Healthcare Project Management System',
 'Deliver an integrated system for five hospitals that manages patient appointments, laboratory reports, doctor schedules, online consultations and hospital billing within 16 weeks and PKR 5,000,000.',
 'In scope: appointments, lab reports, doctor schedules, online consultations, billing, patient records. Out of scope until approved by change request: AI-based diagnosis module and mobile application.',
 5000000, '2026-08-31', '2026-12-21', 'Executing', '2026-10-26', 0.90, 0.90);

INSERT INTO stakeholder (id, project_id, name, role, interest) VALUES
(1, 1, 'Hospital Network Management', 'Sponsor', 'Delivery on time and within budget'),
(2, 1, 'Doctors', 'End User', 'Fast schedule and consultation tools'),
(3, 1, 'Patients', 'End User', 'Easy appointments and report access'),
(4, 1, 'Laboratory Staff', 'End User', 'Accurate lab report handling'),
(5, 1, 'Finance Department', 'Stakeholder', 'Correct billing and budget control'),
(6, 1, 'Ayesha Khan', 'Project Manager', 'Overall project success');

INSERT INTO team_member (id, project_id, name, role) VALUES
(1, 1, 'Ayesha Khan', 'Project Manager'),
(2, 1, 'Hassan Raza', 'Backend Developer'),
(3, 1, 'Usman Tariq', 'Backend Developer'),
(4, 1, 'Sana Malik', 'Frontend Developer'),
(5, 1, 'Bilal Ahmed', 'Frontend Developer'),
(6, 1, 'Zainab Ali', 'Database Developer'),
(7, 1, 'Omar Farooq', 'QA Engineer');

INSERT INTO project_plan (project_id, scope_summary, schedule_summary, cost_summary, quality_summary, version, last_updated) VALUES
(1,
 'Six core capabilities across five hospitals: appointments, patient records, lab reports, doctor schedules, online consultations and billing.',
 '16-week schedule from 2026-08-31 to 2026-12-21 with six milestones from requirements baseline to go-live.',
 'Approved budget PKR 5,000,000. Activity cost baseline PKR 4,600,000 with PKR 400,000 reserve.',
 'Every deliverable is inspected against a checklist. Critical defects must be resolved before UAT.',
 1, '2026-08-31');

INSERT INTO requirement (id, project_id, code, title, description, priority, status) VALUES
(1, 1, 'REQ-01', 'Patient appointment scheduling', 'Book, reschedule and cancel appointments across five hospitals', 'Critical', 'Approved'),
(2, 1, 'REQ-02', 'Laboratory reports', 'Upload, store and view lab reports linked to patients', 'High', 'Approved'),
(3, 1, 'REQ-03', 'Doctor schedules', 'Manage doctor availability and duty rosters', 'High', 'Approved'),
(4, 1, 'REQ-04', 'Online consultations', 'Video or chat consultation between doctor and patient', 'High', 'Approved'),
(5, 1, 'REQ-05', 'Hospital billing', 'Generate bills and record payments', 'High', 'Approved'),
(6, 1, 'REQ-06', 'Patient record management', 'Maintain complete and consistent patient records', 'Critical', 'Approved'),
(7, 1, 'REQ-07', 'AI-based preliminary diagnosis', 'AI module that suggests a preliminary diagnosis from symptoms', 'High', 'Proposed'),
(8, 1, 'REQ-08', 'Mobile application', 'Mobile app for patients to book appointments and view reports', 'High', 'Proposed');

INSERT INTO wbs_item (id, project_id, parent_id, code, name, item_type, requirement_id) VALUES
(1, 1, NULL, '1', 'Planning and Design', 'Deliverable', NULL),
(2, 1, NULL, '2', 'Core Modules', 'Deliverable', NULL),
(3, 1, NULL, '3', 'User Interfaces', 'Deliverable', NULL),
(4, 1, NULL, '4', 'Testing and Deployment', 'Deliverable', NULL),
(5, 1, 1, '1.1', 'Requirements Document', 'Work Package', NULL),
(6, 1, 1, '1.2', 'Architecture and Database Design', 'Work Package', NULL),
(7, 1, 2, '2.1', 'Appointment Management', 'Work Package', 1),
(8, 1, 2, '2.2', 'Patient Record Management', 'Work Package', 6),
(9, 1, 2, '2.3', 'Lab Reports and Billing', 'Work Package', 2),
(10, 1, 2, '2.4', 'Doctor Schedule and Online Consultation', 'Work Package', 3),
(11, 1, 3, '3.1', 'Appointment and Patient UI', 'Work Package', 1),
(12, 1, 3, '3.2', 'Lab, Billing and Consultation UI', 'Work Package', 4),
(13, 1, 4, '4.1', 'Integration and System Testing', 'Work Package', NULL),
(14, 1, 4, '4.2', 'Defect Fixing and Stabilization', 'Work Package', NULL),
(15, 1, 4, '4.3', 'User Acceptance Testing and Deployment', 'Work Package', NULL);

INSERT INTO change_request (id, project_id, code, title, justification, requirement_id, cost_impact, schedule_impact_days, status, requested_date) VALUES
(1, 1, 'CR-01', 'Add AI-based preliminary diagnosis module', 'Hospital management wants AI support to speed up first-level patient triage.', 7, 450000, 14, 'Pending', '2026-10-05'),
(2, 1, 'CR-02', 'Add patient mobile application', 'Hospital management wants patients to access services from their phones.', 8, 350000, 7, 'Pending', '2026-10-12');

INSERT INTO activity (id, project_id, code, name, wbs_id, assignee_id, requirement_id, duration_days, start_date, end_date, baseline_start, baseline_end, percent_complete, planned_cost, actual_cost, status) VALUES
(1, 1, 'A01', 'Requirements Gathering', 5, 1, NULL, 10, '2026-08-31', '2026-09-09', '2026-08-31', '2026-09-09', 100, 250000, 260000, 'Completed'),
(2, 1, 'A02', 'System Architecture and DB Design', 6, 6, NULL, 7, '2026-09-10', '2026-09-16', '2026-09-10', '2026-09-16', 100, 300000, 310000, 'Completed'),
(3, 1, 'A03', 'Appointment Module Backend', 7, 2, 1, 21, '2026-09-17', '2026-10-07', '2026-09-17', '2026-10-07', 100, 600000, 680000, 'Completed'),
(4, 1, 'A04', 'Patient Records Module Backend', 8, 3, 6, 21, '2026-09-17', '2026-10-07', '2026-09-17', '2026-10-07', 100, 600000, 640000, 'Completed'),
(5, 1, 'A05', 'Lab Reports and Billing Backend', 9, 2, 2, 21, '2026-10-08', '2026-10-28', '2026-10-08', '2026-10-28', 60, 550000, 380000, 'In Progress'),
(6, 1, 'A06', 'Doctor Schedule and Consultation Backend', 10, 3, 3, 14, '2026-10-08', '2026-10-21', '2026-10-08', '2026-10-21', 80, 450000, 400000, 'In Progress'),
(7, 1, 'A07', 'Appointment and Patient UI', 11, 4, 1, 21, '2026-10-08', '2026-10-28', '2026-10-08', '2026-10-28', 70, 500000, 360000, 'In Progress'),
(8, 1, 'A08', 'Lab, Billing and Consultation UI', 12, 5, 4, 21, '2026-10-22', '2026-11-11', '2026-10-22', '2026-11-11', 20, 450000, 120000, 'In Progress'),
(9, 1, 'A09', 'Integration and System Testing', 13, 7, NULL, 14, '2026-11-12', '2026-11-25', '2026-11-12', '2026-11-25', 0, 350000, 0, 'Not Started'),
(10, 1, 'A10', 'Defect Fixing and Stabilization', 14, 2, NULL, 10, '2026-11-26', '2026-12-05', '2026-11-26', '2026-12-05', 0, 250000, 0, 'Not Started'),
(11, 1, 'A11', 'UAT and Deployment', 15, 1, NULL, 10, '2026-12-06', '2026-12-15', '2026-12-06', '2026-12-15', 0, 300000, 0, 'Not Started');

INSERT INTO activity_dependency (activity_id, predecessor_id) VALUES
(2, 1), (3, 2), (4, 2), (5, 3), (6, 4), (7, 3), (7, 4), (8, 6), (9, 5), (9, 7), (9, 8), (10, 9), (11, 10);

INSERT INTO milestone (id, project_id, name, target_date, status, responsible_id, activity_id) VALUES
(1, 1, 'Requirements baseline approved', '2026-09-09', 'Achieved', 1, 1),
(2, 1, 'Architecture and database design complete', '2026-09-16', 'Achieved', 6, 2),
(3, 1, 'Core backend modules complete', '2026-10-28', 'Pending', 2, 5),
(4, 1, 'User interfaces complete', '2026-11-11', 'Pending', 5, 8),
(5, 1, 'System testing complete', '2026-11-25', 'Pending', 7, 9),
(6, 1, 'Go-live and deployment', '2026-12-15', 'Pending', 1, 11);

INSERT INTO defect (id, project_id, code, title, severity, status, assigned_to, activity_id, requirement_id, reported_date, resolved_date) VALUES
(1, 1, 'D-01', 'Appointment double booking for the same doctor slot', 'Critical', 'Open', 2, 3, 1, '2026-10-12', NULL),
(2, 1, 'D-02', 'Patient record overwritten when two users save together', 'Critical', 'In Progress', 3, 4, 6, '2026-10-13', NULL),
(3, 1, 'D-03', 'Billing total rounding error', 'Medium', 'Open', 2, 5, 5, '2026-10-20', NULL),
(4, 1, 'D-04', 'Button misalignment on appointment screen', 'Low', 'Resolved', 4, 7, 1, '2026-10-15', '2026-10-20');

INSERT INTO quality_check (id, project_id, wbs_id, item, result, inspector_id, inspection_date, notes) VALUES
(1, 1, 5, 'Requirements document reviewed and signed off', 'Pass', 7, '2026-09-10', 'Approved by sponsor'),
(2, 1, 6, 'Database schema normalised and peer reviewed', 'Pass', 7, '2026-09-17', 'No issues found'),
(3, 1, 7, 'Appointment module passes functional tests', 'Fail', 7, '2026-10-12', 'Double booking found'),
(4, 1, 8, 'Patient record module passes functional tests', 'Fail', 7, '2026-10-13', 'Concurrent save overwrites data'),
(5, 1, 11, 'User interface reviewed against mockups', 'Pending', NULL, NULL, NULL);

INSERT INTO risk (id, project_id, code, description, likelihood, impact, preventive_action, corrective_action, owner_id, status) VALUES
(1, 1, 'R-01', 'Scope creep from hospital management requests', 5, 4, 'Enforce formal change request process', 'Re-baseline scope, cost and schedule after approval', 1, 'Occurred'),
(2, 1, 'R-02', 'AI diagnosis module is technically complex to integrate', 4, 4, 'Build a prototype early and use a proven AI service', 'Reduce module scope to a basic symptom checker', 2, 'Open'),
(3, 1, 'R-03', 'Key developer becomes unavailable', 2, 4, 'Cross-train backend developers', 'Reassign tasks and add temporary resource', 1, 'Open'),
(4, 1, 'R-04', 'Data inconsistency across the five hospitals', 3, 5, 'Define a common data standard and validation rules', 'Run data cleanup scripts and re-sync', 6, 'Open'),
(5, 1, 'R-05', 'Budget overrun because of defects and rework', 4, 3, 'Track EVM weekly and fix defects early', 'Request budget revision through change control', 1, 'Open');

SET FOREIGN_KEY_CHECKS = 1;
