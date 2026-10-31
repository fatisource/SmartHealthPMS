import { pages, STORE_KEY } from "./config.js";
import { app } from "./core/context.js";
import { validateState } from "./core/state.js";
import { persist } from "./core/storage.js";
import { clone, $ } from "./core/utils.js";
import { loadWorkspace, saveWorkspace } from "./core/api.js";
import { seed } from "./data/seed.js";
import { initializeEvents } from "./events.js";
import { toast } from "./ui/notifications.js";
import { renderDashboard } from "./views/dashboard.js";
import { renderInitiation } from "./views/initiation.js";
import { renderScope } from "./views/scope.js";
import { renderSchedule } from "./views/schedule.js";
import { renderCost } from "./views/cost.js";
import { renderQuality } from "./views/quality.js";

app.render = render;
app.persist = persist;
app.toast = toast;

function render() {
  const currentPage = pages[app.currentPage] ? app.currentPage : "dashboard";
  app.currentPage = currentPage;
  const [title] = pages[currentPage];
  $("#today-label").textContent = new Date().toLocaleDateString(undefined, { weekday: "short", month: "short", day: "numeric" });
  document.title = `CarePlan | ${title}`;
  $("#current-section").textContent = title;
  document.querySelectorAll(".nav-link").forEach(button => button.classList.toggle("active", button.dataset.page === currentPage));
  const renderers = { dashboard: renderDashboard, initiation: renderInitiation, scope: renderScope, schedule: renderSchedule, cost: renderCost, quality: renderQuality };
  $("#page-container").innerHTML = renderers[currentPage]();
}

async function start() {
  try {
    app.state = await loadWorkspace();
    if (!app.state) {
      const legacyData = localStorage.getItem(STORE_KEY);
      let migratedBrowserData = false;
      if (legacyData) {
        try {
          const parsed = JSON.parse(legacyData);
          migratedBrowserData = validateState(parsed);
          app.state = migratedBrowserData ? parsed : clone(seed);
        } catch {
          app.state = clone(seed);
        }
      } else {
        app.state = clone(seed);
      }
      await saveWorkspace(app.state);
      if (migratedBrowserData) {
        localStorage.removeItem(STORE_KEY);
        toast("Existing browser data has been moved into MySQL.");
      } else {
        toast("Case-study sample data initialized in MySQL.");
      }
    }
    $("#database-status").textContent = "Connected to MySQL";
    initializeEvents();
    render();
  } catch (error) {
    console.error("Could not start CarePlan:", error);
    $("#database-status").textContent = "MySQL disconnected";
    $("#page-container").innerHTML = `<section class="panel"><div class="panel-body"><h1>Database connection required</h1><p>${error.message}</p><p>Import <code>database/schema.sql</code> into the <code>spms_db</code> database with phpMyAdmin, then check the XAMPP MySQL service and credentials in <code>api/config.php</code>.</p></div></section>`;
  }
}

start();
