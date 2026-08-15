# it-ops-monitor

Internal-style **IT ops monitor + ticketing hub** (portfolio PoC).

**Pitch:** Python for integration PoCs; Java for operational services and incident workflows.

Companion to [open-banking-integration-sandbox](https://github.com/turjoy18/open-banking-integration-sandbox) (Python/FastAPI + React).

## Status

Health checks + SQL incident log on failure (Issues 1–3). Next: ticket creation (Jira/mock), richer incident APIs, ops runbook.

## Features

- Scheduled HTTP health probes (configurable interval + target list)
- Built-in mocks: payments/fx **UP**, ledger **DOWN** (toggleable)
- On failure: open an H2/SQL incident (deduped while already OPEN); on recovery: mark RESOLVED
- `GET /api/health-checks` — latest probe results (in-memory)
- `POST /api/health-checks/run` — run probes immediately
- `GET /api/incidents` — persisted incidents
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
- Actuator: http://127.0.0.1:8080/actuator/health

Watch the console for `Health check OK` / `FAIL` and `Incident OPEN` / `RESOLVED`.

### H2 console

1. Open http://127.0.0.1:8080/h2-console
2. JDBC URL: `jdbc:h2:file:./data/ops-monitor`
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
  ticket/       # (next) Jira / mock tickets
```

## Project tracking

Work is split into GitHub Issues (1 issue ≈ 1 branch ≈ 1 PR), similar to a lightweight Jira workflow.
