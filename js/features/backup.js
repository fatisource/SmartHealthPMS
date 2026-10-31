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

export async function importBackup(file) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = async () => {
      try {
        const parsed = JSON.parse(reader.result);
        if (!validateState(parsed)) throw new Error("This file is not a valid CarePlan version 1 backup.");
        if (!confirm("Importing replaces the project data in MySQL. Export a backup first if needed. Continue?")) return;
        const previousState = app.state;
        app.state = parsed;
        if (!await app.persist("Backup imported to MySQL.")) {
          app.state = previousState;
          app.render();
        }
      } catch (error) {
        app.toast(error.message || "Could not read the backup file.", true);
      }
    };
    reader.onerror = () => app.toast("Could not read the selected backup file.", true);
    reader.readAsText(file);
  }
