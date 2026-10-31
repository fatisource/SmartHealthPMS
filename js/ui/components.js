import { pages } from "../config.js";
import { app } from "../core/context.js";
import { escapeHtml, prettyDate } from "../core/utils.js";

export function heading(page = app.currentPage, actions = "") {
    return `<div class="page-heading"><div><p class="eyebrow">CAREPLAN WORKSPACE</p><h1>${escapeHtml(pages[page][0])}</h1><p>${escapeHtml(pages[page][1])}</p></div><div class="heading-actions">${actions}</div></div>`;
  }

export function metric(label, value, detail, tone = "") {
    return `<article class="metric-card ${tone}"><div class="metric-label">${escapeHtml(label)}</div><div class="metric-value">${escapeHtml(value)}</div><div class="metric-detail">${escapeHtml(detail)}</div></article>`;
  }

export function panel(title, body, options = {}) {
    const subtitle = options.subtitle ? `<p class="panel-subtitle">${escapeHtml(options.subtitle)}</p>` : "";
    const actions = options.actions || "";
    return `<section class="panel"><div class="panel-header"><div><h2 class="panel-title">${escapeHtml(title)}</h2>${subtitle}</div>${actions}</div><div class="panel-body ${options.flush ? "flush" : ""}">${body}</div></section>`;
  }

export function badge(value) {
    const kind = String(value || "").toLowerCase().replace(/\s+/g, "-");
    const labels = { "in-progress": "In progress", "not-started": "Not started" };
    return `<span class="badge ${escapeHtml(kind)}">${escapeHtml(labels[kind] || value || "—")}</span>`;
  }

export function table(headers, rows, emptyText = "No records yet.") {
    if (!rows.length) return `<div class="empty-state"><strong>Nothing to show yet</strong>${escapeHtml(emptyText)}</div>`;
    return `<div class="table-wrap"><table><thead><tr>${headers.map(header => `<th>${escapeHtml(header)}</th>`).join("")}</tr></thead><tbody>${rows.join("")}</tbody></table></div>`;
  }

export function rowActions(entity, id, extras = "") {
    return `<div class="table-actions">${extras}<button class="table-action" data-action="edit" data-entity="${entity}" data-id="${id}">Edit</button><button class="table-action danger" data-action="delete" data-entity="${entity}" data-id="${id}">Delete</button></div>`;
  }

export function actionButton(label, action, entity = "", id = "", kind = "") {
    return `<button class="button ${kind}" data-action="${action}" ${entity ? `data-entity="${entity}"` : ""} ${id !== "" ? `data-id="${id}"` : ""}>${escapeHtml(label)}</button>`;
  }

export function displayDate(value) { return prettyDate(value); }

export function field(label, name, value = "", type = "text", options = {}) {
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
