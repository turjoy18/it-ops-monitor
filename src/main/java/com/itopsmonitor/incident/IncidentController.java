package com.itopsmonitor.incident;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal read API so the failure → SQL flow is easy to demo.
 * Issue 5 can expand listing / latest-status shaping.
 */
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public Map<String, Object> list() {
        List<Incident> incidents = incidentService.listAll();
        long openCount = incidents.stream().filter(i -> i.getStatus() == IncidentStatus.OPEN).count();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("count", incidents.size());
        body.put("openCount", openCount);
        body.put("incidents", incidents);
        return body;
    }
}
