# it-ops-monitor

Internal-style **IT ops monitor + ticketing hub** (portfolio PoC).

**Pitch:** Python for integration PoCs; Java for operational services and incident workflows.

Companion to [open-banking-integration-sandbox](https://github.com/turjoy18/open-banking-integration-sandbox) (Python/FastAPI + React).

## Status

Health checks against built-in mock endpoints (Issue 2). Next: SQL incident log, ticket creation, ops runbook.

## Features

- Scheduled HTTP health probes (configurable interval + target list)
- Built-in mocks: payments/fx **UP**, ledger **DOWN** (toggleable)
- `GET /api/health-checks` — latest probe results (in-memory)
- `POST /api/health-checks/run` — run probes immediately
- Actuator `GET /actuator/health`

## Stack

- Java 17+
- Spring Boot 3.4 (Web + Actuator + Scheduling)
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
- Actuator: http://127.0.0.1:8080/actuator/health

Watch the console for `Health check OK` / `Health check FAIL` lines every `ops.monitor.poll-interval-ms` (default 15s).

### Force ledger mock healthy

In `src/main/resources/application.yml`:

```yaml
ops:
  mocks:
    ledger-force-down: false
```

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
  incident/     # (next) SQL incident log
  ticket/       # (next) Jira / mock tickets
```

## Project tracking

Work is split into GitHub Issues (1 issue ≈ 1 branch ≈ 1 PR), similar to a lightweight Jira workflow.
