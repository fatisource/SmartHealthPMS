import { pages } from "./config.js";
import { app } from "./core/context.js";
import { loadState } from "./core/state.js";
import { persist } from "./core/storage.js";
import { $ } from "./core/utils.js";
import { initializeEvents } from "./events.js";
import { toast } from "./ui/notifications.js";
import { renderDashboard } from "./views/dashboard.js";
import { renderInitiation } from "./views/initiation.js";
import { renderScope } from "./views/scope.js";
import { renderSchedule } from "./views/schedule.js";
import { renderCost } from "./views/cost.js";
import { renderQuality } from "./views/quality.js";

app.state = loadState();
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

initializeEvents();
render();
