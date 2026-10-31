import { TODAY } from "../config.js";
import { app } from "../core/context.js";
import { $, byId, escapeHtml, money, prettyDate } from "../core/utils.js";
import { addDays, getSchedule } from "../core/calculations.js";

const dialog = $("#record-dialog");

export function editProjectDialog() {
    const fields = [
      { key: "name", label: "Project name", required: true }, { key: "approved_budget", label: "Approved budget (PKR)", type: "number", required: true, min: 1 },
      { key: "objectives", label: "Objectives", type: "textarea", span: 2 }, { key: "scope_boundaries", label: "Scope boundaries", type: "textarea", span: 2 },
      { key: "status", label: "Status", type: "select", options: ["Initiated", "Planning", "Executing", "Monitoring", "Closed"] },
      { key: "start_date", label: "Start date", type: "date", required: true }, { key: "deadline", label: "Deadline", type: "date", required: true },
      { key: "status_date", label: "Reporting date", type: "date", required: true }
    ];
    app.editingRecord = { entity: "project", id: 1, fields };
    $("#dialog-eyebrow").textContent = "PROJECT BASELINE";
    $("#dialog-title").textContent = "Edit project charter";
    $("#dialog-fields").innerHTML = fields.map(item => {
      const value = app.state.project[item.key] ?? "";
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

export function submitProject(form) {
    const oldDeadline = app.state.project.deadline;
    for (const key of ["name", "objectives", "scope_boundaries", "status", "start_date", "deadline", "status_date"]) app.state.project[key] = form.elements[key].value.trim();
    app.state.project.approved_budget = Number(form.elements.approved_budget.value);
    if (!app.state.project.name || !app.state.project.start_date || !app.state.project.deadline || !app.state.project.status_date) throw new Error("Complete the required project fields.");
    if (!(app.state.project.deadline > app.state.project.start_date)) throw new Error("Project deadline must be after the start date.");
    if (oldDeadline !== app.state.project.deadline) app.toast("Project deadline updated. Review milestone dates and the schedule forecast.");
    app.editingRecord = null;
    dialog.close();
    app.persist("Project charter saved.");
  }

export function decideChange(id, approve) {
    const change = byId(app.state.changes, id);
    if (!change || change.status !== "Pending") return;
    if (approve) {
      const req = byId(app.state.requirements, change.requirement_id);
      if (!req) { app.toast("Cannot approve: the linked requirement no longer exists.", true); return; }
      const confirmation = `Approve ${change.code}?\n\nBudget: ${money(app.state.project.approved_budget)} → ${money(Number(app.state.project.approved_budget) + Number(change.cost_impact))}\nDeadline: ${app.state.project.deadline} → ${addDays(app.state.project.deadline, Number(change.schedule_impact_days))}\n\nThe linked requirement will become approved scope.`;
      if (!confirm(confirmation)) return;
      app.state.project.approved_budget = Number(app.state.project.approved_budget) + Number(change.cost_impact);
      app.state.project.deadline = addDays(app.state.project.deadline, Number(change.schedule_impact_days));
      change.status = "Approved";
      change.decision_date = TODAY;
      change.applied = true;
      req.status = "Approved";
      app.state.plan.version = Number(app.state.plan.version || 1) + 1;
      app.state.plan.last_updated = TODAY;
      app.persist(`${change.code} approved and applied to scope, budget, and deadline.`);
    } else {
      if (!confirm(`Reject ${change.code} and keep its linked requirement out of approved scope?`)) return;
      change.status = "Rejected";
      change.decision_date = TODAY;
      const req = byId(app.state.requirements, change.requirement_id);
      if (req && req.status === "Approved" && !app.state.wbs.some(item => Number(item.requirement_id) === Number(req.id))) req.status = "Proposed";
      app.persist(`${change.code} rejected; project baseline unchanged.`);
    }
  }

export function generateSchedule() {
    const schedule = getSchedule();
    if (schedule.error) { app.toast(schedule.error, true); return; }
    if (!app.state.activities.length) { app.toast("Add project activities before generating a schedule.", true); return; }
    schedule.order.forEach(id => {
      const activity = byId(app.state.activities, id);
      const dates = schedule.dates.get(id);
      activity.start_date = dates.start;
      activity.end_date = dates.end;
    });
    app.persist(`Schedule generated. Forecast finish: ${prettyDate(schedule.finish)}${schedule.overdue ? " (after the current deadline)" : ""}.`);
  }

export function closeProject() {
    const incomplete = app.state.activities.filter(item => item.status !== "Completed").length;
    const blockingDefects = app.state.defects.filter(item => ["Critical", "High"].includes(item.severity) && !["Resolved", "Closed"].includes(item.status)).length;
    const pending = app.state.changes.filter(item => item.status === "Pending").length;
    if (incomplete || blockingDefects || pending) {
      app.toast(`Cannot close: ${incomplete} incomplete activities, ${blockingDefects} unresolved critical/high defects, ${pending} pending changes.`, true);
      return;
    }
    if (!confirm("Close this project? This updates its status and closure date.")) return;
    app.state.project.status = "Closed";
    app.state.project.closure_date = TODAY;
    app.state.project.status_date = TODAY;
    app.state.project.closure_notes = $("#closure-notes")?.value.trim() || "";
    app.persist("Project closed and closure record saved.");
  }
