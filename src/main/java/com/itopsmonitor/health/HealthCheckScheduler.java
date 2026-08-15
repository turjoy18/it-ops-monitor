package com.itopsmonitor.health;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckScheduler {

    private final HealthCheckService healthCheckService;

    public HealthCheckScheduler(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @Scheduled(fixedDelayString = "${ops.monitor.poll-interval-ms:30000}")
    public void poll() {
        healthCheckService.checkAll();
    }
}
