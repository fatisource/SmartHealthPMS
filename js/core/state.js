import { seed } from "../data/seed.js";
import { STORE_KEY } from "../config.js";
import { clone } from "./utils.js";

export function validateState(value) {
  const keys = ["project", "plan", "stakeholders", "team", "requirements", "wbs", "changes", "activities", "milestones", "defects", "quality", "risks"];
  return value && value.schemaVersion === 1 && value.project && keys.every(key => Array.isArray(value[key]) || ["project", "plan"].includes(key));
}

export function loadState() {
  try {
    const saved = localStorage.getItem(STORE_KEY);
    if (!saved) {
      localStorage.setItem(STORE_KEY, JSON.stringify(seed));
      return clone(seed);
    }
    const parsed = JSON.parse(saved);
    return validateState(parsed) ? parsed : clone(seed);
  } catch (error) {
    console.error("Could not load browser data:", error);
    return clone(seed);
  }
}
