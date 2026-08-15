package com.itopsmonitor.health;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class HealthCheckControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorProperties monitorProperties;

    @Autowired
    private HealthStatusStore statusStore;

    @BeforeEach
    void configureLocalMockTargets() {
        statusStore.clear();

        MonitorProperties.Target payments = new MonitorProperties.Target();
        payments.setName("payments-api");
        payments.setUrl("http://127.0.0.1:" + port + "/mocks/payments");

        MonitorProperties.Target ledger = new MonitorProperties.Target();
        ledger.setName("ledger-api");
        ledger.setUrl("http://127.0.0.1:" + port + "/mocks/ledger");

        monitorProperties.setTargets(List.of(payments, ledger));
    }

    @Test
    void runNowRecordsUpAndDownMocks() throws Exception {
        mockMvc.perform(post("/api/health-checks/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.downCount").value(1))
                .andExpect(jsonPath("$.results", hasSize(2)));

        mockMvc.perform(get("/api/health-checks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downCount").value(1));
    }

    @Test
    void mockLedgerIsDownByDefault() throws Exception {
        mockMvc.perform(get("/mocks/ledger"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @Test
    void mockPaymentsIsUp() throws Exception {
        mockMvc.perform(get("/mocks/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
