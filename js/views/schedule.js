import { app } from "../core/context.js";
import { pages } from "../config.js";
import { escapeHtml, labelFor, money, pct, prettyDate, safeDate } from "../core/utils.js";
import { getDelayRows, getEvm, getSchedule } from "../core/calculations.js";
import { actionButton, badge, heading, metric, panel, rowActions, table } from "../ui/components.js";

export function renderSchedule() {
    const schedule = getSchedule();
    const activityRows = app.state.activities.map(activity => {
      const delay = getDelayRows().find(row => Number(row.activity.id) === Number(activity.id));
      return `<tr><td><span class="cell-primary">${escapeHtml(activity.code)}</span></td><td><span class="cell-primary">${escapeHtml(activity.name)}</span><span class="cell-secondary">${escapeHtml(labelFor(app.state.wbs, activity.wbs_id, "code"))} · ${escapeHtml(labelFor(app.state.team, activity.assignee_id))}</span></td><td>${Number(activity.duration_days)} days</td><td>${prettyDate(activity.start_date)}</td><td>${prettyDate(activity.end_date)}</td><td>${Number(activity.percent_complete)}%</td><td>${badge(delay ? "Delayed" : activity.status)}</td><td>${rowActions("activities", activity.id)}</td></tr>`;
    });
    const gantt = renderGantt();
    const delayRows = getDelayRows().map(({ activity, days }) => `<tr><td><span class="cell-primary">${escapeHtml(activity.code)} · ${escapeHtml(activity.name)}</span></td><td>${days} days after baseline end</td><td>${prettyDate(activity.baseline_end)}</td></tr>`);
    const finishNote = schedule.error ? `<div class="alert-item urgent"><span class="alert-marker"></span><div><strong>Schedule cannot be generated</strong><span>${escapeHtml(schedule.error)}</span></div></div>` : schedule.finish ? `<div class="alert-item ${schedule.overdue ? "urgent" : ""}"><span class="alert-marker"></span><div><strong>Forecast finish: ${prettyDate(schedule.finish)}</strong><span>Approved deadline: ${prettyDate(app.state.project.deadline)} · ${schedule.overdue ? "Forecast is late." : "Forecast is within the deadline."}</span></div></div>` : `<div class="no-alerts">Add activities to create a schedule forecast.</div>`;
    const actions = actionButton("Generate schedule", "generate-schedule") + actionButton("Add activity", "add", "activities", "", "button-outline");
    const completedMilestones = app.state.milestones.filter(item => item.status === "Achieved").length;
    const metricCards = [
      metric("ACTIVITIES", app.state.activities.length, "Defined project activities"),
      metric("DELAYED ACTIVITIES", delayRows.length, "Past baseline end", delayRows.length ? "warning" : "good"),
      metric("FORECAST FINISH", schedule.finish ? prettyDate(schedule.finish) : "—", schedule.overdue ? "After approved deadline" : "Approved deadline " + prettyDate(app.state.project.deadline), schedule.overdue ? "alert" : ""),
      metric("MILESTONES", completedMilestones + " / " + app.state.milestones.length, "Achieved to date")
    ].join("");
    return `${heading("schedule", actions)}
      <div class="metric-grid">${metricCards}</div>
      <div class="stack">
        ${panel("Schedule forecast", finishNote)}
        ${panel("Activity plan", table(["Code", "Activity / owner", "Duration", "Start", "Finish", "Progress", "Status", "Actions"], activityRows, "Add activities and assign project team members."), { subtitle: "Dependencies are used when generating the schedule.", flush: true })}
        ${panel("Schedule timeline", gantt, { subtitle: "Timeline based on planned activity dates" })}
        <div class="content-grid equal">${panel("Delay impact", table(["Delayed activity", "Delay", "Baseline end"], delayRows, "No incomplete activity is past its baseline end date."), { flush: true })}
        ${panel("Project milestones", table(["Milestone", "Target", "Status", "Owner"], app.state.milestones.map(item => `<tr><td>${escapeHtml(item.name)}</td><td>${prettyDate(item.target_date)}</td><td>${badge(item.status)}</td><td>${escapeHtml(labelFor(app.state.team, item.responsible_id))}</td></tr>`), "No milestones defined."), { flush: true })}</div>
      </div>`;
  }

export function renderGantt() {
    const items = app.state.activities.filter(item => safeDate(item.start_date) && safeDate(item.end_date));
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
