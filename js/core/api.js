async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(path, {
      ...options,
      headers: {
        Accept: "application/json",
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers
      }
    });
  } catch (error) {
    throw new Error("Cannot reach the PHP API. Start the XAMPP MySQL service and open CarePlan through the local PHP server.");
  }

  let payload;
  try {
    payload = await response.json();
  } catch {
    throw new Error("The PHP API did not return JSON. Confirm the app is running through XAMPP/PHP and api/state.php exists.");
  }
  if (!response.ok) throw new Error(payload.error || `Database request failed (${response.status}).`);
  return payload;
}

export async function loadWorkspace() {
  const result = await request("./api/state.php");
  return result.data;
}

export async function saveWorkspace(state) {
  await request("./api/state.php", {
    method: "PUT",
    body: JSON.stringify(state)
  });
}
