package com.itopsmonitor.incident;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.MockMvc;

import com.itopsmonitor.health.HealthStatusStore;
import com.itopsmonitor.health.MonitorProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class IncidentPersistenceIT {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorProperties monitorProperties;

    @Autowired
    private HealthStatusStore statusStore;

    @Autowired
    private IncidentRepository incidentRepository;

    @BeforeEach
    void setUp() {
        statusStore.clear();
        incidentRepository.deleteAll();

        MonitorProperties.Target payments = new MonitorProperties.Target();
        payments.setName("payments-api");
        payments.setUrl("http://127.0.0.1:" + port + "/mocks/payments");

        MonitorProperties.Target ledger = new MonitorProperties.Target();
        ledger.setName("ledger-api");
        ledger.setUrl("http://127.0.0.1:" + port + "/mocks/ledger");

        monitorProperties.setTargets(List.of(payments, ledger));
    }

    @Test
    void failurePersistsOpenIncidentAndDoesNotDuplicate() throws Exception {
        mockMvc.perform(post("/api/health-checks/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downCount").value(1));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openCount").value(1))
                .andExpect(jsonPath("$.count").value(1));

        List<Incident> afterFirst = incidentRepository.findAll();
        assertThat(afterFirst).hasSize(1);
        assertThat(afterFirst.get(0).getTargetName()).isEqualTo("ledger-api");
        assertThat(afterFirst.get(0).getStatus()).isEqualTo(IncidentStatus.OPEN);

        mockMvc.perform(post("/api/health-checks/run"))
                .andExpect(status().isOk());

        assertThat(incidentRepository.findAll()).hasSize(1);

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.openCount").value(1));
    }
}
