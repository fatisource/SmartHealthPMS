import { app } from "../core/context.js";
import { $, byId, escapeHtml, listOption, nextId } from "../core/utils.js";
const dialog = $("#record-dialog");

export const entityConfigs = {
    stakeholders: { title: "Stakeholder", fields: [
      { key: "name", label: "Name", required: true }, { key: "role", label: "Role", required: true }, { key: "interest", label: "Interest / expectations", type: "textarea", span: 2 }
    ] },
    team: { title: "Team member", fields: [
      { key: "name", label: "Name", required: true }, { key: "role", label: "Responsibility", required: true }
    ] },
    milestones: { title: "Milestone", fields: [
      { key: "name", label: "Milestone name", required: true, span: 2 }, { key: "target_date", label: "Target date", type: "date", required: true },
      { key: "status", label: "Status", type: "select", options: ["Pending", "Achieved", "Missed"] },
      { key: "responsible_id", label: "Responsible team member", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "activity_id", label: "Linked activity", type: "ref", ref: "activities", optionLabel: item => `${item.code} · ${item.name}` }
    ] },
    requirements: { title: "Requirement", fields: [
      { key: "code", label: "Requirement code", required: true }, { key: "priority", label: "Priority", type: "select", options: ["Critical", "High", "Medium", "Low"] },
      { key: "title", label: "Title", required: true, span: 2 }, { key: "description", label: "Description", type: "textarea", span: 2 },
      { key: "status", label: "Approval status", type: "select", options: ["Proposed", "Approved", "Rejected"] }
    ] },
    wbs: { title: "WBS item", fields: [
      { key: "code", label: "WBS code", required: true }, { key: "item_type", label: "Type", type: "select", options: ["Deliverable", "Work Package"] },
      { key: "name", label: "Name", required: true, span: 2 }, { key: "parent_id", label: "Parent item", type: "ref", ref: "wbs", optionLabel: item => `${item.code} · ${item.name}`, empty: "Top-level item" },
      { key: "requirement_id", label: "Linked approved requirement", type: "ref", ref: "approvedRequirements", optionLabel: item => `${item.code} · ${item.title}`, empty: "No linked requirement" }
    ] },
    changes: { title: "Change request", fields: [
      { key: "code", label: "Request code", required: true }, { key: "requirement_id", label: "Linked requirement", type: "ref", ref: "requirements", optionLabel: item => `${item.code} · ${item.title}` },
      { key: "title", label: "Request title", required: true, span: 2 }, { key: "justification", label: "Business justification", type: "textarea", required: true, span: 2 },
      { key: "cost_impact", label: "Estimated cost impact (PKR)", type: "number", min: 0, step: 1000, required: true },
      { key: "schedule_impact_days", label: "Schedule impact (days)", type: "number", min: 0, step: 1, required: true },
      { key: "requested_date", label: "Requested date", type: "date", required: true }
    ] },
    activities: { title: "Activity", fields: [
      { key: "code", label: "Activity code", required: true }, { key: "name", label: "Activity name", required: true },
      { key: "wbs_id", label: "WBS work package", type: "ref", ref: "workPackages", optionLabel: item => `${item.code} · ${item.name}` },
      { key: "assignee_id", label: "Assigned team member", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "requirement_id", label: "Approved requirement", type: "ref", ref: "approvedRequirements", optionLabel: item => `${item.code} · ${item.title}` },
      { key: "duration_days", label: "Duration (days)", type: "number", min: 1, step: 1, required: true },
      { key: "percent_complete", label: "Progress (%)", type: "number", min: 0, max: 100, step: 1, required: true },
      { key: "status", label: "Status", type: "select", options: ["Not Started", "In Progress", "Completed"] },
      { key: "planned_cost", label: "Planned cost (PKR)", type: "number", min: 0, step: 1000, required: true },
      { key: "actual_cost", label: "Actual cost (PKR)", type: "number", min: 0, step: 1000, required: true },
      { key: "baseline_start", label: "Baseline start", type: "date" }, { key: "baseline_end", label: "Baseline finish", type: "date" },
      { key: "predecessors", label: "Predecessor activities (Ctrl/Cmd-click to select multiple)", type: "multiRef", ref: "activities", optionLabel: item => `${item.code} · ${item.name}`, span: 2 }
    ] },
    defects: { title: "Defect", fields: [
      { key: "code", label: "Defect code", required: true }, { key: "severity", label: "Severity", type: "select", options: ["Critical", "High", "Medium", "Low"] },
      { key: "title", label: "Defect summary", required: true, span: 2 }, { key: "status", label: "Resolution status", type: "select", options: ["Open", "In Progress", "Resolved", "Closed"] },
      { key: "assigned_to", label: "Assigned team member", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "activity_id", label: "Related activity", type: "ref", ref: "activities", optionLabel: item => `${item.code} · ${item.name}` },
      { key: "requirement_id", label: "Related approved requirement", type: "ref", ref: "approvedRequirements", optionLabel: item => `${item.code} · ${item.title}` },
      { key: "reported_date", label: "Reported date", type: "date", required: true }, { key: "resolved_date", label: "Resolved date", type: "date" }
    ] },
    quality: { title: "Quality inspection", fields: [
      { key: "wbs_id", label: "WBS item", type: "ref", ref: "wbs", optionLabel: item => `${item.code} · ${item.name}` },
      { key: "result", label: "Inspection result", type: "select", options: ["Pending", "Pass", "Fail"] },
      { key: "item", label: "Checklist / inspection item", required: true, span: 2 },
      { key: "inspector_id", label: "Inspector", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "inspection_date", label: "Inspection date", type: "date" },
      { key: "notes", label: "Findings / notes", type: "textarea", span: 2 }
    ] },
    risks: { title: "Project risk", fields: [
      { key: "code", label: "Risk code", required: true }, { key: "status", label: "Status", type: "select", options: ["Open", "Mitigated", "Occurred", "Closed"] },
      { key: "description", label: "Risk description", required: true, span: 2 },
      { key: "likelihood", label: "Likelihood (1–5)", type: "number", min: 1, max: 5, step: 1, required: true },
      { key: "impact", label: "Impact (1–5)", type: "number", min: 1, max: 5, step: 1, required: true },
      { key: "owner_id", label: "Risk owner", type: "ref", ref: "team", optionLabel: item => `${item.name} · ${item.role}` },
      { key: "preventive_action", label: "Preventive action", type: "textarea", span: 2 },
      { key: "corrective_action", label: "Corrective action", type: "textarea", span: 2 }
    ] }
  };

export function resolveOptions(ref) {
    if (ref === "approvedRequirements") return app.state.requirements.filter(item => item.status === "Approved");
    if (ref === "workPackages") return app.state.wbs.filter(item => item.item_type === "Work Package");
    return app.state[ref] || [];
  }

export function openRecordDialog(entity, record = null) {
    const config = entityConfigs[entity];
    if (!config) return;
    app.editingRecord = { entity, id: record?.id ?? null };
    $("#dialog-eyebrow").textContent = entity.toUpperCase().replace(/S$/, "") + " RECORD";
    $("#dialog-title").textContent = `${record ? "Edit" : "Add"} ${config.title.toLowerCase()}`;
    $("#dialog-fields").innerHTML = config.fields.map(item => {
      const value = record?.[item.key] ?? "";
      const span = item.span === 2 ? " span-2" : "";
      const required = item.required ? "required" : "";
      let control = "";
      if (item.type === "textarea") control = `<textarea name="${item.key}" ${required}>${escapeHtml(value)}</textarea>`;
      else if (item.type === "select") control = `<select name="${item.key}" ${required}>${item.options.map(option => `<option value="${escapeHtml(option)}" ${String(option) === String(value) ? "selected" : ""}>${escapeHtml(option)}</option>`).join("")}</select>`;
      else if (item.type === "ref") {
        const records = resolveOptions(item.ref);
        control = `<select name="${item.key}">${listOption(records, "id", item.optionLabel, value, item.empty || "Not assigned")}</select>`;
      } else if (item.type === "multiRef") {
        const ids = (value || []).map(Number);
        control = `<select name="${item.key}" multiple>${resolveOptions(item.ref).filter(option => Number(option.id) !== Number(record?.id)).map(option => `<option value="${option.id}" ${ids.includes(Number(option.id)) ? "selected" : ""}>${escapeHtml(item.optionLabel(option))}</option>`).join("")}</select><small class="dialog-hint">Use Ctrl (Windows) or Command (Mac) to select more than one predecessor.</small>`;
      } else control = `<input name="${item.key}" type="${item.type || "text"}" value="${escapeHtml(value)}" ${required} ${item.min !== undefined ? `min="${item.min}"` : ""} ${item.max !== undefined ? `max="${item.max}"` : ""} ${item.step !== undefined ? `step="${item.step}"` : ""}>`;
      return `<div class="dialog-field${span}"><label>${escapeHtml(item.label)}</label>${control}</div>`;
    }).join("");
    dialog.showModal();
  }

export function saveDialog(form) {
    if (!app.editingRecord) return;
    const { entity, id } = app.editingRecord;
    const config = entityConfigs[entity];
    const records = app.state[entity];
    const old = id === null ? null : byId(records, id);
    const record = old ? { ...old } : { id: nextId(records) };
    for (const fieldConfig of config.fields) {
      const control = form.elements[fieldConfig.key];
      if (!control) continue;
      if (fieldConfig.type === "multiRef") {
        record[fieldConfig.key] = Array.from(control.selectedOptions).map(option => Number(option.value));
      } else if (fieldConfig.type === "ref") {
        record[fieldConfig.key] = control.value ? Number(control.value) : null;
      } else if (fieldConfig.type === "number") {
        record[fieldConfig.key] = control.value === "" ? 0 : Number(control.value);
        if (!Number.isFinite(record[fieldConfig.key])) throw new Error(`${fieldConfig.label} must be a valid number.`);
      } else {
        record[fieldConfig.key] = control.value.trim();
      }
    }
    if (entity === "requirements" && record.status !== "Approved" && app.state.wbs.some(item => Number(item.requirement_id) === Number(record.id))) {
      if (!confirm("This requirement is linked to one or more WBS items. Change its status anyway?")) throw new Error("Requirement status change was cancelled.");
    }
    if (entity === "activities") {
      if (record.percent_complete >= 100) record.status = "Completed";
      else if (record.percent_complete > 0 && record.status === "Not Started") record.status = "In Progress";
      if (record.baseline_start && record.baseline_end && record.baseline_end < record.baseline_start) throw new Error("Baseline finish cannot be before baseline start.");
      if ((record.predecessors || []).includes(Number(record.id))) throw new Error("An activity cannot depend on itself.");
    }
    if (entity === "defects") {
      if (["Resolved", "Closed"].includes(record.status) && !record.resolved_date) throw new Error("Enter a resolved date for a resolved or closed defect.");
      if (!["Resolved", "Closed"].includes(record.status)) record.resolved_date = "";
    }
    if (entity === "changes") {
      record.status = old?.status || "Pending";
      record.applied = Boolean(old?.applied);
      record.decision_date = old?.decision_date || "";
    }
    if (old) records[records.indexOf(old)] = record;
    else records.push(record);
    if (entity === "plan") { /* project plan is edited through its own form */ }
    app.editingRecord = null;
    dialog.close();
    app.persist(`${config.title} ${old ? "updated" : "added"}.`);
  }

export function deleteRecord(entity, id) {
    const record = byId(app.state[entity], id);
    if (!record) return;
    if (entity === "requirements" && (
      app.state.changes.some(change => Number(change.requirement_id) === Number(id)) ||
      app.state.wbs.some(item => Number(item.requirement_id) === Number(id)) ||
      app.state.activities.some(item => Number(item.requirement_id) === Number(id)) ||
      app.state.defects.some(item => Number(item.requirement_id) === Number(id))
    )) {
      app.toast("This requirement is referenced by scope, a task, a defect, or a change request. Remove those links first.", true);
      return;
    }
    if (entity === "wbs" && (
      app.state.wbs.some(item => Number(item.parent_id) === Number(id)) ||
      app.state.activities.some(item => Number(item.wbs_id) === Number(id)) ||
      app.state.quality.some(item => Number(item.wbs_id) === Number(id))
    )) {
      app.toast("This WBS item has child items, activities, or inspections linked to it. Remove those links first.", true);
      return;
    }
    if (entity === "activities" && app.state.activities.some(item => (item.predecessors || []).map(Number).includes(Number(id)))) {
      app.toast("Other activities depend on this activity. Remove those dependencies first.", true);
      return;
    }
    if (entity === "activities" && (
      app.state.milestones.some(item => Number(item.activity_id) === Number(id)) ||
      app.state.defects.some(item => Number(item.activity_id) === Number(id))
    )) {
      app.toast("This activity is linked to a milestone or defect. Update those records first.", true);
      return;
    }
    if (entity === "changes" && record.applied) {
      app.toast("Applied change requests are retained as project decision history.", true);
      return;
    }
    if (!confirm(`Delete ${record.code || record.name || record.title || "this record"}? This cannot be undone except by importing a backup.`)) return;
    app.state[entity] = app.state[entity].filter(item => Number(item.id) !== Number(id));
    if (entity === "team") {
      app.state.milestones.forEach(item => { if (Number(item.responsible_id) === Number(id)) item.responsible_id = null; });
      app.state.defects.forEach(item => { if (Number(item.assigned_to) === Number(id)) item.assigned_to = null; });
      app.state.risks.forEach(item => { if (Number(item.owner_id) === Number(id)) item.owner_id = null; });
    }
    if (entity === "wbs") app.state.quality.forEach(item => { if (Number(item.wbs_id) === Number(id)) item.wbs_id = null; });
    app.persist("Record deleted.");
  }
