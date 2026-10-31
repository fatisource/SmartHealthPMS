export function validateState(value) {
  const keys = ["project", "plan", "stakeholders", "team", "requirements", "wbs", "changes", "activities", "milestones", "defects", "quality", "risks"];
  return value && value.schemaVersion === 1 && value.project && keys.every(key => Array.isArray(value[key]) || ["project", "plan"].includes(key));
}
