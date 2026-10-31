export const $ = (selector, root = document) => root.querySelector(selector);
export const money = value => `PKR ${Number(value || 0).toLocaleString("en-PK", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
export const pct = (value, total) => total ? `${Math.round((value / total) * 100)}%` : "0%";
export const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g, char => ({
  "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
})[char]);
export const safeDate = value => /^\d{4}-\d{2}-\d{2}$/.test(String(value || "")) ? value : "";
export const prettyDate = value => safeDate(value) ? new Date(`${value}T00:00:00`).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" }) : "?";
export const clone = value => JSON.parse(JSON.stringify(value));
export const nextId = records => Math.max(0, ...records.map(item => Number(item.id) || 0)) + 1;
export const byId = (records, id) => records.find(item => Number(item.id) === Number(id));
export const labelFor = (records, id, key = "name") => records.find(item => Number(item.id) === Number(id))?.[key] || "?";
export function listOption(records, valueKey, labelFn, selected, emptyLabel = "Not assigned") {
  const options = records.map(item => `<option value="${escapeHtml(item[valueKey])}" ${String(item[valueKey]) === String(selected ?? "") ? "selected" : ""}>${escapeHtml(labelFn(item))}</option>`).join("");
  return `<option value="">${escapeHtml(emptyLabel)}</option>${options}`;
}
