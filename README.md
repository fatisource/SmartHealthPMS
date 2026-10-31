# CarePlan

CarePlan is a static, browser-based project management workspace for the Smart Healthcare Project Management System case study. It uses native JavaScript ES modules, HTML, and CSS; no package installation or build step is required.

## Run locally

Serve the repository root over HTTP, then open the local address in a modern browser. For example, with Python installed, run `python -m http.server 8000` from the repository directory and open `http://localhost:8000`. Serving over HTTP is needed because browsers restrict JavaScript modules opened directly with `file://`.

The app starts with sample project data the first time it is opened.

## Deploy with GitHub Pages

1. Push the repository to GitHub.
2. Open the repository's **Settings → Pages**.
3. Under **Build and deployment**, choose **Deploy from a branch**.
4. Select the `main` branch and `/ (root)` folder, then save.
5. Open the Pages URL shown on that settings page.

GitHub Pages serves the root `index.html`, which loads `styles.css` and the small root `app.js` entrypoint. The entrypoint imports the application modules from `js/`.

## Project structure

```text
.
├── index.html
├── styles.css
├── app.js                 # Browser entrypoint
├── js/
│   ├── app.js             # Application startup and page rendering
│   ├── config.js          # Module labels and shared constants
│   ├── events.js          # Navigation and form event wiring
│   ├── core/
│   │   ├── calculations.js
│   │   ├── context.js
│   │   ├── state.js
│   │   ├── storage.js
│   │   └── utils.js
│   ├── data/
│   │   └── seed.js
│   ├── features/
│   │   ├── backup.js
│   │   ├── project.js
│   │   └── records.js
│   ├── ui/
│   │   ├── components.js
│   │   └── notifications.js
│   └── views/             # One renderer per project module
└── README.md
```

## Modules

- **Overview:** project progress, current risks, schedule delays, unresolved defects, and cost/schedule indicators.
- **Initiation & integration:** project charter, stakeholders, team, milestones, project plan, and closure.
- **Scope & WBS:** requirements approval, work breakdown structure, and change requests.
- **Schedule & resources:** activity ownership, dependencies, calculated dates, timeline, and delay impact.
- **Cost & EVM:** planned value, earned value, actual cost, CPI, SPI, and threshold warnings.
- **Quality & risk:** defect tracking, deliverable inspections, risk prioritization, and response plans.

The app validates records, saves changes in browser `localStorage`, and supports JSON backup export and import. Approving a change request applies its linked requirement and cost/schedule impacts to the project baseline.

## Data and hosting limitations

Browser data is stored only in the current browser on the current device. It is not shared with other users or synchronized between devices. Export a JSON backup regularly if you need to preserve or transfer project data. GitHub Pages is static hosting; it does not provide a shared backend or database. Do not enter real patient or other sensitive information.
