import { app } from "../core/context.js";
import { pages } from "../config.js";
import { byId, escapeHtml, labelFor, money, pct, prettyDate } from "../core/utils.js";
import { getDelayRows, getEvm, getSchedule } from "../core/calculations.js";
import { actionButton, badge, heading, metric, panel, rowActions, table } from "../ui/components.js";

export function renderScope() {
    const requirements = app.state.requirements.map(item => `<tr><td><span class="cell-primary">${escapeHtml(item.code)}</span></td><td><span class="cell-primary">${escapeHtml(item.title)}</span><span class="cell-secondary">${escapeHtml(item.description)}</span></td><td>${badge(item.priority)}</td><td>${badge(item.status)}</td><td>${rowActions("requirements", item.id)}</td></tr>`);
    const wbs = app.state.wbs.slice().sort((a, b) => String(a.code).localeCompare(String(b.code), undefined, { numeric: true })).map(item => {
      const req = byId(app.state.requirements, item.requirement_id);
      const child = item.parent_id ? " child" : "";
      return `<div class="wbs-item${child}"><span class="wbs-code">${escapeHtml(item.code)}</span><span class="wbs-name">${escapeHtml(item.name)}${req ? `<span class="cell-secondary">Linked: ${escapeHtml(req.code)} · ${escapeHtml(req.title)}</span>` : ""}</span><span class="wbs-type">${escapeHtml(item.item_type)}</span><div class="table-actions">${actionButton("Edit", "edit", "wbs", item.id, "button-small button-outline")}${actionButton("Delete", "delete", "wbs", item.id, "button-small button-danger")}</div></div>`;
    });
    const changes = app.state.changes.map(item => {
      const extras = item.status === "Pending" ? `<button class="table-action" data-action="approve-change" data-id="${item.id}">Approve &amp; apply</button><button class="table-action danger" data-action="reject-change" data-id="${item.id}">Reject</button>` : "";
      return `<tr><td>${escapeHtml(item.code)}</td><td><span class="cell-primary">${escapeHtml(item.title)}</span><span class="cell-secondary">${escapeHtml(item.justification)}</span></td><td>${escapeHtml(labelFor(app.state.requirements, item.requirement_id, "code"))}</td><td class="nowrap">${money(item.cost_impact)}</td><td>${Number(item.schedule_impact_days)} days</td><td>${badge(item.status)}${item.applied ? `<span class="cell-secondary">Applied to project</span>` : ""}</td><td><div class="table-actions">${extras}${rowActions("changes", item.id)}</div></td></tr>`;
    });
    const approvedReqs = app.state.requirements.filter(item => item.status === "Approved");
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
