# CarePlan

CarePlan is a static, browser-based project management workspace for the Smart Healthcare Project Management System case study. It uses plain HTML, CSS, and JavaScript; no build step, server, or database setup is required to deploy the web app.

## Run locally

Open `index.html` in a modern browser. The app starts with sample project data the first time it is opened.

## Deploy with GitHub Pages

1. Push the repository to GitHub.
2. Open the repository's **Settings → Pages**.
3. Under **Build and deployment**, choose **Deploy from a branch**.
4. Select the `main` branch and `/ (root)` folder, then save.
5. Open the Pages URL shown on that settings page.

GitHub Pages serves the root `index.html`, which loads `styles.css` and `app.js`.

## Modules

- **Overview:** project progress, current risks, schedule delays, unresolved defects, and cost/schedule indicators.
- **Initiation & integration:** project charter, stakeholders, team, milestones, project plan, and closure.
- **Scope & WBS:** requirements approval, work breakdown structure, and change requests.
- **Schedule & resources:** activity ownership, dependencies, calculated dates, timeline, and delay impact.
- **Cost & EVM:** planned value, earned value, actual cost, CPI, SPI, and threshold warnings.
- **Quality & risk:** defect tracking, deliverable inspections, risk prioritization, and response plans.

The app validates records, saves changes in browser `localStorage`, and supports JSON backup export and import. Approving a change request applies its linked requirement and cost/schedule impacts to the project baseline.

## Data and hosting limitations

Browser data is stored only in the current browser on the current device. It is not shared with other users or synchronized between devices. Export a JSON backup regularly if you need to preserve or transfer project data. GitHub Pages is static hosting; it does not run Java, MySQL, or provide a shared backend. Do not enter real patient or other sensitive information.

The original Java/MySQL source and database files remain in the repository as a separate desktop implementation. GitHub Pages uses only the root HTML, CSS, and JavaScript files.
