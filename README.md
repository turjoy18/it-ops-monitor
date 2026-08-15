# it-ops-monitor

Internal-style **IT ops monitor + ticketing hub** (portfolio PoC).

**Pitch:** Python for integration PoCs; Java for operational services and incident workflows.

Companion to [open-banking-integration-sandbox](https://github.com/turjoy18/open-banking-integration-sandbox) (Python/FastAPI + React).

## Status

Core ops flow + read APIs (Issues 1–5). Next: broader test coverage polish, ops runbook docs.

## Features

- Scheduled HTTP health probes (configurable interval + target list)
- Built-in mocks: payments/fx **UP**, ledger **DOWN** (toggleable)
- On failure: open an H2/SQL incident (deduped while already OPEN); on recovery: mark RESOLVED
- On new incident: create a support ticket (in-process **mock** by default, or **Jira REST** / built-in `/mocks/jira`)
- `GET /api/health-checks` — latest probe results (in-memory)
- `POST /api/health-checks/run` — run probes immediately
- `GET /api/incidents` — list incidents (`?status=OPEN|RESOLVED`)
- `GET /api/incidents/{id}` — incident detail
- `GET /api/status` — overall snapshot (probes + open incidents)
- `GET /api/tickets` — mock tickets created this run
- Actuator `GET /actuator/health`
- H2 console at `/h2-console`

## Stack

- Java 17+
- Spring Boot 3.4 (Web + Actuator + Scheduling + Data JPA)
- H2 (file DB under `./data/`)
- Maven

## Requirements

- JDK 17 or newer
- Maven 3.9+ (`mvn -v`)

## Run locally

```bash
cd it-ops-monitor
mvn spring-boot:run
```

Then:

- Service root: http://127.0.0.1:8080/
- Run now: `curl -X POST http://127.0.0.1:8080/api/health-checks/run`
- Status: http://127.0.0.1:8080/api/status
- Open incidents: http://127.0.0.1:8080/api/incidents?status=OPEN
- Tickets: http://127.0.0.1:8080/api/tickets
- Actuator: http://127.0.0.1:8080/actuator/health

Watch the console for `Incident OPEN`, `Mock ticket created`, and `linked to ticket`.

### Ticketing modes

Default (`ops.ticket.provider: mock`): in-process tickets like `OPS-1` (no external Jira).

Optional Jira-shaped HTTP (still no Cloud license needed — use the local stub):

```yaml
ops:
  ticket:
    provider: jira
    jira:
      base-url: http://127.0.0.1:8080/mocks/jira
```

For a real Jira Cloud site, set `base-url` to your site and fill `username` + `api-token`.

### H2 console

1. Open http://127.0.0.1:8080/h2-console
2. JDBC URL: `jdbc:h2:file:./data/ops-monitor` (run the app from the project root)
3. User: `sa` (blank password)
4. Query: `SELECT * FROM INCIDENTS;`

### Force ledger mock healthy

In `src/main/resources/application.yml`:

```yaml
ops:
  mocks:
    ledger-force-down: false
```

After the next successful probe, the open ledger incident is marked `RESOLVED`.

## Tests

```bash
mvn test
```

## Project layout

```text
src/main/java/com/itopsmonitor/
  ItOpsMonitorApplication.java
  web/          # root, status snapshot, mock targets
  health/       # probe service, scheduler, health-check API
  incident/     # JPA entity, persistence, incident APIs
  ticket/       # TicketClient (mock + Jira REST) + /mocks/jira stub
```

## Project tracking

Work is split into GitHub Issues (1 issue ≈ 1 branch ≈ 1 PR), similar to a lightweight Jira workflow.
