import { STORE_KEY } from "../config.js";
import { app } from "./context.js";

export function persist(message = "Changes saved on this browser.") {
  try {
    localStorage.setItem(STORE_KEY, JSON.stringify(app.state));
    app.render();
    app.toast(message);
    return true;
  } catch (error) {
    console.error("Could not save browser data:", error);
    app.toast("Could not save. Browser storage may be full or disabled.", true);
    return false;
  }
}
