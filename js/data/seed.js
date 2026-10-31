export const seed = {
    schemaVersion: 1,
    project: {
      id: 1,
      name: "Smart Healthcare Project Management System",
      objectives: "Deliver an integrated system for five hospitals that manages patient appointments, laboratory reports, doctor schedules, online consultations and hospital billing within 16 weeks and PKR 5,000,000.",
      scope_boundaries: "In scope: appointments, lab reports, doctor schedules, online consultations, billing, patient records. Out of scope until approved by change request: AI-based diagnosis module and mobile application.",
      approved_budget: 5000000,
      start_date: "2026-08-31",
      deadline: "2026-12-21",
      status: "Executing",
      status_date: "2026-10-26",
      cost_threshold: 0.9,
      schedule_threshold: 0.9,
      closure_date: "",
      closure_notes: ""
    },
    plan: {
      scope_summary: "Six core capabilities across five hospitals: appointments, patient records, lab reports, doctor schedules, online consultations and billing.",
      schedule_summary: "16-week schedule from 2026-08-31 to 2026-12-21 with six milestones from requirements baseline to go-live.",
      cost_summary: "Approved budget PKR 5,000,000. Activity cost baseline PKR 4,600,000 with PKR 400,000 reserve.",
      quality_summary: "Every deliverable is inspected against a checklist. Critical defects must be resolved before UAT.",
      version: 1,
      last_updated: "2026-08-31"
    },
    stakeholders: [
      { id: 1, name: "Hospital Network Management", role: "Sponsor", interest: "Delivery on time and within budget" },
      { id: 2, name: "Doctors", role: "End User", interest: "Fast schedule and consultation tools" },
      { id: 3, name: "Patients", role: "End User", interest: "Easy appointments and report access" },
      { id: 4, name: "Laboratory Staff", role: "End User", interest: "Accurate lab report handling" },
      { id: 5, name: "Finance Department", role: "Stakeholder", interest: "Correct billing and budget control" },
      { id: 6, name: "Ayesha Khan", role: "Project Manager", interest: "Overall project success" }
    ],
    team: [
      { id: 1, name: "Ayesha Khan", role: "Project Manager" },
      { id: 2, name: "Hassan Raza", role: "Backend Developer" },
      { id: 3, name: "Usman Tariq", role: "Backend Developer" },
      { id: 4, name: "Sana Malik", role: "Frontend Developer" },
      { id: 5, name: "Bilal Ahmed", role: "Frontend Developer" },
      { id: 6, name: "Zainab Ali", role: "Database Developer" },
      { id: 7, name: "Omar Farooq", role: "QA Engineer" }
    ],
    requirements: [
      { id: 1, code: "REQ-01", title: "Patient appointment scheduling", description: "Book, reschedule and cancel appointments across five hospitals", priority: "Critical", status: "Approved" },
      { id: 2, code: "REQ-02", title: "Laboratory reports", description: "Upload, store and view lab reports linked to patients", priority: "High", status: "Approved" },
      { id: 3, code: "REQ-03", title: "Doctor schedules", description: "Manage doctor availability and duty rosters", priority: "High", status: "Approved" },
      { id: 4, code: "REQ-04", title: "Online consultations", description: "Video or chat consultation between doctor and patient", priority: "High", status: "Approved" },
      { id: 5, code: "REQ-05", title: "Hospital billing", description: "Generate bills and record payments", priority: "High", status: "Approved" },
      { id: 6, code: "REQ-06", title: "Patient record management", description: "Maintain complete and consistent patient records", priority: "Critical", status: "Approved" },
      { id: 7, code: "REQ-07", title: "AI-based preliminary diagnosis", description: "AI module that suggests a preliminary diagnosis from symptoms", priority: "High", status: "Proposed" },
      { id: 8, code: "REQ-08", title: "Mobile application", description: "Mobile app for patients to book appointments and view reports", priority: "High", status: "Proposed" }
    ],
    wbs: [
      { id: 1, parent_id: null, code: "1", name: "Planning and Design", item_type: "Deliverable", requirement_id: null },
      { id: 2, parent_id: null, code: "2", name: "Core Modules", item_type: "Deliverable", requirement_id: null },
      { id: 3, parent_id: null, code: "3", name: "User Interfaces", item_type: "Deliverable", requirement_id: null },
      { id: 4, parent_id: null, code: "4", name: "Testing and Deployment", item_type: "Deliverable", requirement_id: null },
      { id: 5, parent_id: 1, code: "1.1", name: "Requirements Document", item_type: "Work Package", requirement_id: null },
      { id: 6, parent_id: 1, code: "1.2", name: "Architecture and Database Design", item_type: "Work Package", requirement_id: null },
      { id: 7, parent_id: 2, code: "2.1", name: "Appointment Management", item_type: "Work Package", requirement_id: 1 },
      { id: 8, parent_id: 2, code: "2.2", name: "Patient Record Management", item_type: "Work Package", requirement_id: 6 },
      { id: 9, parent_id: 2, code: "2.3", name: "Lab Reports and Billing", item_type: "Work Package", requirement_id: 2 },
      { id: 10, parent_id: 2, code: "2.4", name: "Doctor Schedule and Online Consultation", item_type: "Work Package", requirement_id: 3 },
      { id: 11, parent_id: 3, code: "3.1", name: "Appointment and Patient UI", item_type: "Work Package", requirement_id: 1 },
      { id: 12, parent_id: 3, code: "3.2", name: "Lab, Billing and Consultation UI", item_type: "Work Package", requirement_id: 4 },
      { id: 13, parent_id: 4, code: "4.1", name: "Integration and System Testing", item_type: "Work Package", requirement_id: null },
      { id: 14, parent_id: 4, code: "4.2", name: "Defect Fixing and Stabilization", item_type: "Work Package", requirement_id: null },
      { id: 15, parent_id: 4, code: "4.3", name: "User Acceptance Testing and Deployment", item_type: "Work Package", requirement_id: null }
    ],
    changes: [
      { id: 1, code: "CR-01", title: "Add AI-based preliminary diagnosis module", justification: "Hospital management wants AI support to speed up first-level patient triage.", requirement_id: 7, cost_impact: 450000, schedule_impact_days: 14, status: "Pending", requested_date: "2026-10-05", decision_date: "", applied: false },
      { id: 2, code: "CR-02", title: "Add patient mobile application", justification: "Hospital management wants patients to access services from their phones.", requirement_id: 8, cost_impact: 350000, schedule_impact_days: 7, status: "Pending", requested_date: "2026-10-12", decision_date: "", applied: false }
    ],
    activities: [
      { id: 1, code: "A01", name: "Requirements Gathering", wbs_id: 5, assignee_id: 1, requirement_id: null, duration_days: 10, start_date: "2026-08-31", end_date: "2026-09-09", baseline_start: "2026-08-31", baseline_end: "2026-09-09", percent_complete: 100, planned_cost: 250000, actual_cost: 260000, status: "Completed", predecessors: [] },
      { id: 2, code: "A02", name: "System Architecture and DB Design", wbs_id: 6, assignee_id: 6, requirement_id: null, duration_days: 7, start_date: "2026-09-10", end_date: "2026-09-16", baseline_start: "2026-09-10", baseline_end: "2026-09-16", percent_complete: 100, planned_cost: 300000, actual_cost: 310000, status: "Completed", predecessors: [1] },
      { id: 3, code: "A03", name: "Appointment Module Backend", wbs_id: 7, assignee_id: 2, requirement_id: 1, duration_days: 21, start_date: "2026-09-17", end_date: "2026-10-07", baseline_start: "2026-09-17", baseline_end: "2026-10-07", percent_complete: 100, planned_cost: 600000, actual_cost: 680000, status: "Completed", predecessors: [2] },
      { id: 4, code: "A04", name: "Patient Records Module Backend", wbs_id: 8, assignee_id: 3, requirement_id: 6, duration_days: 21, start_date: "2026-09-17", end_date: "2026-10-07", baseline_start: "2026-09-17", baseline_end: "2026-10-07", percent_complete: 100, planned_cost: 600000, actual_cost: 640000, status: "Completed", predecessors: [2] },
      { id: 5, code: "A05", name: "Lab Reports and Billing Backend", wbs_id: 9, assignee_id: 2, requirement_id: 2, duration_days: 21, start_date: "2026-10-08", end_date: "2026-10-28", baseline_start: "2026-10-08", baseline_end: "2026-10-28", percent_complete: 60, planned_cost: 550000, actual_cost: 380000, status: "In Progress", predecessors: [3] },
      { id: 6, code: "A06", name: "Doctor Schedule and Consultation Backend", wbs_id: 10, assignee_id: 3, requirement_id: 3, duration_days: 14, start_date: "2026-10-08", end_date: "2026-10-21", baseline_start: "2026-10-08", baseline_end: "2026-10-21", percent_complete: 80, planned_cost: 450000, actual_cost: 400000, status: "In Progress", predecessors: [4] },
      { id: 7, code: "A07", name: "Appointment and Patient UI", wbs_id: 11, assignee_id: 4, requirement_id: 1, duration_days: 21, start_date: "2026-10-08", end_date: "2026-10-28", baseline_start: "2026-10-08", baseline_end: "2026-10-28", percent_complete: 70, planned_cost: 500000, actual_cost: 360000, status: "In Progress", predecessors: [3, 4] },
      { id: 8, code: "A08", name: "Lab, Billing and Consultation UI", wbs_id: 12, assignee_id: 5, requirement_id: 4, duration_days: 21, start_date: "2026-10-22", end_date: "2026-11-11", baseline_start: "2026-10-22", baseline_end: "2026-11-11", percent_complete: 20, planned_cost: 450000, actual_cost: 120000, status: "In Progress", predecessors: [6] },
      { id: 9, code: "A09", name: "Integration and System Testing", wbs_id: 13, assignee_id: 7, requirement_id: null, duration_days: 14, start_date: "2026-11-12", end_date: "2026-11-25", baseline_start: "2026-11-12", baseline_end: "2026-11-25", percent_complete: 0, planned_cost: 350000, actual_cost: 0, status: "Not Started", predecessors: [5, 7, 8] },
      { id: 10, code: "A10", name: "Defect Fixing and Stabilization", wbs_id: 14, assignee_id: 2, requirement_id: null, duration_days: 10, start_date: "2026-11-26", end_date: "2026-12-05", baseline_start: "2026-11-26", baseline_end: "2026-12-05", percent_complete: 0, planned_cost: 250000, actual_cost: 0, status: "Not Started", predecessors: [9] },
      { id: 11, code: "A11", name: "UAT and Deployment", wbs_id: 15, assignee_id: 1, requirement_id: null, duration_days: 10, start_date: "2026-12-06", end_date: "2026-12-15", baseline_start: "2026-12-06", baseline_end: "2026-12-15", percent_complete: 0, planned_cost: 300000, actual_cost: 0, status: "Not Started", predecessors: [10] }
    ],
    milestones: [
      { id: 1, name: "Requirements baseline approved", target_date: "2026-09-09", status: "Achieved", responsible_id: 1, activity_id: 1 },
      { id: 2, name: "Architecture and database design complete", target_date: "2026-09-16", status: "Achieved", responsible_id: 6, activity_id: 2 },
      { id: 3, name: "Core backend modules complete", target_date: "2026-10-28", status: "Pending", responsible_id: 2, activity_id: 5 },
      { id: 4, name: "User interfaces complete", target_date: "2026-11-11", status: "Pending", responsible_id: 5, activity_id: 8 },
      { id: 5, name: "System testing complete", target_date: "2026-11-25", status: "Pending", responsible_id: 7, activity_id: 9 },
      { id: 6, name: "Go-live and deployment", target_date: "2026-12-15", status: "Pending", responsible_id: 1, activity_id: 11 }
    ],
    defects: [
      { id: 1, code: "D-01", title: "Appointment double booking for the same doctor slot", severity: "Critical", status: "Open", assigned_to: 2, activity_id: 3, requirement_id: 1, reported_date: "2026-10-12", resolved_date: "" },
      { id: 2, code: "D-02", title: "Patient record overwritten when two users save together", severity: "Critical", status: "In Progress", assigned_to: 3, activity_id: 4, requirement_id: 6, reported_date: "2026-10-13", resolved_date: "" },
      { id: 3, code: "D-03", title: "Billing total rounding error", severity: "Medium", status: "Open", assigned_to: 2, activity_id: 5, requirement_id: 5, reported_date: "2026-10-20", resolved_date: "" },
      { id: 4, code: "D-04", title: "Button misalignment on appointment screen", severity: "Low", status: "Resolved", assigned_to: 4, activity_id: 7, requirement_id: 1, reported_date: "2026-10-15", resolved_date: "2026-10-20" }
    ],
    quality: [
      { id: 1, wbs_id: 5, item: "Requirements document reviewed and signed off", result: "Pass", inspector_id: 7, inspection_date: "2026-09-10", notes: "Approved by sponsor" },
      { id: 2, wbs_id: 6, item: "Database schema normalised and peer reviewed", result: "Pass", inspector_id: 7, inspection_date: "2026-09-17", notes: "No issues found" },
      { id: 3, wbs_id: 7, item: "Appointment module passes functional tests", result: "Fail", inspector_id: 7, inspection_date: "2026-10-12", notes: "Double booking found" },
      { id: 4, wbs_id: 8, item: "Patient record module passes functional tests", result: "Fail", inspector_id: 7, inspection_date: "2026-10-13", notes: "Concurrent save overwrites data" },
      { id: 5, wbs_id: 11, item: "User interface reviewed against mockups", result: "Pending", inspector_id: null, inspection_date: "", notes: "" }
    ],
    risks: [
      { id: 1, code: "R-01", description: "Scope creep from hospital management requests", likelihood: 5, impact: 4, preventive_action: "Enforce formal change request process", corrective_action: "Re-baseline scope, cost and schedule after approval", owner_id: 1, status: "Occurred" },
      { id: 2, code: "R-02", description: "AI diagnosis module is technically complex to integrate", likelihood: 4, impact: 4, preventive_action: "Build a prototype early and use a proven AI service", corrective_action: "Reduce module scope to a basic symptom checker", owner_id: 2, status: "Open" },
      { id: 3, code: "R-03", description: "Key developer becomes unavailable", likelihood: 2, impact: 4, preventive_action: "Cross-train backend developers", corrective_action: "Reassign tasks and add temporary resource", owner_id: 1, status: "Open" },
      { id: 4, code: "R-04", description: "Data inconsistency across the five hospitals", likelihood: 3, impact: 5, preventive_action: "Define a common data standard and validation rules", corrective_action: "Run data cleanup scripts and re-sync", owner_id: 6, status: "Open" },
      { id: 5, code: "R-05", description: "Budget overrun because of defects and rework", likelihood: 4, impact: 3, preventive_action: "Track EVM weekly and fix defects early", corrective_action: "Request budget revision through change control", owner_id: 1, status: "Open" }
    ]
  };
