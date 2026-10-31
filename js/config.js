export const STORE_KEY = "careplan.workspace.v1";
export const TODAY = new Date().toISOString().slice(0, 10);

export const pages = {
    dashboard: ["Overview", "Project dashboard and priority follow-up"],
    initiation: ["Initiation & integration", "Maintain the charter, project plan, stakeholders, team, and milestones."],
    scope: ["Scope & WBS", "Control requirements, approved scope, work packages, and change requests."],
    schedule: ["Schedule & resources", "Plan activity dependencies, generate dates, and monitor delays."],
    cost: ["Cost & EVM", "Monitor planned value, earned value, actual cost, and project performance."],
    quality: ["Quality & risk", "Manage defects, inspections, risk priorities, and response plans."]
  };
