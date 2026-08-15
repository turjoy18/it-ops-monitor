package com.itopsmonitor.web;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itopsmonitor.health.HealthCheckResult;
import com.itopsmonitor.health.HealthStatusStore;
import com.itopsmonitor.incident.Incident;
import com.itopsmonitor.incident.IncidentResponse;
import com.itopsmonitor.incident.IncidentService;
import com.itopsmonitor.incident.IncidentStatus;

/**
 * Single ops snapshot: latest probe results + open incidents.
 */
@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final HealthStatusStore statusStore;
    private final IncidentService incidentService;

    public StatusController(HealthStatusStore statusStore, IncidentService incidentService) {
        this.statusStore = statusStore;
        this.incidentService = incidentService;
    }

    @GetMapping
    public Map<String, Object> latestStatus() {
        List<HealthCheckResult> probes = statusStore.findAll().stream()
                .sorted(Comparator.comparing(HealthCheckResult::name))
                .toList();
        long downCount = probes.stream().filter(r -> !r.up()).count();
        boolean healthy = downCount == 0 && !probes.isEmpty();

        List<Incident> openIncidents = incidentService.listByStatus(IncidentStatus.OPEN);
        Instant lastCheckedAt = probes.stream()
                .map(HealthCheckResult::checkedAt)
                .max(Instant::compareTo)
                .orElse(null);

        String overall;
        if (probes.isEmpty()) {
            overall = "UNKNOWN";
        } else if (healthy && openIncidents.isEmpty()) {
            overall = "HEALTHY";
        } else if (downCount > 0 || !openIncidents.isEmpty()) {
            overall = "DEGRADED";
        } else {
            overall = "HEALTHY";
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("overall", overall);
        body.put("checkedAt", lastCheckedAt);
        body.put("targets", Map.of(
                "count", probes.size(),
                "downCount", downCount,
                "results", probes
        ));
        body.put("incidents", Map.of(
                "openCount", openIncidents.size(),
                "open", openIncidents.stream().map(IncidentResponse::from).toList()
        ));
        return body;
    }
}
