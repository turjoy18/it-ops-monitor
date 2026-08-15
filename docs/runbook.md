# Ops runbook

How to **detect → investigate → resolve** when the monitor reports unhealthy targets.

This is written for the local PoC. Treat mock ticket IDs (`OPS-n`) like Jira keys in a real ops workflow.

## 1. Detect

**Signals**

- Console: `Health check FAIL` and `Incident OPEN` / `Mock ticket created`
- Actuator: `GET /actuator/health` (process up/down only)
- Ops snapshot: `GET /api/status` → `overall` is `DEGRADED` or `UNKNOWN`
- Open work: `GET /api/incidents?status=OPEN` and/or `GET /api/tickets`

**Quick checks**

```bash
curl -s http://127.0.0.1:8080/api/status
curl -s "http://127.0.0.1:8080/api/incidents?status=OPEN"
curl -s -X POST http://127.0.0.1:8080/api/health-checks/run
```

Default local demo forces **ledger-api** down (`ops.mocks.ledger-force-down: true`), so a fresh run usually shows `DEGRADED` with one OPEN incident and a ticket key.

## 2. Investigate

1. Confirm which target is down from `/api/status` → `targets.results` (`up: false`).
2. Open the incident: `GET /api/incidents/{id}` — note `httpStatus`, `message`, `ticketKey`.
3. Hit the target directly (mock examples):

```bash
curl -i http://127.0.0.1:8080/mocks/payments
curl -i http://127.0.0.1:8080/mocks/fx
curl -i http://127.0.0.1:8080/mocks/ledger
```

4. Optional SQL confirmation (H2 console at `/h2-console`):

   - JDBC URL: `jdbc:h2:file:./data/ops-monitor` (app started from project root)
   - User: `sa`, password blank
   - `SELECT * FROM INCIDENTS ORDER BY DETECTED_AT DESC;`

5. Correlate ticket: mock list at `GET /api/tickets`, or use `ticketKey` / `ticketUrl` on the incident.

**Common causes (PoC)**

| Symptom | Likely cause |
|---------|----------------|
| Only ledger down | `ops.mocks.ledger-force-down: true` (intentional demo) |
| All targets down / connection errors | App not listening on `8080`, or wrong target URLs in config |
| No new incident while still failing | Expected — OPEN incident already exists for that target |
| `/api/status` is `UNKNOWN` | No probes run yet — call `POST /api/health-checks/run` or wait for the scheduler |

## 3. Resolve

1. Fix or restore the dependency (for the demo, set ledger healthy):

```yaml
ops:
  mocks:
    ledger-force-down: false
```

Restart the app if you changed `application.yml` (or flip the property in a running test/demo bean if available).

2. Re-run probes:

```bash
curl -s -X POST http://127.0.0.1:8080/api/health-checks/run
curl -s http://127.0.0.1:8080/api/status
```

3. Expect:

   - Target `up: true`
   - Incident `status: RESOLVED` with `resolvedAt` set
   - `ticketKey` retained on the row for audit
   - `/api/status` → `overall: HEALTHY` when all probed targets are up and no OPEN incidents remain

4. Close/comment the ticket in the real world; in this PoC, note the `OPS-n` key in your PR/demo notes.

## Escalation (portfolio framing)

If a real dependency stays down after basic checks:

1. Keep the OPEN incident and ticket as the system of record.
2. Capture last probe payload (`/api/health-checks` or `/api/status`).
3. Hand off with target name, URL, HTTP status, ticket key, and time detected.

## Related docs

- [Architecture](architecture.md) — components and data flow
- [README](../README.md) — run locally, API list, ticketing modes
