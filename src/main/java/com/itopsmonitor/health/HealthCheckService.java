package com.itopsmonitor.health;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final MonitorProperties properties;
    private final HealthStatusStore statusStore;
    private final RestTemplate restTemplate;

    @Autowired
    public HealthCheckService(
            MonitorProperties properties,
            HealthStatusStore statusStore,
            RestTemplateBuilder restTemplateBuilder
    ) {
        this.properties = properties;
        this.statusStore = statusStore;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
    }

    /** Visible for tests that supply a custom RestTemplate. */
    HealthCheckService(
            MonitorProperties properties,
            HealthStatusStore statusStore,
            RestTemplate restTemplate
    ) {
        this.properties = properties;
        this.statusStore = statusStore;
        this.restTemplate = restTemplate;
    }

    public void checkAll() {
        for (MonitorProperties.Target target : properties.getTargets()) {
            HealthCheckResult result = checkOne(target);
            statusStore.save(result);
            if (result.up()) {
                log.info("Health check OK name={} status={} latencyMs={}",
                        result.name(), result.httpStatus(), result.latencyMs());
            } else {
                log.warn("Health check FAIL name={} status={} latencyMs={} message={}",
                        result.name(), result.httpStatus(), result.latencyMs(), result.message());
            }
        }
    }

    public HealthCheckResult checkOne(MonitorProperties.Target target) {
        Instant started = Instant.now();
        long startNanos = System.nanoTime();
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(target.getUrl(), Void.class);
            long latencyMs = elapsedMs(startNanos);
            boolean up = response.getStatusCode().is2xxSuccessful();
            return new HealthCheckResult(
                    target.getName(),
                    target.getUrl(),
                    up,
                    response.getStatusCode().value(),
                    latencyMs,
                    up ? "OK" : "Unexpected status",
                    started
            );
        } catch (RestClientResponseException ex) {
            long latencyMs = elapsedMs(startNanos);
            return new HealthCheckResult(
                    target.getName(),
                    target.getUrl(),
                    false,
                    ex.getStatusCode().value(),
                    latencyMs,
                    ex.getStatusText(),
                    started
            );
        } catch (Exception ex) {
            long latencyMs = elapsedMs(startNanos);
            return new HealthCheckResult(
                    target.getName(),
                    target.getUrl(),
                    false,
                    null,
                    latencyMs,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                    started
            );
        }
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
