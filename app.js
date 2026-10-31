(() => {
  "use strict";

  const STORE_KEY = "careplan.workspace.v1";
  const TODAY = new Date().toISOString().slice(0, 10);
  const $ = (selector, root = document) => root.querySelector(selector);
  const money = value => `PKR ${Number(value || 0).toLocaleString("en-PK", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  const pct = (value, total) => total ? `${Math.round((value / total) * 100)}%` : "0%";
  const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g, char => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
  const safeDate = value => /^\d{4}-\d{2}-\d{2}$/.test(String(value || "")) ? value : "";
  const prettyDate = value => safeDate(value) ? new Date(`${value}T00:00:00`).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" }) : "—";

  const seed = {
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

  const pages = {
    dashboard: ["Overview", "Project dashboard and priority follow-up"],
    initiation: ["Initiation & integration", "Maintain the charter, project plan, stakeholders, team, and milestones."],
    scope: ["Scope & WBS", "Control requirements, approved scope, work packages, and change requests."],
    schedule: ["Schedule & resources", "Plan activity dependencies, generate dates, and monitor delays."],
    cost: ["Cost & EVM", "Monitor planned value, earned value, actual cost, and project performance."],
    quality: ["Quality & risk", "Manage defects, inspections, risk priorities, and response plans."]
  };

  let state = loadState();
  let currentPage = "dashboard";
  let editingRecord = null;
  const pageContainer = $("#page-container");
  const dialog = $("#record-dialog");

  function clone(value) { return JSON.parse(JSON.stringify(value)); }

  function loadState() {
    try {
      const saved = localStorage.getItem(STORE_KEY);
      if (!saved) {
        localStorage.setItem(STORE_KEY, JSON.stringify(seed));
        return clone(seed);
      }
      const parsed = JSON.parse(saved);
      return validateState(parsed) ? parsed : clone(seed);
    } catch (error) {
      console.error("Could not load browser data:", error);
      return clone(seed);
    }
  }

  function validateState(value) {
    const keys = ["project", "plan", "stakeholders", "team", "requirements", "wbs", "changes", "activities", "milestones", "defects", "quality", "risks"];
    return value && value.schemaVersion === 1 && value.project && keys.every(key => Array.isArray(value[key]) || ["project", "plan"].includes(key));
  }

  function persist(message = "Changes saved on this browser.") {
    try {
      localStorage.setItem(STORE_KEY, JSON.stringify(state));
      render();
      toast(message);
      return true;
    } catch (error) {
      console.error("Could not save browser data:", error);
      toast("Could not save. Browser storage may be full or disabled.", true);
      return false;
    }
  }

  function toast(message, error = false) {
    const item = document.createElement("div");
    item.className = `toast${error ? " error" : ""}`;
    item.textContent = message;
    $("#toast-region").append(item);
    window.setTimeout(() => item.remove(), 3600);
  }

  function nextId(records) { return Math.max(0, ...records.map(item => Number(item.id) || 0)) + 1; }
  function byId(records, id) { return records.find(item => Number(item.id) === Number(id)); }
  function labelFor(records, id, key = "name") { return records.find(item => Number(item.id) === Number(id))?.[key] || "—"; }
  function listOption(records, valueKey, labelFn, selected, emptyLabel = "Not assigned") {
    const options = records.map(item => `<option value="${escapeHtml(item[valueKey])}" ${String(item[valueKey]) === String(selected ?? "") ? "selected" : ""}>${escapeHtml(labelFn(item))}</option>`).join("");
    return `<option value="">${escapeHtml(emptyLabel)}</option>${options}`;
  }

  function render() {
    $("#today-label").textContent = new Date().toLocaleDateString(undefined, { weekday: "short", month: "short", day: "numeric" });
    document.title = `CarePlan | ${pages[currentPage][0]}`;
    $("#current-section").textContent = pages[currentPage][0];
    document.querySelectorAll(".nav-link").forEach(button => button.classList.toggle("active", button.dataset.page === currentPage));
    const renderers = { dashboard: renderDashboard, initiation: renderInitiation, scope: renderScope, schedule: renderSchedule, cost: renderCost, quality: renderQuality };
    pageContainer.innerHTML = renderers[currentPage]();
  }

  function heading(page = currentPage, actions = "") {
    return `<div class="page-heading"><div><p class="eyebrow">CAREPLAN WORKSPACE</p><h1>${escapeHtml(pages[page][0])}</h1><p>${escapeHtml(pages[page][1])}</p></div><div class="heading-actions">${actions}</div></div>`;
  }

  function metric(label, value, detail, tone = "") {
    return `<article class="metric-card ${tone}"><div class="metric-label">${escapeHtml(label)}</div><div class="metric-value">${escapeHtml(value)}</div><div class="metric-detail">${escapeHtml(detail)}</div></article>`;
  }

  function panel(title, body, options = {}) {
    const subtitle = options.subtitle ? `<p class="panel-subtitle">${escapeHtml(options.subtitle)}</p>` : "";
    const actions = options.actions || "";
    return `<section class="panel"><div class="panel-header"><div><h2 class="panel-title">${escapeHtml(title)}</h2>${subtitle}</div>${actions}</div><div class="panel-body ${options.flush ? "flush" : ""}">${body}</div></section>`;
  }

  function badge(value) {
    const kind = String(value || "").toLowerCase().replace(/\s+/g, "-");
    const labels = { "in-progress": "In progress", "not-started": "Not started" };
    return `<span class="badge ${escapeHtml(kind)}">${escapeHtml(labels[kind] || value || "—")}</span>`;
  }

  function table(headers, rows, emptyText = "No records yet.") {
    if (!rows.length) return `<div class="empty-state"><strong>Nothing to show yet</strong>${escapeHtml(emptyText)}</div>`;
    return `<div class="table-wrap"><table><thead><tr>${headers.map(header => `<th>${escapeHtml(header)}</th>`).join("")}</tr></thead><tbody>${rows.join("")}</tbody></table></div>`;
  }

  function rowActions(entity, id, extras = "") {
    return `<div class="table-actions">${extras}<button class="table-action" data-action="edit" data-entity="${entity}" data-id="${id}">Edit</button><button class="table-action danger" data-action="delete" data-entity="${entity}" data-id="${id}">Delete</button></div>`;
  }

  function actionButton(label, action, entity = "", id = "", kind = "") {
    return `<button class="button ${kind}" data-action="${action}" ${entity ? `data-entity="${entity}"` : ""} ${id !== "" ? `data-id="${id}"` : ""}>${escapeHtml(label)}</button>`;
  }

  function displayDate(value) { return prettyDate(value); }

  function getSchedule() {
    const activities = state.activities;
    const indegree = new Map(activities.map(activity => [Number(activity.id), 0]));
    const successors = new Map(activities.map(activity => [Number(activity.id), []]));
    activities.forEach(activity => {
      (activity.predecessors || []).forEach(predId => {
        if (indegree.has(Number(predId))) {
          indegree.set(Number(activity.id), indegree.get(Number(activity.id)) + 1);
          successors.get(Number(predId)).push(Number(activity.id));
        }
      });
    });
    const ready = activities.filter(activity => indegree.get(Number(activity.id)) === 0).map(activity => Number(activity.id));
    const order = [];
    while (ready.length) {
      const id = ready.shift();
      order.push(id);
      successors.get(id).forEach(next => {
        indegree.set(next, indegree.get(next) - 1);
        if (indegree.get(next) === 0) ready.push(next);
      });
    }
    if (order.length !== activities.length) return { error: "Activity dependencies contain a cycle or invalid reference." };
    const dates = new Map();
    const projectStart = safeDate(state.project.start_date);
    if (!projectStart) return { error: "Set a valid project start date before generating the schedule." };
    order.forEach(id => {
      const activity = byId(activities, id);
      let start = projectStart;
      (activity.predecessors || []).forEach(predId => {
        const predecessor = dates.get(Number(predId));
        if (predecessor && predecessor.end >= start) start = addDays(predecessor.end, 1);
      });
      dates.set(id, { start, end: addDays(start, Math.max(1, Number(activity.duration_days) || 1) - 1) });
    });
    const finish = activities.length ? activities.reduce((latest, activity) => {
      const end = dates.get(Number(activity.id)).end;
      return !latest || end > latest ? end : latest;
    }, "") : "";
    return { order, dates, finish, deadline: state.project.deadline, overdue: Boolean(finish && state.project.deadline && finish > state.project.deadline) };
  }

  function addDays(date, amount) {
    if (!safeDate(date)) return "";
    const value = new Date(`${date}T00:00:00Z`);
    value.setUTCDate(value.getUTCDate() + Number(amount));
    return value.toISOString().slice(0, 10);
  }

  function getEvm() {
    const statusDate = safeDate(state.project.status_date) || TODAY;
    let pv = 0, ev = 0, ac = 0;
    state.activities.forEach(activity => {
      const cost = Number(activity.planned_cost) || 0;
      const start = safeDate(activity.baseline_start);
      const end = safeDate(activity.baseline_end);
      let plannedFraction = 0;
      if (start && end && statusDate >= start) {
        const totalDays = Math.max(1, Math.round((Date.parse(`${end}T00:00:00Z`) - Date.parse(`${start}T00:00:00Z`)) / 86400000) + 1);
        const elapsedDays = statusDate >= end ? totalDays : Math.max(0, Math.round((Date.parse(`${statusDate}T00:00:00Z`) - Date.parse(`${start}T00:00:00Z`)) / 86400000) + 1);
        plannedFraction = Math.min(1, elapsedDays / totalDays);
      }
      pv += cost * plannedFraction;
      ev += cost * Math.min(100, Math.max(0, Number(activity.percent_complete) || 0)) / 100;
      ac += Number(activity.actual_cost) || 0;
    });
    const cpi = ac ? ev / ac : 0;
    const spi = pv ? ev / pv : 0;
    return {
      bac: Number(state.project.approved_budget) || 0, pv, ev, ac, cpi, spi,
      cv: ev - ac, sv: ev - pv,
      eac: cpi > 0 ? (Number(state.project.approved_budget) || 0) / cpi : Number(state.project.approved_budget) || 0
    };
  }

  function getDelayRows() {
    const statusDate = safeDate(state.project.status_date) || TODAY;
    return state.activities.filter(activity => {
      const baselineEnd = safeDate(activity.baseline_end);
      return Number(activity.percent_complete) < 100 && baselineEnd && baselineEnd < statusDate;
    }).map(activity => ({
      activity,
      days: Math.max(1, Math.round((Date.parse(`${statusDate}T00:00:00Z`) - Date.parse(`${activity.baseline_end}T00:00:00Z`)) / 86400000))
    }));
  }

  function renderDashboard() {
    const evm = getEvm();
    const schedule = getSchedule();
    const delays = getDelayRows();
    const pending = state.changes.filter(change => change.status === "Pending").length;
    const critical = state.defects.filter(defect => defect.severity === "Critical" && !["Resolved", "Closed"].includes(defect.status)).length;
    const activeRisks = state.risks.filter(risk => ["Open", "Occurred"].includes(risk.status)).length;
    const passed = state.quality.filter(item => item.result === "Pass").length;
    const alerts = [];
    if (critical) alerts.push(`<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>${critical} unresolved critical defect${critical === 1 ? "" : "s"}</strong><span>Review in Quality &amp; Risk before project sign-off.</span></div></div>`);
    if (pending) alerts.push(`<div class="alert-item"><span class="alert-marker"></span><div><strong>${pending} change request${pending === 1 ? "" : "s"} awaiting decision</strong><span>Approval changes the linked scope, budget, and deadline.</span></div></div>`);
    if (delays.length) alerts.push(`<div class="alert-item"><span class="alert-marker"></span><div><strong>${delays.length} activity${delays.length === 1 ? "" : "ies"} past baseline</strong><span>Open Schedule to review the delay and downstream impact.</span></div></div>`);
    if (schedule.finish && schedule.overdue) alerts.push(`<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>Forecast finish is after the approved deadline</strong><span>Forecast ${prettyDate(schedule.finish)} · deadline ${prettyDate(state.project.deadline)}.</span></div></div>`);
    if (evm.bac && evm.ac > evm.bac) alerts.push(`<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>Actual expenditure exceeds approved budget</strong><span>${money(evm.ac)} spent against ${money(evm.bac)} approved.</span></div></div>`);
    const alertBody = alerts.length ? `<div class="alert-list">${alerts.join("")}</div>` : `<div class="no-alerts">No urgent follow-up flags from the current records.</div>`;
    const qualityProgress = `<div class="progress-track"><div class="progress-bar ${passed < state.quality.length ? "warning" : ""}" style="width:${state.quality.length ? passed / state.quality.length * 100 : 0}%"></div></div><div class="progress-caption"><span>${passed} passed</span><span>${state.quality.length} checks</span></div>`;
    const activityRows = state.activities.slice().sort((a, b) => Number(a.percent_complete) - Number(b.percent_complete)).slice(0, 5).map(activity => {
      const progress = Math.min(100, Math.max(0, Number(activity.percent_complete) || 0));
      return `<tr><td><span class="cell-primary">${escapeHtml(activity.name)}</span><span class="cell-secondary">${escapeHtml(activity.code)}</span></td><td>${badge(activity.status)}</td><td><div class="progress-track"><div class="progress-bar ${progress < 35 && activity.status === "In Progress" ? "warning" : ""}" style="width:${progress}%"></div></div><div class="progress-caption"><span>${progress}%</span><span>${escapeHtml(labelFor(state.team, activity.assignee_id))}</span></div></td></tr>`;
    });
    const delayRows = delays.map(({ activity, days }) => `<tr><td><span class="cell-primary">${escapeHtml(activity.code)} · ${escapeHtml(activity.name)}</span></td><td>${days} day${days === 1 ? "" : "s"}</td><td>${prettyDate(activity.baseline_end)}</td></tr>`);
    const summary = `<div class="hero-panel"><div class="hero-panel-content"><p class="eyebrow">PROJECT OPERATIONS · ${escapeHtml(state.project.status || "Initiated").toUpperCase()}</p><h2>${escapeHtml(state.project.name)}</h2><p>Monitor the delivery plan for five hospitals—from approved scope and schedule through cost performance, testing, and deployment.</p></div><div class="hero-panel-meta"><span>Reporting date</span><strong>${prettyDate(state.project.status_date)}</strong><span style="margin-top:11px">Approved deadline</span><strong>${prettyDate(state.project.deadline)}</strong></div></div>`;
    return `${heading("dashboard", actionButton("Edit project charter", "edit-project"))}${summary}<div class="metric-grid">
      ${metric("ACTIVITY PROGRESS", `${state.activities.filter(item => item.status === "Completed").length} / ${state.activities.length}`, `${pct(state.activities.filter(item => item.status === "Completed").length, state.activities.length)} complete`)}
      ${metric("APPROVED SCOPE", `${state.requirements.filter(item => item.status === "Approved").length} / ${state.requirements.length}`, "Requirements formally approved")}
      ${metric("OPEN DEFECTS", state.defects.filter(item => !["Resolved", "Closed"].includes(item.status)).length, `${critical} critical require priority review`, critical ? "alert" : "")}
      ${metric("ACTIVE RISKS", activeRisks, "Open or occurred risks", activeRisks ? "warning" : "good")}
      ${metric("QUALITY PASS RATE", pct(passed, state.quality.length), `${passed} of ${state.quality.length} checks passed`, passed < state.quality.length ? "warning" : "good")}
      ${metric("PENDING CHANGES", pending, "Awaiting approval or rejection", pending ? "warning" : "good")}
      ${metric("COST PERFORMANCE", evm.cpi.toFixed(2), `CPI · threshold ${Number(state.project.cost_threshold).toFixed(2)}`, evm.cpi && evm.cpi < Number(state.project.cost_threshold) ? "alert" : "")}
      ${metric("SCHEDULE PERFORMANCE", evm.spi.toFixed(2), `SPI · threshold ${Number(state.project.schedule_threshold).toFixed(2)}`, evm.spi && evm.spi < Number(state.project.schedule_threshold) ? "alert" : "")}
    </div><div class="content-grid">
      <div class="stack">${panel("Priority follow-up", alertBody, { subtitle: "Items needing the project manager's attention" })}
      ${panel("Activity progress", table(["Activity", "Status", "Completion"], activityRows, "Add activities in the Schedule module."), { subtitle: "A quick view of active delivery work", flush: true })}</div>
      <div class="stack">${panel("Readiness snapshot", `<div class="stack"><div class="meter-line"><span>Approved requirements</span><strong>${state.requirements.filter(item => item.status === "Approved").length} / ${state.requirements.length}</strong></div><div class="progress-track"><div class="progress-bar" style="width:${pct(state.requirements.filter(item => item.status === "Approved").length, state.requirements.length)}"></div></div><div class="meter-line"><span>Quality checks passed</span><strong>${passed} / ${state.quality.length}</strong></div>${qualityProgress}<div class="meter-line"><span>Milestones achieved</span><strong>${state.milestones.filter(item => item.status === "Achieved").length} / ${state.milestones.length}</strong></div></div>`, { subtitle: "Current completion indicators" })}
      ${panel("Delayed activities", table(["Activity", "Delay", "Baseline end"], delayRows.slice(0, 5), "No incomplete activity is past its baseline end date."), { flush: true })}</div>
    </div>`;
  }

  function renderInitiation() {
    const stakeholders = state.stakeholders.map(item => `<tr><td>${escapeHtml(item.name)}</td><td>${escapeHtml(item.role)}</td><td>${escapeHtml(item.interest)}</td><td>${rowActions("stakeholders", item.id)}</td></tr>`);
    const members = state.team.map(item => `<tr><td>${escapeHtml(item.name)}</td><td>${escapeHtml(item.role)}</td><td>${rowActions("team", item.id)}</td></tr>`);
    const milestones = state.milestones.map(item => `<tr><td>${escapeHtml(item.name)}</td><td>${prettyDate(item.target_date)}</td><td>${badge(item.status)}</td><td>${escapeHtml(labelFor(state.team, item.responsible_id))}</td><td>${rowActions("milestones", item.id)}</td></tr>`);
    return `${heading("initiation", actionButton("Edit project charter", "edit-project"))}
      <div class="content-grid">
        <div class="stack">
          ${panel("Project charter", `<form id="project-form"><div class="form-grid">
            ${field("Project name", "name", state.project.name, "text", { required: true, span: 2 })}
            ${field("Objectives", "objectives", state.project.objectives, "textarea", { span: 2 })}
            ${field("Scope boundaries", "scope_boundaries", state.project.scope_boundaries, "textarea", { span: 2 })}
            ${field("Approved budget (PKR)", "approved_budget", state.project.approved_budget, "number", { required: true, min: 1 })}
            ${field("Project status", "status", state.project.status, "select", { options: ["Initiated", "Planning", "Executing", "Monitoring", "Closed"] })}
            ${field("Start date", "start_date", state.project.start_date, "date", { required: true })}
            ${field("Deadline", "deadline", state.project.deadline, "date", { required: true })}
            ${field("Reporting date", "status_date", state.project.status_date, "date", { required: true })}
            ${field("Cost warning threshold (CPI)", "cost_threshold", state.project.cost_threshold, "number", { min: 0.1, max: 1.5, step: 0.01 })}
            ${field("Schedule warning threshold (SPI)", "schedule_threshold", state.project.schedule_threshold, "number", { min: 0.1, max: 1.5, step: 0.01 })}
          </div><div class="form-actions"><button class="button" type="submit">Save charter</button></div></form>`, { subtitle: "Project baseline and monitoring date" })}
          ${panel("Project management plan", `<form id="plan-form"><div class="form-grid">
            ${field("Scope summary", "scope_summary", state.plan.scope_summary, "textarea")}
            ${field("Schedule summary", "schedule_summary", state.plan.schedule_summary, "textarea")}
            ${field("Cost summary", "cost_summary", state.plan.cost_summary, "textarea")}
            ${field("Quality summary", "quality_summary", state.plan.quality_summary, "textarea")}
          </div><div class="form-actions"><span class="muted" style="margin-right:auto;align-self:center">Version ${state.plan.version} · Updated ${prettyDate(state.plan.last_updated)}</span><button class="button" type="submit">Save plan</button></div></form>`)}
        </div>
        <div class="stack">
          ${panel("Project team", table(["Name", "Responsibility", "Actions"], members, "Add team members."), { actions: actionButton("Add member", "add", "team"), flush: true })}
          ${panel("Stakeholders", table(["Stakeholder", "Role", "Interest", "Actions"], stakeholders, "Add project stakeholders."), { actions: actionButton("Add stakeholder", "add", "stakeholders"), flush: true })}
        </div>
      </div>
      <div class="section-spacer">${panel("Milestones", table(["Milestone", "Target date", "Status", "Responsible", "Actions"], milestones, "Add project milestones."), { actions: actionButton("Add milestone", "add", "milestones"), flush: true })}</div>
      <div class="section-spacer">${panel("Project closure", `<p class="muted">Closure is blocked while activities remain incomplete, critical/high defects are unresolved, or changes are awaiting a decision.</p><div class="form-grid"><div class="form-field span-2"><label for="closure-notes">Closure notes</label><textarea id="closure-notes" rows="2">${escapeHtml(state.project.closure_notes)}</textarea></div></div><div class="form-actions"><button class="button button-danger" data-action="close-project">Close project</button></div>`)}</div>`;
  }

  function renderScope() {
    const requirements = state.requirements.map(item => `<tr><td><span class="cell-primary">${escapeHtml(item.code)}</span></td><td><span class="cell-primary">${escapeHtml(item.title)}</span><span class="cell-secondary">${escapeHtml(item.description)}</span></td><td>${badge(item.priority)}</td><td>${badge(item.status)}</td><td>${rowActions("requirements", item.id)}</td></tr>`);
    const wbs = state.wbs.slice().sort((a, b) => String(a.code).localeCompare(String(b.code), undefined, { numeric: true })).map(item => {
      const req = byId(state.requirements, item.requirement_id);
      const child = item.parent_id ? " child" : "";
      return `<div class="wbs-item${child}"><span class="wbs-code">${escapeHtml(item.code)}</span><span class="wbs-name">${escapeHtml(item.name)}${req ? `<span class="cell-secondary">Linked: ${escapeHtml(req.code)} · ${escapeHtml(req.title)}</span>` : ""}</span><span class="wbs-type">${escapeHtml(item.item_type)}</span><div class="table-actions">${actionButton("Edit", "edit", "wbs", item.id, "button-small button-outline")}${actionButton("Delete", "delete", "wbs", item.id, "button-small button-danger")}</div></div>`;
    });
    const changes = state.changes.map(item => {
      const extras = item.status === "Pending" ? `<button class="table-action" data-action="approve-change" data-id="${item.id}">Approve &amp; apply</button><button class="table-action danger" data-action="reject-change" data-id="${item.id}">Reject</button>` : "";
      return `<tr><td>${escapeHtml(item.code)}</td><td><span class="cell-primary">${escapeHtml(item.title)}</span><span class="cell-secondary">${escapeHtml(item.justification)}</span></td><td>${escapeHtml(labelFor(state.requirements, item.requirement_id, "code"))}</td><td class="nowrap">${money(item.cost_impact)}</td><td>${Number(item.schedule_impact_days)} days</td><td>${badge(item.status)}${item.applied ? `<span class="cell-secondary">Applied to project</span>` : ""}</td><td><div class="table-actions">${extras}${rowActions("changes", item.id)}</div></td></tr>`;
    });
    const approvedReqs = state.requirements.filter(item => item.status === "Approved");
    const approvedList = approvedReqs.length ? approvedReqs.map(item => `<div class="wbs-item"><span class="wbs-code">${escapeHtml(item.code)}</span><span class="wbs-name">${escapeHtml(item.title)}</span><span class="badge approved">Approved</span></div>`).join("") : `<div class="empty-state">No approved requirements. Proposed requirements are kept out of the approved scope.</div>`;
    return `${heading("scope")}
      <div class="content-grid">
        <div class="stack">
          ${panel("Requirements register", table(["Code", "Requirement", "Priority", "Status", "Actions"], requirements, "Add requirements to begin scope planning."), { actions: actionButton("Add requirement", "add", "requirements"), flush: true })}
          ${panel("Change requests", table(["Code", "Request", "Linked req.", "Cost impact", "Schedule impact", "Decision", "Actions"], changes, "No change requests have been recorded."), { actions: actionButton("New change request", "add", "changes"), flush: true })}
        </div>
        <div class="stack">
          ${panel("Work breakdown structure", `<div class="wbs-list">${wbs.length ? wbs.join("") : `<div class="empty-state">Add deliverables and work packages to build the WBS.</div>`}</div>`, { actions: actionButton("Add WBS item", "add", "wbs"), subtitle: "Hierarchical deliverables and work packages" })}
          ${panel("Approved scope", `<div class="stack">${approvedList}</div>`, { subtitle: "Only approved requirements are included" })}
        </div>
      </div>`;
  }

  function renderSchedule() {
    const schedule = getSchedule();
    const activityRows = state.activities.map(activity => {
      const delay = getDelayRows().find(row => Number(row.activity.id) === Number(activity.id));
      return `<tr><td><span class="cell-primary">${escapeHtml(activity.code)}</span></td><td><span class="cell-primary">${escapeHtml(activity.name)}</span><span class="cell-secondary">${escapeHtml(labelFor(state.wbs, activity.wbs_id, "code"))} · ${escapeHtml(labelFor(state.team, activity.assignee_id))}</span></td><td>${Number(activity.duration_days)} days</td><td>${prettyDate(activity.start_date)}</td><td>${prettyDate(activity.end_date)}</td><td>${Number(activity.percent_complete)}%</td><td>${badge(delay ? "Delayed" : activity.status)}</td><td>${rowActions("activities", activity.id)}</td></tr>`;
    });
    const gantt = renderGantt();
    const delayRows = getDelayRows().map(({ activity, days }) => `<tr><td><span class="cell-primary">${escapeHtml(activity.code)} · ${escapeHtml(activity.name)}</span></td><td>${days} days after baseline end</td><td>${prettyDate(activity.baseline_end)}</td></tr>`);
    const finishNote = schedule.error ? `<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>Schedule cannot be generated</strong><span>${escapeHtml(schedule.error)}</span></div></div>` : schedule.finish ? `<div class="alert-item ${schedule.overdue ? "urgent" : ""}"><span class="alert-marker"></span><div><strong>Forecast finish: ${prettyDate(schedule.finish)}</strong><span>Approved deadline: ${prettyDate(state.project.deadline)} · ${schedule.overdue ? "Forecast is late." : "Forecast is within the deadline."}</span></div></div>` : `<div class="no-alerts">Add activities to create a schedule forecast.</div>`;
    const actions = actionButton("Generate schedule", "generate-schedule") + actionButton("Add activity", "add", "activities", "", "button-outline");
    const completedMilestones = state.milestones.filter(item => item.status === "Achieved").length;
    const metricCards = [
      metric("ACTIVITIES", state.activities.length, "Defined project activities"),
      metric("DELAYED ACTIVITIES", delayRows.length, "Past baseline end", delayRows.length ? "warning" : "good"),
      metric("FORECAST FINISH", schedule.finish ? prettyDate(schedule.finish) : "—", schedule.overdue ? "After approved deadline" : "Approved deadline " + prettyDate(state.project.deadline), schedule.overdue ? "alert" : ""),
      metric("MILESTONES", completedMilestones + " / " + state.milestones.length, "Achieved to date")
    ].join("");
    return `${heading("schedule", actions)}
      <div class="metric-grid">${metricCards}</div>
      <div class="stack">
        ${panel("Schedule forecast", finishNote)}
        ${panel("Activity plan", table(["Code", "Activity / owner", "Duration", "Start", "Finish", "Progress", "Status", "Actions"], activityRows, "Add activities and assign project team members."), { subtitle: "Dependencies are used when generating the schedule.", flush: true })}
        ${panel("Schedule timeline", gantt, { subtitle: "Timeline based on planned activity dates" })}
        <div class="content-grid equal">${panel("Delay impact", table(["Delayed activity", "Delay", "Baseline end"], delayRows, "No incomplete activity is past its baseline end date."), { flush: true })}
        ${panel("Project milestones", table(["Milestone", "Target", "Status", "Owner"], state.milestones.map(item => `<tr><td>${escapeHtml(item.name)}</td><td>${prettyDate(item.target_date)}</td><td>${badge(item.status)}</td><td>${escapeHtml(labelFor(state.team, item.responsible_id))}</td></tr>`), "No milestones defined."), { flush: true })}</div>
      </div>`;
  }

  function renderGantt() {
    const items = state.activities.filter(item => safeDate(item.start_date) && safeDate(item.end_date));
    if (!items.length) return `<div class="empty-state">Generate a schedule to display the activity timeline.</div>`;
    const first = items.reduce((min, item) => item.start_date < min ? item.start_date : min, items[0].start_date);
    const last = items.reduce((max, item) => item.end_date > max ? item.end_date : max, items[0].end_date);
    const total = Math.max(1, Math.round((Date.parse(`${last}T00:00:00Z`) - Date.parse(`${first}T00:00:00Z`)) / 86400000) + 1);
    const rows = items.map(item => {
      const left = Math.max(0, Math.round((Date.parse(`${item.start_date}T00:00:00Z`) - Date.parse(`${first}T00:00:00Z`)) / 86400000)) / total * 100;
      const width = Math.max(1.3, (Math.round((Date.parse(`${item.end_date}T00:00:00Z`) - Date.parse(`${item.start_date}T00:00:00Z`)) / 86400000) + 1) / total * 100);
      const late = getDelayRows().some(row => Number(row.activity.id) === Number(item.id));
      const kind = late ? "late" : item.status === "Completed" ? "done" : "";
      return `<div class="gantt-row"><div class="gantt-name" title="${escapeHtml(item.code + " · " + item.name)}">${escapeHtml(item.code)} · ${escapeHtml(item.name)}</div><div class="gantt-track"><div class="gantt-bar ${kind}" style="left:${left}%;width:${width}%"><span>${Number(item.percent_complete)}%</span></div></div></div>`;
    }).join("");
    return `<div class="gantt-wrap"><div class="gantt-axis"><span></span><div class="gantt-months"><span>${prettyDate(first)}</span><span>${prettyDate(last)}</span></div></div>${rows}</div>`;
  }

  function renderCost() {
    const report = getEvm();
    const costLimit = Number(state.project.cost_threshold) || .9;
    const scheduleLimit = Number(state.project.schedule_threshold) || .9;
    const costWarn = report.cpi > 0 && report.cpi < costLimit;
    const scheduleWarn = report.spi > 0 && report.spi < scheduleLimit;
    const warnings = [];
    if (report.ac > report.bac) warnings.push(`<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>Actual cost is above the approved project budget</strong><span>${money(report.ac)} actual against ${money(report.bac)} approved.</span></div></div>`);
    if (costWarn) warnings.push(`<div class="alert-item"><span class="alert-marker"></span><div><strong>Cost performance is below threshold</strong><span>CPI ${report.cpi.toFixed(2)} · threshold ${costLimit.toFixed(2)}.</span></div></div>`);
    if (scheduleWarn) warnings.push(`<div class="alert-item"><span class="alert-marker"></span><div><strong>Schedule performance is below threshold</strong><span>SPI ${report.spi.toFixed(2)} · threshold ${scheduleLimit.toFixed(2)}.</span></div></div>`);
    const activityRows = state.activities.map(item => {
      const planned = Number(item.planned_cost) || 0;
      const earned = planned * Math.min(100, Math.max(0, Number(item.percent_complete) || 0)) / 100;
      const actual = Number(item.actual_cost) || 0;
      return `<tr><td>${escapeHtml(item.code)}</td><td>${escapeHtml(item.name)}</td><td class="nowrap">${money(planned)}</td><td class="nowrap">${money(actual)}</td><td class="nowrap">${money(earned)}</td><td class="nowrap">${money(earned - actual)}</td><td>${Number(item.percent_complete)}%</td><td><button class="table-action" data-action="edit" data-entity="activities" data-id="${item.id}">Update costs</button></td></tr>`;
    });
    const metricRows = [
      ["Budget at Completion (BAC)", money(report.bac), "Approved project budget"],
      ["Planned Value (PV)", money(report.pv), `Planned budgeted work by ${prettyDate(state.project.status_date)}`],
      ["Earned Value (EV)", money(report.ev), "Budgeted value of completed work"],
      ["Actual Cost (AC)", money(report.ac), "Actual expenditure recorded"],
      ["Cost Variance (CV)", money(report.cv), report.cv >= 0 ? "Favorable: earned value is at/above actual cost" : "Unfavorable: actual cost exceeds earned value"],
      ["Schedule Variance (SV)", money(report.sv), report.sv >= 0 ? "Favorable: earned value is at/above planned value" : "Unfavorable: earned value is behind plan"],
      ["Cost Performance Index (CPI)", report.cpi.toFixed(2), `Warning threshold: ${costLimit.toFixed(2)}`],
      ["Schedule Performance Index (SPI)", report.spi.toFixed(2), `Warning threshold: ${scheduleLimit.toFixed(2)}`],
      ["Estimate at Completion (EAC)", money(report.eac), "Forecast total cost at current CPI"]
    ].map(row => `<tr><td><span class="cell-primary">${escapeHtml(row[0])}</span></td><td class="nowrap">${escapeHtml(row[1])}</td><td>${escapeHtml(row[2])}</td></tr>`);
    return `${heading("cost")}<div class="metric-grid">
      ${metric("APPROVED BUDGET", money(report.bac), "Budget at completion")}
      ${metric("PLANNED VALUE", money(report.pv), "PV at reporting date")}
      ${metric("EARNED VALUE", money(report.ev), "Budgeted value completed")}
      ${metric("ACTUAL COST", money(report.ac), report.ac > report.bac ? "Above approved budget" : "Recorded expenditure", report.ac > report.bac ? "alert" : "")}
      ${metric("COST VARIANCE", money(report.cv), report.cv >= 0 ? "At/under earned cost" : "Over earned cost", report.cv < 0 ? "alert" : "good")}
      ${metric("SCHEDULE VARIANCE", money(report.sv), report.sv >= 0 ? "At/ahead of plan" : "Behind plan", report.sv < 0 ? "warning" : "good")}
      ${metric("CPI", report.cpi.toFixed(2), `Threshold ${costLimit.toFixed(2)}`, costWarn ? "alert" : "good")}
      ${metric("SPI", report.spi.toFixed(2), `Threshold ${scheduleLimit.toFixed(2)}`, scheduleWarn ? "alert" : "good")}
    </div><div class="stack">
      ${panel("Performance alerts", warnings.length ? `<div class="alert-list">${warnings.join("")}</div>` : `<div class="no-alerts">Cost and schedule indicators are within configured thresholds based on current records.</div>`)}
      ${panel("Earned value indicators", table(["Measure", "Value", "Interpretation"], metricRows), { subtitle: `Status date: ${prettyDate(state.project.status_date)}`, flush: true })}
      ${panel("Activity cost tracking", table(["Code", "Activity", "Planned cost", "Actual cost", "Earned value", "Cost variance", "Progress", "Actions"], activityRows, "Add activities in the Schedule module."), { flush: true })}
    </div>`;
  }

  function renderQuality() {
    const defects = state.defects.map(item => `<tr class="${item.severity === "Critical" && !["Resolved", "Closed"].includes(item.status) ? "critical-row" : ""}"><td>${escapeHtml(item.code)}</td><td><span class="cell-primary">${escapeHtml(item.title)}</span><span class="cell-secondary">Reported ${prettyDate(item.reported_date)}</span></td><td>${badge(item.severity)}</td><td>${badge(item.status)}</td><td>${escapeHtml(labelFor(state.team, item.assigned_to))}</td><td>${prettyDate(item.resolved_date)}</td><td>${rowActions("defects", item.id)}</td></tr>`);
    const checks = state.quality.map(item => `<tr><td>${escapeHtml(labelFor(state.wbs, item.wbs_id, "code"))}</td><td>${escapeHtml(item.item)}</td><td>${badge(item.result)}</td><td>${escapeHtml(labelFor(state.team, item.inspector_id))}</td><td>${prettyDate(item.inspection_date)}</td><td>${rowActions("quality", item.id)}</td></tr>`);
    const risks = state.risks.slice().sort((a, b) => b.likelihood * b.impact - a.likelihood * a.impact).map(item => {
      const score = Number(item.likelihood) * Number(item.impact);
      return `<tr><td>${escapeHtml(item.code)}</td><td><span class="cell-primary">${escapeHtml(item.description)}</span></td><td>${item.likelihood}</td><td>${item.impact}</td><td class="risk-score">${score} · ${score >= 15 ? "High" : score >= 8 ? "Medium" : "Low"}</td><td>${escapeHtml(labelFor(state.team, item.owner_id))}</td><td>${badge(item.status)}</td><td>${rowActions("risks", item.id)}</td></tr>`;
    });
    const riskMatrix = renderRiskMatrix();
    const pdpc = state.risks.length ? `<div class="pdpc-list">${state.risks.map(item => `<div class="pdpc-row"><div class="pdpc-risk"><strong>${escapeHtml(item.code)} · ${escapeHtml(item.description)}</strong><br>Score ${Number(item.likelihood) * Number(item.impact)} / 25</div><div class="pdpc-arrow">→</div><div class="pdpc-response"><strong>Prevent:</strong> ${escapeHtml(item.preventive_action || "No preventive response recorded")}<br><strong>Respond:</strong> ${escapeHtml(item.corrective_action || "No corrective response recorded")}</div></div>`).join("")}</div>` : `<div class="empty-state">Add risks and their preventive/corrective responses to build the response view.</div>`;
    return `${heading("quality")}<div class="metric-grid">
      ${metric("OPEN DEFECTS", state.defects.filter(item => !["Resolved", "Closed"].includes(item.status)).length, "Unresolved items")}
      ${metric("CRITICAL DEFECTS", state.defects.filter(item => item.severity === "Critical" && !["Resolved", "Closed"].includes(item.status)).length, "Require priority review", state.defects.some(item => item.severity === "Critical" && !["Resolved", "Closed"].includes(item.status)) ? "alert" : "good")}
      ${metric("QUALITY PASS RATE", pct(state.quality.filter(item => item.result === "Pass").length, state.quality.length), `${state.quality.filter(item => item.result === "Pass").length} of ${state.quality.length} passed`)}
      ${metric("HIGH-PRIORITY RISKS", state.risks.filter(item => item.likelihood * item.impact >= 15 && !["Closed", "Mitigated"].includes(item.status)).length, "Score 15 or above", state.risks.some(item => item.likelihood * item.impact >= 15 && !["Closed", "Mitigated"].includes(item.status)) ? "warning" : "good")}
    </div><div class="stack">
      ${panel("Defect register", table(["Code", "Defect", "Severity", "Status", "Assigned to", "Resolved", "Actions"], defects, "Record defects found during testing."), { actions: actionButton("Log defect", "add", "defects"), flush: true })}
      ${panel("Quality checklist", table(["WBS", "Inspection item", "Result", "Inspector", "Date", "Actions"], checks, "Record inspections against project deliverables."), { actions: actionButton("Add inspection", "add", "quality"), flush: true })}
      <div class="content-grid equal">
        ${panel("Likelihood × impact matrix", riskMatrix, { subtitle: "Likelihood and impact are rated from 1 to 5" })}
        ${panel("Risk response view (PDPC)", pdpc, { subtitle: "Potential problem → preventive and corrective actions" })}
      </div>
      ${panel("Risk register", table(["Code", "Risk", "Likelihood", "Impact", "Score", "Owner", "Status", "Actions"], risks, "Add risks and assign owners."), { actions: actionButton("Add risk", "add", "risks"), flush: true })}
    </div>`;
  }

  function renderRiskMatrix() {
    const cells = [];
    cells.push(`<span class="matrix-label"></span>`);
    for (let impact = 5; impact >= 1; impact--) cells.push(`<span class="matrix-label">I${impact}</span>`);
    for (let likelihood = 5; likelihood >= 1; likelihood--) {
      cells.push(`<span class="matrix-label">L${likelihood}</span>`);
      for (let impact = 5; impact >= 1; impact--) {
        const score = likelihood * impact;
        const risks = state.risks.filter(item => Number(item.likelihood) === likelihood && Number(item.impact) === impact);
        const tone = score >= 15 ? "high-risk" : score >= 8 ? "medium-risk" : "low-risk";
        cells.push(`<div class="matrix-cell ${tone}" title="Likelihood ${likelihood}, impact ${impact}, score ${score}">${risks.map(item => `<span class="matrix-risk">${escapeHtml(item.code)}</span>`).join("")}</div>`);
      }
    }
    return `<div class="matrix">${cells.join("")}</div><div class="matrix-key"><span><i class="key-dot" style="background:#e7f3e7"></i>Low</span><span><i class="key-dot" style="background:#fff1cc"></i>Medium</span><span><i class="key-dot" style="background:#f9d9d4"></i>High</span></div>`;
  }

  function field(label, name, value = "", type = "text", options = {}) {
    const span = options.span ? ` span-${options.span}` : "";
    const required = options.required ? "required" : "";
    const min = options.min !== undefined ? `min="${options.min}"` : "";
    const max = options.max !== undefined ? `max="${options.max}"` : "";
    const step = options.step !== undefined ? `step="${options.step}"` : "";
    let control;
    if (type === "textarea") control = `<textarea name="${name}" ${required}>${escapeHtml(value)}</textarea>`;
    else if (type === "select") control = `<select name="${name}" ${required}>${options.options.map(item => `<option ${String(item) === String(value) ? "selected" : ""}>${escapeHtml(item)}</option>`).join("")}</select>`;
    else control = `<input name="${name}" type="${type}" value="${escapeHtml(value)}" ${required} ${min} ${max} ${step}>`;
    return `<div class="form-field${span}"><label>${escapeHtml(label)}</label>${control}</div>`;
  }

  const entityConfigs = {
    stakeholders: { title: "Stakeholder", fields: [
      { key: "name", label: "Name", required: true }, { key: "role", label: "Role", required: true }, { key: "interest", label: "Interest / expectations", type: "textarea", span: 2 }
    ] },
    team: { title: "Team member", fields: [
      { key: "name", label: "Name", required: true }, { key: "role", label: "Responsibility", required: true }
    ] },
    milestones: { title: "Milestone", fields: [
      { key: "name", label: "Milestone name", required: true, span: 2 }, { key: "target_date", label: "Target date", type: "date", required: true },
      { key: "status", label: "Status", type: "select", options: ["Pending", "Achieved", "Missed"] },
      { key: "responsible_id", label: "Responsible team member", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "activity_id", label: "Linked activity", type: "ref", ref: "activities", optionLabel: item => `${item.code} · ${item.name}` }
    ] },
    requirements: { title: "Requirement", fields: [
      { key: "code", label: "Requirement code", required: true }, { key: "priority", label: "Priority", type: "select", options: ["Critical", "High", "Medium", "Low"] },
      { key: "title", label: "Title", required: true, span: 2 }, { key: "description", label: "Description", type: "textarea", span: 2 },
      { key: "status", label: "Approval status", type: "select", options: ["Proposed", "Approved", "Rejected"] }
    ] },
    wbs: { title: "WBS item", fields: [
      { key: "code", label: "WBS code", required: true }, { key: "item_type", label: "Type", type: "select", options: ["Deliverable", "Work Package"] },
      { key: "name", label: "Name", required: true, span: 2 }, { key: "parent_id", label: "Parent item", type: "ref", ref: "wbs", optionLabel: item => `${item.code} · ${item.name}`, empty: "Top-level item" },
      { key: "requirement_id", label: "Linked approved requirement", type: "ref", ref: "approvedRequirements", optionLabel: item => `${item.code} · ${item.title}`, empty: "No linked requirement" }
    ] },
    changes: { title: "Change request", fields: [
      { key: "code", label: "Request code", required: true }, { key: "requirement_id", label: "Linked requirement", type: "ref", ref: "requirements", optionLabel: item => `${item.code} · ${item.title}` },
      { key: "title", label: "Request title", required: true, span: 2 }, { key: "justification", label: "Business justification", type: "textarea", required: true, span: 2 },
      { key: "cost_impact", label: "Estimated cost impact (PKR)", type: "number", min: 0, step: 1000, required: true },
      { key: "schedule_impact_days", label: "Schedule impact (days)", type: "number", min: 0, step: 1, required: true },
      { key: "requested_date", label: "Requested date", type: "date", required: true }
    ] },
    activities: { title: "Activity", fields: [
      { key: "code", label: "Activity code", required: true }, { key: "name", label: "Activity name", required: true },
      { key: "wbs_id", label: "WBS work package", type: "ref", ref: "workPackages", optionLabel: item => `${item.code} · ${item.name}` },
      { key: "assignee_id", label: "Assigned team member", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "requirement_id", label: "Approved requirement", type: "ref", ref: "approvedRequirements", optionLabel: item => `${item.code} · ${item.title}` },
      { key: "duration_days", label: "Duration (days)", type: "number", min: 1, step: 1, required: true },
      { key: "percent_complete", label: "Progress (%)", type: "number", min: 0, max: 100, step: 1, required: true },
      { key: "status", label: "Status", type: "select", options: ["Not Started", "In Progress", "Completed"] },
      { key: "planned_cost", label: "Planned cost (PKR)", type: "number", min: 0, step: 1000, required: true },
      { key: "actual_cost", label: "Actual cost (PKR)", type: "number", min: 0, step: 1000, required: true },
      { key: "baseline_start", label: "Baseline start", type: "date" }, { key: "baseline_end", label: "Baseline finish", type: "date" },
      { key: "predecessors", label: "Predecessor activities (Ctrl/Cmd-click to select multiple)", type: "multiRef", ref: "activities", optionLabel: item => `${item.code} · ${item.name}`, span: 2 }
    ] },
    defects: { title: "Defect", fields: [
      { key: "code", label: "Defect code", required: true }, { key: "severity", label: "Severity", type: "select", options: ["Critical", "High", "Medium", "Low"] },
      { key: "title", label: "Defect summary", required: true, span: 2 }, { key: "status", label: "Resolution status", type: "select", options: ["Open", "In Progress", "Resolved", "Closed"] },
      { key: "assigned_to", label: "Assigned team member", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "activity_id", label: "Related activity", type: "ref", ref: "activities", optionLabel: item => `${item.code} · ${item.name}` },
      { key: "requirement_id", label: "Related approved requirement", type: "ref", ref: "approvedRequirements", optionLabel: item => `${item.code} · ${item.title}` },
      { key: "reported_date", label: "Reported date", type: "date", required: true }, { key: "resolved_date", label: "Resolved date", type: "date" }
    ] },
    quality: { title: "Quality inspection", fields: [
      { key: "wbs_id", label: "WBS item", type: "ref", ref: "wbs", optionLabel: item => `${item.code} · ${item.name}` },
      { key: "result", label: "Inspection result", type: "select", options: ["Pending", "Pass", "Fail"] },
      { key: "item", label: "Checklist / inspection item", required: true, span: 2 },
      { key: "inspector_id", label: "Inspector", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "inspection_date", label: "Inspection date", type: "date" },
      { key: "notes", label: "Findings / notes", type: "textarea", span: 2 }
    ] },
    risks: { title: "Project risk", fields: [
      { key: "code", label: "Risk code", required: true }, { key: "status", label: "Status", type: "select", options: ["Open", "Mitigated", "Occurred", "Closed"] },
      { key: "description", label: "Risk description", required: true, span: 2 },
      { key: "likelihood", label: "Likelihood (1–5)", type: "number", min: 1, max: 5, step: 1, required: true },
      { key: "impact", label: "Impact (1–5)", type: "number", min: 1, max: 5, step: 1, required: true },
      { key: "owner_id", label: "Risk owner", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "preventive_action", label: "Preventive action", type: "textarea", span: 2 },
      { key: "corrective_action", label: "Corrective action", type: "textarea", span: 2 }
    ] }
  };

  function resolveOptions(ref) {
    if (ref === "approvedRequirements") return state.requirements.filter(item => item.status === "Approved");
    if (ref === "workPackages") return state.wbs.filter(item => item.item_type === "Work Package");
    return state[ref] || [];
  }

  function openRecordDialog(entity, record = null) {
    const config = entityConfigs[entity];
    if (!config) return;
    editingRecord = { entity, id: record?.id ?? null };
    $("#dialog-eyebrow").textContent = entity.toUpperCase().replace(/S$/, "") + " RECORD";
    $("#dialog-title").textContent = `${record ? "Edit" : "Add"} ${config.title.toLowerCase()}`;
    $("#dialog-fields").innerHTML = config.fields.map(item => {
      const value = record?.[item.key] ?? "";
      const span = item.span === 2 ? " span-2" : "";
      const required = item.required ? "required" : "";
      let control = "";
      if (item.type === "textarea") control = `<textarea name="${item.key}" ${required}>${escapeHtml(value)}</textarea>`;
      else if (item.type === "select") control = `<select name="${item.key}" ${required}>${item.options.map(option => `<option value="${escapeHtml(option)}" ${String(option) === String(value) ? "selected" : ""}>${escapeHtml(option)}</option>`).join("")}</select>`;
      else if (item.type === "ref") {
        const records = resolveOptions(item.ref);
        control = `<select name="${item.key}">${listOption(records, "id", item.optionLabel, value, item.empty || "Not assigned")}</select>`;
      } else if (item.type === "multiRef") {
        const ids = (value || []).map(Number);
        control = `<select name="${item.key}" multiple>${resolveOptions(item.ref).filter(option => Number(option.id) !== Number(record?.id)).map(option => `<option value="${option.id}" ${ids.includes(Number(option.id)) ? "selected" : ""}>${escapeHtml(item.optionLabel(option))}</option>`).join("")}</select><small class="dialog-hint">Use Ctrl (Windows) or Command (Mac) to select more than one predecessor.</small>`;
      } else control = `<input name="${item.key}" type="${item.type || "text"}" value="${escapeHtml(value)}" ${required} ${item.min !== undefined ? `min="${item.min}"` : ""} ${item.max !== undefined ? `max="${item.max}"` : ""} ${item.step !== undefined ? `step="${item.step}"` : ""}>`;
      return `<div class="dialog-field${span}"><label>${escapeHtml(item.label)}</label>${control}</div>`;
    }).join("");
    dialog.showModal();
  }

  function saveDialog(form) {
    if (!editingRecord) return;
    const { entity, id } = editingRecord;
    const config = entityConfigs[entity];
    const records = state[entity];
    const old = id === null ? null : byId(records, id);
    const record = old ? { ...old } : { id: nextId(records) };
    for (const fieldConfig of config.fields) {
      const control = form.elements[fieldConfig.key];
      if (!control) continue;
      if (fieldConfig.type === "multiRef") {
        record[fieldConfig.key] = Array.from(control.selectedOptions).map(option => Number(option.value));
      } else if (fieldConfig.type === "ref") {
        record[fieldConfig.key] = control.value ? Number(control.value) : null;
      } else if (fieldConfig.type === "number") {
        record[fieldConfig.key] = control.value === "" ? 0 : Number(control.value);
        if (!Number.isFinite(record[fieldConfig.key])) throw new Error(`${fieldConfig.label} must be a valid number.`);
      } else {
        record[fieldConfig.key] = control.value.trim();
      }
    }
    if (entity === "requirements" && record.status !== "Approved" && state.wbs.some(item => Number(item.requirement_id) === Number(record.id))) {
      if (!confirm("This requirement is linked to one or more WBS items. Change its status anyway?")) throw new Error("Requirement status change was cancelled.");
    }
    if (entity === "activities") {
      if (record.percent_complete >= 100) record.status = "Completed";
      else if (record.percent_complete > 0 && record.status === "Not Started") record.status = "In Progress";
      if (record.baseline_start && record.baseline_end && record.baseline_end < record.baseline_start) throw new Error("Baseline finish cannot be before baseline start.");
      if ((record.predecessors || []).includes(Number(record.id))) throw new Error("An activity cannot depend on itself.");
    }
    if (entity === "defects") {
      if (["Resolved", "Closed"].includes(record.status) && !record.resolved_date) throw new Error("Enter a resolved date for a resolved or closed defect.");
      if (!["Resolved", "Closed"].includes(record.status)) record.resolved_date = "";
    }
    if (entity === "changes") {
      record.status = old?.status || "Pending";
      record.applied = Boolean(old?.applied);
      record.decision_date = old?.decision_date || "";
    }
    if (old) records[records.indexOf(old)] = record;
    else records.push(record);
    if (entity === "plan") { /* project plan is edited through its own form */ }
    editingRecord = null;
    dialog.close();
    persist(`${config.title} ${old ? "updated" : "added"}.`);
  }

  function editProjectDialog() {
    const fields = [
      { key: "name", label: "Project name", required: true }, { key: "approved_budget", label: "Approved budget (PKR)", type: "number", required: true, min: 1 },
      { key: "objectives", label: "Objectives", type: "textarea", span: 2 }, { key: "scope_boundaries", label: "Scope boundaries", type: "textarea", span: 2 },
      { key: "status", label: "Status", type: "select", options: ["Initiated", "Planning", "Executing", "Monitoring", "Closed"] },
      { key: "start_date", label: "Start date", type: "date", required: true }, { key: "deadline", label: "Deadline", type: "date", required: true },
      { key: "status_date", label: "Reporting date", type: "date", required: true }
    ];
    editingRecord = { entity: "project", id: 1, fields };
    $("#dialog-eyebrow").textContent = "PROJECT BASELINE";
    $("#dialog-title").textContent = "Edit project charter";
    $("#dialog-fields").innerHTML = fields.map(item => {
      const value = state.project[item.key] ?? "";
      const span = item.span === 2 ? " span-2" : "";
      const control = item.type === "textarea"
        ? `<textarea name="${item.key}">${escapeHtml(value)}</textarea>`
        : item.type === "select"
          ? `<select name="${item.key}">${item.options.map(option => `<option ${option === value ? "selected" : ""}>${escapeHtml(option)}</option>`).join("")}</select>`
          : `<input name="${item.key}" type="${item.type || "text"}" value="${escapeHtml(value)}" ${item.required ? "required" : ""} ${item.min ? `min="${item.min}"` : ""}>`;
      return `<div class="dialog-field${span}"><label>${escapeHtml(item.label)}</label>${control}</div>`;
    }).join("");
    dialog.showModal();
  }

  function submitProject(form) {
    const oldDeadline = state.project.deadline;
    for (const key of ["name", "objectives", "scope_boundaries", "status", "start_date", "deadline", "status_date"]) state.project[key] = form.elements[key].value.trim();
    state.project.approved_budget = Number(form.elements.approved_budget.value);
    if (!state.project.name || !state.project.start_date || !state.project.deadline || !state.project.status_date) throw new Error("Complete the required project fields.");
    if (!(state.project.deadline > state.project.start_date)) throw new Error("Project deadline must be after the start date.");
    if (oldDeadline !== state.project.deadline) toast("Project deadline updated. Review milestone dates and the schedule forecast.");
    editingRecord = null;
    dialog.close();
    persist("Project charter saved.");
  }

  function deleteRecord(entity, id) {
    const record = byId(state[entity], id);
    if (!record) return;
    if (entity === "requirements" && (
      state.changes.some(change => Number(change.requirement_id) === Number(id)) ||
      state.wbs.some(item => Number(item.requirement_id) === Number(id)) ||
      state.activities.some(item => Number(item.requirement_id) === Number(id)) ||
      state.defects.some(item => Number(item.requirement_id) === Number(id))
    )) {
      toast("This requirement is referenced by scope, a task, a defect, or a change request. Remove those links first.", true);
      return;
    }
    if (entity === "wbs" && (
      state.wbs.some(item => Number(item.parent_id) === Number(id)) ||
      state.activities.some(item => Number(item.wbs_id) === Number(id)) ||
      state.quality.some(item => Number(item.wbs_id) === Number(id))
    )) {
      toast("This WBS item has child items, activities, or inspections linked to it. Remove those links first.", true);
      return;
    }
    if (entity === "activities" && state.activities.some(item => (item.predecessors || []).map(Number).includes(Number(id)))) {
      toast("Other activities depend on this activity. Remove those dependencies first.", true);
      return;
    }
    if (entity === "activities" && (
      state.milestones.some(item => Number(item.activity_id) === Number(id)) ||
      state.defects.some(item => Number(item.activity_id) === Number(id))
    )) {
      toast("This activity is linked to a milestone or defect. Update those records first.", true);
      return;
    }
    if (entity === "changes" && record.applied) {
      toast("Applied change requests are retained as project decision history.", true);
      return;
    }
    if (!confirm(`Delete ${record.code || record.name || record.title || "this record"}? This cannot be undone except by importing a backup.`)) return;
    state[entity] = state[entity].filter(item => Number(item.id) !== Number(id));
    if (entity === "team") {
      state.milestones.forEach(item => { if (Number(item.responsible_id) === Number(id)) item.responsible_id = null; });
      state.defects.forEach(item => { if (Number(item.assigned_to) === Number(id)) item.assigned_to = null; });
      state.risks.forEach(item => { if (Number(item.owner_id) === Number(id)) item.owner_id = null; });
    }
    if (entity === "wbs") state.quality.forEach(item => { if (Number(item.wbs_id) === Number(id)) item.wbs_id = null; });
    persist("Record deleted.");
  }

  function decideChange(id, approve) {
    const change = byId(state.changes, id);
    if (!change || change.status !== "Pending") return;
    if (approve) {
      const req = byId(state.requirements, change.requirement_id);
      if (!req) { toast("Cannot approve: the linked requirement no longer exists.", true); return; }
      const confirmation = `Approve ${change.code}?\n\nBudget: ${money(state.project.approved_budget)} → ${money(Number(state.project.approved_budget) + Number(change.cost_impact))}\nDeadline: ${state.project.deadline} → ${addDays(state.project.deadline, Number(change.schedule_impact_days))}\n\nThe linked requirement will become approved scope.`;
      if (!confirm(confirmation)) return;
      state.project.approved_budget = Number(state.project.approved_budget) + Number(change.cost_impact);
      state.project.deadline = addDays(state.project.deadline, Number(change.schedule_impact_days));
      change.status = "Approved";
      change.decision_date = TODAY;
      change.applied = true;
      req.status = "Approved";
      state.plan.version = Number(state.plan.version || 1) + 1;
      state.plan.last_updated = TODAY;
      persist(`${change.code} approved and applied to scope, budget, and deadline.`);
    } else {
      if (!confirm(`Reject ${change.code} and keep its linked requirement out of approved scope?`)) return;
      change.status = "Rejected";
      change.decision_date = TODAY;
      const req = byId(state.requirements, change.requirement_id);
      if (req && req.status === "Approved" && !state.wbs.some(item => Number(item.requirement_id) === Number(req.id))) req.status = "Proposed";
      persist(`${change.code} rejected; project baseline unchanged.`);
    }
  }

  function generateSchedule() {
    const schedule = getSchedule();
    if (schedule.error) { toast(schedule.error, true); return; }
    if (!state.activities.length) { toast("Add project activities before generating a schedule.", true); return; }
    schedule.order.forEach(id => {
      const activity = byId(state.activities, id);
      const dates = schedule.dates.get(id);
      activity.start_date = dates.start;
      activity.end_date = dates.end;
    });
    persist(`Schedule generated. Forecast finish: ${prettyDate(schedule.finish)}${schedule.overdue ? " (after the current deadline)" : ""}.`);
  }

  function closeProject() {
    const incomplete = state.activities.filter(item => item.status !== "Completed").length;
    const blockingDefects = state.defects.filter(item => ["Critical", "High"].includes(item.severity) && !["Resolved", "Closed"].includes(item.status)).length;
    const pending = state.changes.filter(item => item.status === "Pending").length;
    if (incomplete || blockingDefects || pending) {
      toast(`Cannot close: ${incomplete} incomplete activities, ${blockingDefects} unresolved critical/high defects, ${pending} pending changes.`, true);
      return;
    }
    if (!confirm("Close this project? This updates its status and closure date.")) return;
    state.project.status = "Closed";
    state.project.closure_date = TODAY;
    state.project.status_date = TODAY;
    state.project.closure_notes = $("#closure-notes")?.value.trim() || "";
    persist("Project closed and closure record saved.");
  }

  function exportBackup() {
    const data = new Blob([JSON.stringify({ ...state, exportedAt: new Date().toISOString() }, null, 2)], { type: "application/json" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(data);
    link.download = `careplan-backup-${TODAY}.json`;
    link.click();
    URL.revokeObjectURL(link.href);
    toast("Backup exported.");
  }

  function importBackup(file) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      try {
        const parsed = JSON.parse(reader.result);
        if (!validateState(parsed)) throw new Error("This file is not a valid CarePlan version 1 backup.");
        if (!confirm("Importing replaces all current data in this browser. Export a backup first if needed. Continue?")) return;
        state = parsed;
        if (!persist("Backup imported.")) return;
      } catch (error) {
        toast(error.message || "Could not read the backup file.", true);
      }
    };
    reader.onerror = () => toast("Could not read the selected backup file.", true);
    reader.readAsText(file);
  }

  function handleClick(event) {
    const navigation = event.target.closest("[data-page]");
    if (navigation && navigation.classList.contains("nav-link")) {
      currentPage = navigation.dataset.page;
      $("#sidebar").classList.remove("open");
      render();
      return;
    }
    const button = event.target.closest("[data-action]");
    if (!button) return;
    const { action, entity, id } = button.dataset;
    if (action === "add") openRecordDialog(entity);
    else if (action === "edit") openRecordDialog(entity, byId(state[entity], id));
    else if (action === "delete") deleteRecord(entity, id);
    else if (action === "edit-project") editProjectDialog();
    else if (action === "approve-change") decideChange(id, true);
    else if (action === "reject-change") decideChange(id, false);
    else if (action === "generate-schedule") generateSchedule();
    else if (action === "close-project") closeProject();
  }

  function handlePageSubmit(event) {
    if (event.target.id === "project-form") {
      event.preventDefault();
      try {
        const formData = new FormData(event.target);
        state.project.name = formData.get("name").trim();
        state.project.objectives = formData.get("objectives").trim();
        state.project.scope_boundaries = formData.get("scope_boundaries").trim();
        state.project.approved_budget = Number(formData.get("approved_budget"));
        state.project.status = formData.get("status");
        state.project.start_date = formData.get("start_date");
        state.project.deadline = formData.get("deadline");
        state.project.status_date = formData.get("status_date");
        state.project.cost_threshold = Number(formData.get("cost_threshold"));
        state.project.schedule_threshold = Number(formData.get("schedule_threshold"));
        if (!state.project.name || !state.project.start_date || !state.project.deadline) throw new Error("Complete the required project fields.");
        if (state.project.deadline <= state.project.start_date) throw new Error("Deadline must be after the start date.");
        if (state.project.approved_budget <= 0) throw new Error("Approved budget must be greater than zero.");
        if (!(state.project.cost_threshold > 0 && state.project.schedule_threshold > 0)) throw new Error("Performance thresholds must be greater than zero.");
        persist("Project charter saved.");
      } catch (error) { toast(error.message, true); }
    } else if (event.target.id === "plan-form") {
      event.preventDefault();
      const data = new FormData(event.target);
      for (const key of ["scope_summary", "schedule_summary", "cost_summary", "quality_summary"]) state.plan[key] = data.get(key).trim();
      state.plan.version = Number(state.plan.version || 1) + 1;
      state.plan.last_updated = TODAY;
      persist("Project plan saved.");
    }
  }

  function handleDialogSubmit(event) {
    if (event.target.id !== "record-form") return;
    event.preventDefault();
    try {
      if (!editingRecord) return;
      if (editingRecord.entity === "project") submitProject(event.target);
      else saveDialog(event.target);
    } catch (error) { toast(error.message || "Check the entered values.", true); }
  }

  $("#page-container").addEventListener("click", handleClick);
  $("#sidebar").addEventListener("click", handleClick);
  $("#page-container").addEventListener("submit", handlePageSubmit);
  $("#record-form").addEventListener("submit", handleDialogSubmit);
  $("#cancel-dialog").addEventListener("click", () => { editingRecord = null; dialog.close(); });
  $("#close-dialog").addEventListener("click", () => { editingRecord = null; dialog.close(); });
  $("#mobile-menu").addEventListener("click", () => $("#sidebar").classList.toggle("open"));
  $("#export-data").addEventListener("click", exportBackup);
  $("#import-data").addEventListener("click", () => $("#import-file").click());
  $("#import-file").addEventListener("change", event => {
    importBackup(event.target.files[0]);
    event.target.value = "";
  });

  render();
})();
