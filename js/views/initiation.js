import { app } from "../core/context.js";
import { pages } from "../config.js";
import { escapeHtml, labelFor, money, pct, prettyDate } from "../core/utils.js";
import { getDelayRows, getEvm, getSchedule } from "../core/calculations.js";
import { actionButton, badge, field, heading, metric, panel, rowActions, table } from "../ui/components.js";

export function renderInitiation() {
    const stakeholders = app.state.stakeholders.map(item => `<tr><td>${escapeHtml(item.name)}</td><td>${escapeHtml(item.role)}</td><td>${escapeHtml(item.interest)}</td><td>${rowActions("stakeholders", item.id)}</td></tr>`);
    const members = app.state.team.map(item => `<tr><td>${escapeHtml(item.name)}</td><td>${escapeHtml(item.role)}</td><td>${rowActions("team", item.id)}</td></tr>`);
    const milestones = app.state.milestones.map(item => `<tr><td>${escapeHtml(item.name)}</td><td>${prettyDate(item.target_date)}</td><td>${badge(item.status)}</td><td>${escapeHtml(labelFor(app.state.team, item.responsible_id))}</td><td>${rowActions("milestones", item.id)}</td></tr>`);
    return `${heading("initiation", actionButton("Edit project charter", "edit-project"))}
      <div class="content-grid">
        <div class="stack">
          ${panel("Project charter", `<form id="project-form"><div class="form-grid">
            ${field("Project name", "name", app.state.project.name, "text", { required: true, span: 2 })}
            ${field("Objectives", "objectives", app.state.project.objectives, "textarea", { span: 2 })}
            ${field("Scope boundaries", "scope_boundaries", app.state.project.scope_boundaries, "textarea", { span: 2 })}
            ${field("Approved budget (PKR)", "approved_budget", app.state.project.approved_budget, "number", { required: true, min: 1 })}
            ${field("Project status", "status", app.state.project.status, "select", { options: ["Initiated", "Planning", "Executing", "Monitoring", "Closed"] })}
            ${field("Start date", "start_date", app.state.project.start_date, "date", { required: true })}
            ${field("Deadline", "deadline", app.state.project.deadline, "date", { required: true })}
            ${field("Reporting date", "status_date", app.state.project.status_date, "date", { required: true })}
            ${field("Cost warning threshold (CPI)", "cost_threshold", app.state.project.cost_threshold, "number", { min: 0.1, max: 1.5, step: 0.01 })}
            ${field("Schedule warning threshold (SPI)", "schedule_threshold", app.state.project.schedule_threshold, "number", { min: 0.1, max: 1.5, step: 0.01 })}
          </div><div class="form-actions"><button class="button" type="submit">Save charter</button></div></form>`, { subtitle: "Project baseline and monitoring date" })}
          ${panel("Project management plan", `<form id="plan-form"><div class="form-grid">
            ${field("Scope summary", "scope_summary", app.state.plan.scope_summary, "textarea")}
            ${field("Schedule summary", "schedule_summary", app.state.plan.schedule_summary, "textarea")}
            ${field("Cost summary", "cost_summary", app.state.plan.cost_summary, "textarea")}
            ${field("Quality summary", "quality_summary", app.state.plan.quality_summary, "textarea")}
          </div><div class="form-actions"><span class="muted" style="margin-right:auto;align-self:center">Version ${app.state.plan.version} · Updated ${prettyDate(app.state.plan.last_updated)}</span><button class="button" type="submit">Save plan</button></div></form>`)}
        </div>
        <div class="stack">
          ${panel("Project team", table(["Name", "Responsibility", "Actions"], members, "Add team members."), { actions: actionButton("Add member", "add", "team"), flush: true })}
          ${panel("Stakeholders", table(["Stakeholder", "Role", "Interest", "Actions"], stakeholders, "Add project stakeholders."), { actions: actionButton("Add stakeholder", "add", "stakeholders"), flush: true })}
        </div>
      </div>
      <div class="section-spacer">${panel("Milestones", table(["Milestone", "Target date", "Status", "Responsible", "Actions"], milestones, "Add project milestones."), { actions: actionButton("Add milestone", "add", "milestones"), flush: true })}</div>
      <div class="section-spacer">${panel("Project closure", `<p class="muted">Closure is blocked while activities remain incomplete, critical/high defects are unresolved, or changes are awaiting a decision.</p><div class="form-grid"><div class="form-field span-2"><label for="closure-notes">Closure notes</label><textarea id="closure-notes" rows="2">${escapeHtml(app.state.project.closure_notes)}</textarea></div></div><div class="form-actions"><button class="button button-danger" data-action="close-project">Close project</button></div>`)}</div>`;
  }
