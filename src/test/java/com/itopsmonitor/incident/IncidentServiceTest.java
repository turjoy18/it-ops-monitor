package com.itopsmonitor.incident;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.itopsmonitor.health.HealthCheckResult;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(IncidentService.class)
class IncidentServiceTest {

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private IncidentRepository incidentRepository;

    @BeforeEach
    void clear() {
        incidentRepository.deleteAll();
    }

    @Test
    void recordFailureCreatesOpenIncidentOnce() {
        HealthCheckResult down = new HealthCheckResult(
                "ledger-api",
                "http://localhost/mocks/ledger",
                false,
                503,
                12L,
                "Service Unavailable",
                Instant.parse("2026-08-16T00:00:00Z")
        );

        assertThat(incidentService.recordFailure(down)).isPresent();
        assertThat(incidentService.recordFailure(down)).isEmpty();
        assertThat(incidentRepository.findAll()).hasSize(1);
        assertThat(incidentRepository.findAll().get(0).getStatus()).isEqualTo(IncidentStatus.OPEN);
    }

    @Test
    void recordRecoveryResolvesOpenIncident() {
        HealthCheckResult down = new HealthCheckResult(
                "ledger-api",
                "http://localhost/mocks/ledger",
                false,
                503,
                12L,
                "Service Unavailable",
                Instant.parse("2026-08-16T00:00:00Z")
        );
        HealthCheckResult up = new HealthCheckResult(
                "ledger-api",
                "http://localhost/mocks/ledger",
                true,
                200,
                5L,
                "OK",
                Instant.parse("2026-08-16T00:01:00Z")
        );

        incidentService.recordFailure(down);
        assertThat(incidentService.recordRecovery(up)).isPresent();
        assertThat(incidentRepository.findAll()).hasSize(1);
        assertThat(incidentRepository.findAll().get(0).getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(incidentRepository.findAll().get(0).getResolvedAt()).isNotNull();
    }
}
