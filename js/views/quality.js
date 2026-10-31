import { app } from "../core/context.js";
import { pages } from "../config.js";
import { escapeHtml, labelFor, money, pct, prettyDate } from "../core/utils.js";
import { getDelayRows, getEvm, getSchedule } from "../core/calculations.js";
import { actionButton, badge, heading, metric, panel, rowActions, table } from "../ui/components.js";

export function renderQuality() {
    const defects = app.state.defects.map(item => `<tr class="${item.severity === "Critical" && !["Resolved", "Closed"].includes(item.status) ? "critical-row" : ""}"><td>${escapeHtml(item.code)}</td><td><span class="cell-primary">${escapeHtml(item.title)}</span><span class="cell-secondary">Reported ${prettyDate(item.reported_date)}</span></td><td>${badge(item.severity)}</td><td>${badge(item.status)}</td><td>${escapeHtml(labelFor(app.state.team, item.assigned_to))}</td><td>${prettyDate(item.resolved_date)}</td><td>${rowActions("defects", item.id)}</td></tr>`);
    const checks = app.state.quality.map(item => `<tr><td>${escapeHtml(labelFor(app.state.wbs, item.wbs_id, "code"))}</td><td>${escapeHtml(item.item)}</td><td>${badge(item.result)}</td><td>${escapeHtml(labelFor(app.state.team, item.inspector_id))}</td><td>${prettyDate(item.inspection_date)}</td><td>${rowActions("quality", item.id)}</td></tr>`);
    const risks = app.state.risks.slice().sort((a, b) => b.likelihood * b.impact - a.likelihood * a.impact).map(item => {
      const score = Number(item.likelihood) * Number(item.impact);
      return `<tr><td>${escapeHtml(item.code)}</td><td><span class="cell-primary">${escapeHtml(item.description)}</span></td><td>${item.likelihood}</td><td>${item.impact}</td><td class="risk-score">${score} · ${score >= 15 ? "High" : score >= 8 ? "Medium" : "Low"}</td><td>${escapeHtml(labelFor(app.state.team, item.owner_id))}</td><td>${badge(item.status)}</td><td>${rowActions("risks", item.id)}</td></tr>`;
    });
    const riskMatrix = renderRiskMatrix();
    const pdpc = app.state.risks.length ? `<div class="pdpc-list">${app.state.risks.map(item => `<div class="pdpc-row"><div class="pdpc-risk"><strong>${escapeHtml(item.code)} · ${escapeHtml(item.description)}</strong><br>Score ${Number(item.likelihood) * Number(item.impact)} / 25</div><div class="pdpc-arrow">→</div><div class="pdpc-response"><strong>Prevent:</strong> ${escapeHtml(item.preventive_action || "No preventive response recorded")}<br><strong>Respond:</strong> ${escapeHtml(item.corrective_action || "No corrective response recorded")}</div></div>`).join("")}</div>` : `<div class="empty-state">Add risks and their preventive/corrective responses to build the response view.</div>`;
    return `${heading("quality")}<div class="metric-grid">
      ${metric("OPEN DEFECTS", app.state.defects.filter(item => !["Resolved", "Closed"].includes(item.status)).length, "Unresolved items")}
      ${metric("CRITICAL DEFECTS", app.state.defects.filter(item => item.severity === "Critical" && !["Resolved", "Closed"].includes(item.status)).length, "Require priority review", app.state.defects.some(item => item.severity === "Critical" && !["Resolved", "Closed"].includes(item.status)) ? "alert" : "good")}
      ${metric("QUALITY PASS RATE", pct(app.state.quality.filter(item => item.result === "Pass").length, app.state.quality.length), `${app.state.quality.filter(item => item.result === "Pass").length} of ${app.state.quality.length} passed`)}
      ${metric("HIGH-PRIORITY RISKS", app.state.risks.filter(item => item.likelihood * item.impact >= 15 && !["Closed", "Mitigated"].includes(item.status)).length, "Score 15 or above", app.state.risks.some(item => item.likelihood * item.impact >= 15 && !["Closed", "Mitigated"].includes(item.status)) ? "warning" : "good")}
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

export function renderRiskMatrix() {
    const cells = [];
    cells.push(`<span class="matrix-label"></span>`);
    for (let impact = 5; impact >= 1; impact--) cells.push(`<span class="matrix-label">I${impact}</span>`);
    for (let likelihood = 5; likelihood >= 1; likelihood--) {
      cells.push(`<span class="matrix-label">L${likelihood}</span>`);
      for (let impact = 5; impact >= 1; impact--) {
        const score = likelihood * impact;
        const risks = app.state.risks.filter(item => Number(item.likelihood) === likelihood && Number(item.impact) === impact);
        const tone = score >= 15 ? "high-risk" : score >= 8 ? "medium-risk" : "low-risk";
        cells.push(`<div class="matrix-cell ${tone}" title="Likelihood ${likelihood}, impact ${impact}, score ${score}">${risks.map(item => `<span class="matrix-risk">${escapeHtml(item.code)}</span>`).join("")}</div>`);
      }
    }
    return `<div class="matrix">${cells.join("")}</div><div class="matrix-key"><span><i class="key-dot" style="background:#e7f3e7"></i>Low</span><span><i class="key-dot" style="background:#fff1cc"></i>Medium</span><span><i class="key-dot" style="background:#f9d9d4"></i>High</span></div>`;
  }
