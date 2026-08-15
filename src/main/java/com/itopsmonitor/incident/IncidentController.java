package com.itopsmonitor.incident;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String status) {
        List<Incident> incidents;
        IncidentStatus filter = null;
        if (status != null && !status.isBlank()) {
            filter = parseStatus(status);
            incidents = incidentService.listByStatus(filter);
        } else {
            incidents = incidentService.listAll();
        }

        List<IncidentResponse> bodyItems = incidents.stream().map(IncidentResponse::from).toList();
        long openCount = filter == IncidentStatus.OPEN
                ? bodyItems.size()
                : incidentService.countOpen();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("count", bodyItems.size());
        body.put("openCount", openCount);
        body.put("statusFilter", filter != null ? filter.name() : null);
        body.put("incidents", bodyItems);
        return body;
    }

    @GetMapping("/{id}")
    public IncidentResponse getById(@PathVariable Long id) {
        return incidentService.findById(id)
                .map(IncidentResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found: " + id));
    }

    private static IncidentStatus parseStatus(String raw) {
        try {
            return IncidentStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status '" + raw + "'. Use OPEN or RESOLVED."
            );
        }
    }
}
