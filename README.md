# CarePlan

CarePlan is a PHP/MySQL-backed project management workspace for the Smart Healthcare Project Management System case study. Its browser interface uses native JavaScript ES modules, HTML, and CSS; no package installation or build step is required.

## Run with XAMPP

1. Start **MySQL** in the XAMPP Control Panel.
2. Open phpMyAdmin and select the `spms_db` database. If the project tables do not exist yet, use **Import** and select `database/schema.sql`. The schema uses `CREATE TABLE IF NOT EXISTS` and does not delete existing records.
3. If your local MySQL credentials differ from XAMPP defaults, copy `api/config.local.example.php` to `api/config.local.php` and edit the values. The local config is ignored by Git.
4. Run `run-xampp.bat` and open `http://127.0.0.1:8000`.

The app loads the existing project and module records from `spms_db`. If the database is empty, the case-study sample records are inserted automatically. The application stores each module in its own existing relational table and saves changes transactionally. The API accepts local-machine requests only.

Do not open `index.html` directly as a `file://` URL: module scripts and the PHP API require an HTTP server.

## Hosting note

GitHub Pages cannot run PHP or connect to MySQL. Keep this XAMPP setup local for the exam/demo. Public multi-user deployment requires a separately hosted PHP API and database, plus authentication and production database credentials; do not expose the XAMPP database or commit production secrets.

## Project structure

```text
.
├── index.html
├── styles.css
├── app.js                 # Browser entrypoint
├── api/
│   ├── config.php         # Local database connection
│   ├── state.php          # Transactional API entrypoint
│   └── modules/           # Project, initiation, scope, schedule, quality persistence
├── database/
│   └── schema.sql         # Non-destructive schema for spms_db
├── run-xampp.bat          # Starts a local PHP server
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

The app validates records, stores them in MySQL, and supports JSON backup export and import. Approving a change request applies its linked requirement and cost/schedule impacts to the project baseline.

## Data and hosting limitations

This project-management app stores its data in the local `spms_db` database. It is not a patient-record system; do not enter real patient or other sensitive information.
