import { app } from "./context.js";
import { saveWorkspace } from "./api.js";

let pendingWrite = Promise.resolve();

export function persist(message = "Changes saved to the MySQL database.") {
  const snapshot = JSON.parse(JSON.stringify(app.state));
  pendingWrite = pendingWrite.catch(() => {}).then(async () => {
    await saveWorkspace(snapshot);
    app.render();
    app.toast(message);
    return true;
  }).catch(error => {
    console.error("Could not save workspace to MySQL:", error);
    app.toast(`Database save failed. This change is not persisted: ${error.message}`, true);
    return false;
  });
  return pendingWrite;
}
