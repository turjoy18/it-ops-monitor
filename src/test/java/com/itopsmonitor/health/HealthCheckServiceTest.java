package com.itopsmonitor.health;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HealthCheckServiceTest {

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
        service = new HealthCheckService(properties, store, restTemplate);
    }

    @Test
    void checkAllStoresUpAndDownResults() {
        server.expect(requestTo("http://localhost/mocks/payments"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status\":\"UP\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost/mocks/ledger"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        service.checkAll();
        server.verify();

        List<HealthCheckResult> results = store.findAll();
        assertThat(results).hasSize(2);

        HealthCheckResult payments = results.stream()
                .filter(r -> r.name().equals("payments-api"))
                .findFirst()
                .orElseThrow();
        assertThat(payments.up()).isTrue();
        assertThat(payments.httpStatus()).isEqualTo(200);

        HealthCheckResult ledger = results.stream()
                .filter(r -> r.name().equals("ledger-api"))
                .findFirst()
                .orElseThrow();
        assertThat(ledger.up()).isFalse();
        assertThat(ledger.httpStatus()).isEqualTo(503);
    }
}
