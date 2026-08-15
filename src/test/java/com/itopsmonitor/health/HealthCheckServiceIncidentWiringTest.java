package com.itopsmonitor.health;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.itopsmonitor.incident.Incident;
import com.itopsmonitor.incident.IncidentService;
import com.itopsmonitor.incident.IncidentStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceIncidentWiringTest {

    @Mock
    private IncidentService incidentService;

    private MonitorProperties properties;
    private HealthStatusStore store;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private HealthCheckService service;

    @BeforeEach
    void setUp() {
        properties = new MonitorProperties();
        MonitorProperties.Target up = new MonitorProperties.Target();
        up.setName("payments-api");
        up.setUrl("http://localhost/mocks/payments");
        MonitorProperties.Target down = new MonitorProperties.Target();
        down.setName("ledger-api");
        down.setUrl("http://localhost/mocks/ledger");
        properties.setTargets(List.of(up, down));

        store = new HealthStatusStore();
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        service = new HealthCheckService(properties, store, restTemplate, incidentService);
    }

    @Test
    void checkAllRecordsFailureForDownAndRecoveryForUp() {
        server.expect(requestTo("http://localhost/mocks/payments"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status\":\"UP\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost/mocks/ledger"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        when(incidentService.recordRecovery(any())).thenReturn(Optional.empty());
        when(incidentService.recordFailure(any())).thenReturn(Optional.of(
                new Incident("ledger-api", "http://localhost/mocks/ledger", 503, 1L, "x",
                        IncidentStatus.OPEN, java.time.Instant.parse("2026-08-16T00:00:00Z"))
        ));

        service.checkAll();
        server.verify();

        ArgumentCaptor<HealthCheckResult> failCaptor = ArgumentCaptor.forClass(HealthCheckResult.class);
        verify(incidentService).recordFailure(failCaptor.capture());
        assertThat(failCaptor.getValue().name()).isEqualTo("ledger-api");
        assertThat(failCaptor.getValue().up()).isFalse();

        ArgumentCaptor<HealthCheckResult> recoveryCaptor = ArgumentCaptor.forClass(HealthCheckResult.class);
        verify(incidentService).recordRecovery(recoveryCaptor.capture());
        assertThat(recoveryCaptor.getValue().name()).isEqualTo("payments-api");
        assertThat(recoveryCaptor.getValue().up()).isTrue();

        verify(incidentService, never()).recordFailure(
                org.mockito.ArgumentMatchers.argThat(r -> "payments-api".equals(r.name()))
        );
    }
}
