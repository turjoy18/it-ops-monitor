# it-ops-monitor

Internal-style **IT ops monitor + ticketing hub** (portfolio PoC).

**Pitch:** Python for integration PoCs; Java for operational services and incident workflows.

Companion to [open-banking-integration-sandbox](https://github.com/turjoy18/open-banking-integration-sandbox) (Python/FastAPI + React).

## Status

Health checks → SQL incidents → mock/Jira tickets (Issues 1–4). Next: richer list/status APIs, tests polish, ops runbook.

## Features

- Scheduled HTTP health probes (configurable interval + target list)
- Built-in mocks: payments/fx **UP**, ledger **DOWN** (toggleable)
- On failure: open an H2/SQL incident (deduped while already OPEN); on recovery: mark RESOLVED
- On new incident: create a support ticket (in-process **mock** by default, or **Jira REST** / built-in `/mocks/jira`)
- `GET /api/health-checks` — latest probe results (in-memory)
- `POST /api/health-checks/run` — run probes immediately
- `GET /api/incidents` — persisted incidents (includes `ticketKey` / `ticketUrl`)
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
- Latest checks: http://127.0.0.1:8080/api/health-checks
- Run now: `curl -X POST http://127.0.0.1:8080/api/health-checks/run`
- Incidents: http://127.0.0.1:8080/api/incidents
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
  web/          # root + mock target endpoints
  health/       # probe service, scheduler, status API
  incident/     # JPA entity, SQL persistence, list API
  ticket/       # TicketClient (mock + Jira REST) + /mocks/jira stub
```

## Project tracking

Work is split into GitHub Issues (1 issue ≈ 1 branch ≈ 1 PR), similar to a lightweight Jira workflow.
