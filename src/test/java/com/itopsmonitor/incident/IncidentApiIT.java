package com.itopsmonitor.incident;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itopsmonitor.health.HealthStatusStore;
import com.itopsmonitor.health.MonitorProperties;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class IncidentApiIT {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void listFilterDetailAndStatusEndpoints() throws Exception {
        mockMvc.perform(post("/api/health-checks/run"))
                .andExpect(status().isOk());

        MvcResult listResult = mockMvc.perform(get("/api/incidents?status=OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.statusFilter").value("OPEN"))
                .andExpect(jsonPath("$.incidents", hasSize(1)))
                .andExpect(jsonPath("$.incidents[0].ticketKey").isNotEmpty())
                .andReturn();

        JsonNode root = objectMapper.readTree(listResult.getResponse().getContentAsString());
        long id = root.path("incidents").get(0).path("id").asLong();

        mockMvc.perform(get("/api/incidents/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.targetName").value("ledger-api"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/api/incidents/999999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/incidents?status=NOPE"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.targets.downCount").value(1))
                .andExpect(jsonPath("$.incidents.openCount").value(1))
                .andExpect(jsonPath("$.incidents.open", hasSize(1)));
    }
}
