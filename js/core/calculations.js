import { TODAY } from "../config.js";
import { app } from "./context.js";
import { byId, safeDate } from "./utils.js";

export function getSchedule() {
    const activities = app.state.activities;
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
    const projectStart = safeDate(app.state.project.start_date);
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
    return { order, dates, finish, deadline: app.state.project.deadline, overdue: Boolean(finish && app.state.project.deadline && finish > app.state.project.deadline) };
  }

export function addDays(date, amount) {
    if (!safeDate(date)) return "";
    const value = new Date(`${date}T00:00:00Z`);
    value.setUTCDate(value.getUTCDate() + Number(amount));
    return value.toISOString().slice(0, 10);
  }

export function getEvm() {
    const statusDate = safeDate(app.state.project.status_date) || TODAY;
    let pv = 0, ev = 0, ac = 0;
    app.state.activities.forEach(activity => {
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
      bac: Number(app.state.project.approved_budget) || 0, pv, ev, ac, cpi, spi,
      cv: ev - ac, sv: ev - pv,
      eac: cpi > 0 ? (Number(app.state.project.approved_budget) || 0) / cpi : Number(app.state.project.approved_budget) || 0
    };
  }

export function getDelayRows() {
    const statusDate = safeDate(app.state.project.status_date) || TODAY;
    return app.state.activities.filter(activity => {
      const baselineEnd = safeDate(activity.baseline_end);
      return Number(activity.percent_complete) < 100 && baselineEnd && baselineEnd < statusDate;
    }).map(activity => ({
      activity,
      days: Math.max(1, Math.round((Date.parse(`${statusDate}T00:00:00Z`) - Date.parse(`${activity.baseline_end}T00:00:00Z`)) / 86400000))
    }));
  }
