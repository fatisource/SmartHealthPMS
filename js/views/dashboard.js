import { app } from "../core/context.js";
import { pages } from "../config.js";
import { escapeHtml, labelFor, money, pct, prettyDate } from "../core/utils.js";
import { getDelayRows, getEvm, getSchedule } from "../core/calculations.js";
import { actionButton, badge, heading, metric, panel, rowActions, table } from "../ui/components.js";

export function renderDashboard() {
    const evm = getEvm();
    const schedule = getSchedule();
    const delays = getDelayRows();
    const pending = app.state.changes.filter(change => change.status === "Pending").length;
    const critical = app.state.defects.filter(defect => defect.severity === "Critical" && !["Resolved", "Closed"].includes(defect.status)).length;
    const activeRisks = app.state.risks.filter(risk => ["Open", "Occurred"].includes(risk.status)).length;
    const passed = app.state.quality.filter(item => item.result === "Pass").length;
    const alerts = [];
    if (critical) alerts.push(`<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>${critical} unresolved critical defect${critical === 1 ? "" : "s"}</strong><span>Review in Quality &amp; Risk before project sign-off.</span></div></div>`);
    if (pending) alerts.push(`<div class="alert-item"><span class="alert-marker"></span><div><strong>${pending} change request${pending === 1 ? "" : "s"} awaiting decision</strong><span>Approval changes the linked scope, budget, and deadline.</span></div></div>`);
    if (delays.length) alerts.push(`<div class="alert-item"><span class="alert-marker"></span><div><strong>${delays.length} activity${delays.length === 1 ? "" : "ies"} past baseline</strong><span>Open Schedule to review the delay and downstream impact.</span></div></div>`);
    if (schedule.finish && schedule.overdue) alerts.push(`<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>Forecast finish is after the approved deadline</strong><span>Forecast ${prettyDate(schedule.finish)} · deadline ${prettyDate(app.state.project.deadline)}.</span></div></div>`);
    if (evm.bac && evm.ac > evm.bac) alerts.push(`<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>Actual expenditure exceeds approved budget</strong><span>${money(evm.ac)} spent against ${money(evm.bac)} approved.</span></div></div>`);
    const alertBody = alerts.length ? `<div class="alert-list">${alerts.join("")}</div>` : `<div class="no-alerts">No urgent follow-up flags from the current records.</div>`;
    const qualityProgress = `<div class="progress-track"><div class="progress-bar ${passed < app.state.quality.length ? "warning" : ""}" style="width:${app.state.quality.length ? passed / app.state.quality.length * 100 : 0}%"></div></div><div class="progress-caption"><span>${passed} passed</span><span>${app.state.quality.length} checks</span></div>`;
    const activityRows = app.state.activities.slice().sort((a, b) => Number(a.percent_complete) - Number(b.percent_complete)).slice(0, 5).map(activity => {
      const progress = Math.min(100, Math.max(0, Number(activity.percent_complete) || 0));
      return `<tr><td><span class="cell-primary">${escapeHtml(activity.name)}</span><span class="cell-secondary">${escapeHtml(activity.code)}</span></td><td>${badge(activity.status)}</td><td><div class="progress-track"><div class="progress-bar ${progress < 35 && activity.status === "In Progress" ? "warning" : ""}" style="width:${progress}%"></div></div><div class="progress-caption"><span>${progress}%</span><span>${escapeHtml(labelFor(app.state.team, activity.assignee_id))}</span></div></td></tr>`;
    });
    const delayRows = delays.map(({ activity, days }) => `<tr><td><span class="cell-primary">${escapeHtml(activity.code)} · ${escapeHtml(activity.name)}</span></td><td>${days} day${days === 1 ? "" : "s"}</td><td>${prettyDate(activity.baseline_end)}</td></tr>`);
    const summary = `<div class="hero-panel"><div class="hero-panel-content"><p class="eyebrow">PROJECT OPERATIONS · ${escapeHtml(app.state.project.status || "Initiated").toUpperCase()}</p><h2>${escapeHtml(app.state.project.name)}</h2><p>Monitor the delivery plan for five hospitals—from approved scope and schedule through cost performance, testing, and deployment.</p></div><div class="hero-panel-meta"><span>Reporting date</span><strong>${prettyDate(app.state.project.status_date)}</strong><span style="margin-top:11px">Approved deadline</span><strong>${prettyDate(app.state.project.deadline)}</strong></div></div>`;
    return `${heading("dashboard", actionButton("Edit project charter", "edit-project"))}${summary}<div class="metric-grid">
      ${metric("ACTIVITY PROGRESS", `${app.state.activities.filter(item => item.status === "Completed").length} / ${app.state.activities.length}`, `${pct(app.state.activities.filter(item => item.status === "Completed").length, app.state.activities.length)} complete`)}
      ${metric("APPROVED SCOPE", `${app.state.requirements.filter(item => item.status === "Approved").length} / ${app.state.requirements.length}`, "Requirements formally approved")}
      ${metric("OPEN DEFECTS", app.state.defects.filter(item => !["Resolved", "Closed"].includes(item.status)).length, `${critical} critical require priority review`, critical ? "alert" : "")}
      ${metric("ACTIVE RISKS", activeRisks, "Open or occurred risks", activeRisks ? "warning" : "good")}
      ${metric("QUALITY PASS RATE", pct(passed, app.state.quality.length), `${passed} of ${app.state.quality.length} checks passed`, passed < app.state.quality.length ? "warning" : "good")}
      ${metric("PENDING CHANGES", pending, "Awaiting approval or rejection", pending ? "warning" : "good")}
      ${metric("COST PERFORMANCE", evm.cpi.toFixed(2), `CPI · threshold ${Number(app.state.project.cost_threshold).toFixed(2)}`, evm.cpi && evm.cpi < Number(app.state.project.cost_threshold) ? "alert" : "")}
      ${metric("SCHEDULE PERFORMANCE", evm.spi.toFixed(2), `SPI · threshold ${Number(app.state.project.schedule_threshold).toFixed(2)}`, evm.spi && evm.spi < Number(app.state.project.schedule_threshold) ? "alert" : "")}
    </div><div class="content-grid">
      <div class="stack">${panel("Priority follow-up", alertBody, { subtitle: "Items needing the project manager's attention" })}
      ${panel("Activity progress", table(["Activity", "Status", "Completion"], activityRows, "Add activities in the Schedule module."), { subtitle: "A quick view of active delivery work", flush: true })}</div>
      <div class="stack">${panel("Readiness snapshot", `<div class="stack"><div class="meter-line"><span>Approved requirements</span><strong>${app.state.requirements.filter(item => item.status === "Approved").length} / ${app.state.requirements.length}</strong></div><div class="progress-track"><div class="progress-bar" style="width:${pct(app.state.requirements.filter(item => item.status === "Approved").length, app.state.requirements.length)}"></div></div><div class="meter-line"><span>Quality checks passed</span><strong>${passed} / ${app.state.quality.length}</strong></div>${qualityProgress}<div class="meter-line"><span>Milestones achieved</span><strong>${app.state.milestones.filter(item => item.status === "Achieved").length} / ${app.state.milestones.length}</strong></div></div>`, { subtitle: "Current completion indicators" })}
      ${panel("Delayed activities", table(["Activity", "Delay", "Baseline end"], delayRows.slice(0, 5), "No incomplete activity is past its baseline end date."), { flush: true })}</div>
    </div>`;
  }
