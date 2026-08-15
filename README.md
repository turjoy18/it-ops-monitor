# it-ops-monitor

Internal-style **IT ops monitor + ticketing hub** (portfolio PoC).

**Pitch:** Python for integration PoCs; Java for operational services and incident workflows.

Companion to [open-banking-integration-sandbox](https://github.com/turjoy18/open-banking-integration-sandbox) (Python/FastAPI + React).

## Status

Scaffold only (Issue 1). Planned:

- Background health checks against mock endpoints
- Persist incidents to SQL on failure
- Create a support ticket (Jira REST or mock)
- Ops runbook under `/docs`

## Stack

- Java 17+
- Spring Boot 3.4 (Web + Actuator)
- Maven
- SQL + ticket client (coming in later issues)

## Requirements

- JDK 17 or newer
- Maven 3.9+ (`mvn -v`)

Optional: generate a Maven Wrapper so others don’t need a global Maven install (see below).

## Run locally

```bash
cd it-ops-monitor
mvn spring-boot:run
```

Then:

- Service root: http://127.0.0.1:8080/
- Actuator health: http://127.0.0.1:8080/actuator/health

## Tests

```bash
mvn test
```

## Maven Wrapper (optional)

From the project root, with Maven installed once:

```bash
mvn -N wrapper:wrapper -Dmaven=3.9.9
```

After that you can use `./mvnw` / `mvnw.cmd` instead of `mvn`.

## Project layout

```text
src/main/java/com/itopsmonitor/
  ItOpsMonitorApplication.java
  web/          # HTTP entrypoints
  health/       # (next) endpoint probes
  incident/     # (next) SQL incident log
  ticket/       # (next) Jira / mock tickets
src/main/resources/
  application.yml
```

## Project tracking

Work is split into GitHub Issues (1 issue ≈ 1 branch ≈ 1 PR), similar to a lightweight Jira workflow.
