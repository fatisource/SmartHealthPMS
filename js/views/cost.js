import { app } from "../core/context.js";
import { pages } from "../config.js";
import { escapeHtml, labelFor, money, pct, prettyDate } from "../core/utils.js";
import { getDelayRows, getEvm, getSchedule } from "../core/calculations.js";
import { actionButton, badge, heading, metric, panel, rowActions, table } from "../ui/components.js";

export function renderCost() {
    const report = getEvm();
    const costLimit = Number(app.state.project.cost_threshold) || .9;
    const scheduleLimit = Number(app.state.project.schedule_threshold) || .9;
    const costWarn = report.cpi > 0 && report.cpi < costLimit;
    const scheduleWarn = report.spi > 0 && report.spi < scheduleLimit;
    const warnings = [];
    if (report.ac > report.bac) warnings.push(`<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>Actual cost is above the approved project budget</strong><span>${money(report.ac)} actual against ${money(report.bac)} approved.</span></div></div>`);
    if (costWarn) warnings.push(`<div class="alert-item"><span class="alert-marker"></span><div><strong>Cost performance is below threshold</strong><span>CPI ${report.cpi.toFixed(2)} · threshold ${costLimit.toFixed(2)}.</span></div></div>`);
    if (scheduleWarn) warnings.push(`<div class="alert-item"><span class="alert-marker"></span><div><strong>Schedule performance is below threshold</strong><span>SPI ${report.spi.toFixed(2)} · threshold ${scheduleLimit.toFixed(2)}.</span></div></div>`);
    const activityRows = app.state.activities.map(item => {
      const planned = Number(item.planned_cost) || 0;
      const earned = planned * Math.min(100, Math.max(0, Number(item.percent_complete) || 0)) / 100;
      const actual = Number(item.actual_cost) || 0;
      return `<tr><td>${escapeHtml(item.code)}</td><td>${escapeHtml(item.name)}</td><td class="nowrap">${money(planned)}</td><td class="nowrap">${money(actual)}</td><td class="nowrap">${money(earned)}</td><td class="nowrap">${money(earned - actual)}</td><td>${Number(item.percent_complete)}%</td><td><button class="table-action" data-action="edit" data-entity="activities" data-id="${item.id}">Update costs</button></td></tr>`;
    });
    const metricRows = [
      ["Budget at Completion (BAC)", money(report.bac), "Approved project budget"],
      ["Planned Value (PV)", money(report.pv), `Planned budgeted work by ${prettyDate(app.state.project.status_date)}`],
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
      ${panel("Earned value indicators", table(["Measure", "Value", "Interpretation"], metricRows), { subtitle: `Status date: ${prettyDate(app.state.project.status_date)}`, flush: true })}
      ${panel("Activity cost tracking", table(["Code", "Activity", "Planned cost", "Actual cost", "Earned value", "Cost variance", "Progress", "Actions"], activityRows, "Add activities in the Schedule module."), { flush: true })}
    </div>`;
  }
