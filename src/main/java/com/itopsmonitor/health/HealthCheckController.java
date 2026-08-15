package com.itopsmonitor.health;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-checks")
public class HealthCheckController {

    private final HealthCheckService healthCheckService;
    private final HealthStatusStore statusStore;

    public HealthCheckController(HealthCheckService healthCheckService, HealthStatusStore statusStore) {
        this.healthCheckService = healthCheckService;
        this.statusStore = statusStore;
    }

    @GetMapping
    public Map<String, Object> latest() {
        List<HealthCheckResult> results = statusStore.findAll();
        long downCount = results.stream().filter(r -> !r.up()).count();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("count", results.size());
        body.put("downCount", downCount);
        body.put("results", results);
        return body;
    }

    @PostMapping("/run")
    public Map<String, Object> runNow() {
        healthCheckService.checkAll();
        return latest();
    }
}
