import { TODAY } from "../config.js";
import { app } from "../core/context.js";
import { validateState } from "../core/state.js";

export function exportBackup() {
    const data = new Blob([JSON.stringify({ ...app.state, exportedAt: new Date().toISOString() }, null, 2)], { type: "application/json" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(data);
    link.download = `careplan-backup-${TODAY}.json`;
    link.click();
    URL.revokeObjectURL(link.href);
    app.toast("Backup exported.");
  }

export function importBackup(file) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      try {
        const parsed = JSON.parse(reader.result);
        if (!validateState(parsed)) throw new Error("This file is not a valid CarePlan version 1 backup.");
        if (!confirm("Importing replaces all current data in this browser. Export a backup first if needed. Continue?")) return;
        app.state = parsed;
        if (!app.persist("Backup imported.")) return;
      } catch (error) {
        app.toast(error.message || "Could not read the backup file.", true);
      }
    };
    reader.onerror = () => app.toast("Could not read the selected backup file.", true);
    reader.readAsText(file);
  }
