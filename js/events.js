import { TODAY } from "./config.js";
import { app } from "./core/context.js";
import { $, byId } from "./core/utils.js";
import { deleteRecord, openRecordDialog, saveDialog } from "./features/records.js";
import { closeProject, decideChange, editProjectDialog, generateSchedule, submitProject } from "./features/project.js";
import { exportBackup, importBackup } from "./features/backup.js";

export function handleClick(event) {
    const navigation = event.target.closest("[data-page]");
    if (navigation && navigation.classList.contains("nav-link")) {
      app.currentPage = navigation.dataset.page;
      $("#sidebar").classList.remove("open");
      app.render();
      return;
    }
    const button = event.target.closest("[data-action]");
    if (!button) return;
    const { action, entity, id } = button.dataset;
    if (action === "add") openRecordDialog(entity);
    else if (action === "edit") openRecordDialog(entity, byId(app.state[entity], id));
    else if (action === "delete") deleteRecord(entity, id);
    else if (action === "edit-project") editProjectDialog();
    else if (action === "approve-change") decideChange(id, true);
    else if (action === "reject-change") decideChange(id, false);
    else if (action === "generate-schedule") generateSchedule();
    else if (action === "close-project") closeProject();
  }

export function handlePageSubmit(event) {
    if (event.target.id === "project-form") {
      event.preventDefault();
      try {
        const formData = new FormData(event.target);
        app.state.project.name = formData.get("name").trim();
        app.state.project.objectives = formData.get("objectives").trim();
        app.state.project.scope_boundaries = formData.get("scope_boundaries").trim();
        app.state.project.approved_budget = Number(formData.get("approved_budget"));
        app.state.project.status = formData.get("status");
        app.state.project.start_date = formData.get("start_date");
        app.state.project.deadline = formData.get("deadline");
        app.state.project.status_date = formData.get("status_date");
        app.state.project.cost_threshold = Number(formData.get("cost_threshold"));
        app.state.project.schedule_threshold = Number(formData.get("schedule_threshold"));
        if (!app.state.project.name || !app.state.project.start_date || !app.state.project.deadline) throw new Error("Complete the required project fields.");
        if (app.state.project.deadline <= app.state.project.start_date) throw new Error("Deadline must be after the start date.");
        if (app.state.project.approved_budget <= 0) throw new Error("Approved budget must be greater than zero.");
        if (!(app.state.project.cost_threshold > 0 && app.state.project.schedule_threshold > 0)) throw new Error("Performance thresholds must be greater than zero.");
        app.persist("Project charter saved.");
      } catch (error) { app.toast(error.message, true); }
    } else if (event.target.id === "plan-form") {
      event.preventDefault();
      const data = new FormData(event.target);
      for (const key of ["scope_summary", "schedule_summary", "cost_summary", "quality_summary"]) app.state.plan[key] = data.get(key).trim();
      app.state.plan.version = Number(app.state.plan.version || 1) + 1;
      app.state.plan.last_updated = TODAY;
      app.persist("Project plan saved.");
    }
  }

export function handleDialogSubmit(event) {
    if (event.target.id !== "record-form") return;
    event.preventDefault();
    try {
      if (!app.editingRecord) return;
      if (app.editingRecord.entity === "project") submitProject(event.target);
      else saveDialog(event.target);
    } catch (error) { app.toast(error.message || "Check the entered values.", true); }
  }

export function initializeEvents() {
  $("#page-container").addEventListener("click", handleClick);
  $("#sidebar").addEventListener("click", handleClick);
  $("#page-container").addEventListener("submit", handlePageSubmit);
  $("#record-form").addEventListener("submit", handleDialogSubmit);
  $("#cancel-dialog").addEventListener("click", () => { app.editingRecord = null; $("#record-dialog").close(); });
  $("#close-dialog").addEventListener("click", () => { app.editingRecord = null; $("#record-dialog").close(); });
  $("#mobile-menu").addEventListener("click", () => $("#sidebar").classList.toggle("open"));
  $("#export-data").addEventListener("click", exportBackup);
  $("#import-data").addEventListener("click", () => $("#import-file").click());
  $("#import-file").addEventListener("change", event => {
    importBackup(event.target.files[0]);
    event.target.value = "";
  });
}
