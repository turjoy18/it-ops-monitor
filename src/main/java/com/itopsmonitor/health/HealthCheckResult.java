package com.itopsmonitor.health;

import java.time.Instant;

public record HealthCheckResult(
        String name,
        String url,
        boolean up,
        Integer httpStatus,
        long latencyMs,
        String message,
        Instant checkedAt
) {
}
