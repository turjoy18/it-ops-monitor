package com.itopsmonitor;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.MockMvc;

import com.itopsmonitor.health.HealthStatusStore;
import com.itopsmonitor.health.MonitorProperties;
import com.itopsmonitor.incident.Incident;
import com.itopsmonitor.incident.IncidentRepository;
import com.itopsmonitor.incident.IncidentStatus;
import com.itopsmonitor.ticket.MockTicketClient;
import com.itopsmonitor.web.OpsMocksProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage for the portfolio MVP pipeline:
 * probe failure → SQL incident → mock ticket → no duplicate while down → resolve on recovery.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class FailureLogTicketFlowIT {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorProperties monitorProperties;

    @Autowired
    private OpsMocksProperties mocksProperties;

    @Autowired
    private HealthStatusStore statusStore;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private MockTicketClient mockTicketClient;

    @BeforeEach
    void setUp() {
        statusStore.clear();
        incidentRepository.deleteAll();
        mocksProperties.setLedgerForceDown(true);

        MonitorProperties.Target payments = new MonitorProperties.Target();
        payments.setName("payments-api");
        payments.setUrl("http://127.0.0.1:" + port + "/mocks/payments");

        MonitorProperties.Target ledger = new MonitorProperties.Target();
        ledger.setName("ledger-api");
        ledger.setUrl("http://127.0.0.1:" + port + "/mocks/ledger");

        monitorProperties.setTargets(List.of(payments, ledger));
    }

    @Test
    @DisplayName("failure → incident log → ticket; re-poll does not duplicate; recovery resolves")
    void failureLogsIncidentCreatesTicketThenRecoveryResolves() throws Exception {
        int ticketsBefore = mockTicketClient.listCreated().size();

        mockMvc.perform(post("/api/health-checks/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downCount").value(1));

        List<Incident> openAfterFail = incidentRepository.findAll();
        assertThat(openAfterFail).hasSize(1);
        Incident incident = openAfterFail.get(0);
        assertThat(incident.getTargetName()).isEqualTo("ledger-api");
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(incident.getTicketKey()).isNotBlank();
        assertThat(incident.getTicketUrl()).isNotBlank();

        assertThat(mockTicketClient.listCreated()).hasSize(ticketsBefore + 1);
        assertThat(mockTicketClient.listCreated().get(ticketsBefore).key())
                .isEqualTo(incident.getTicketKey());

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.incidents.openCount").value(1));

        mockMvc.perform(post("/api/health-checks/run"))
                .andExpect(status().isOk());
        assertThat(incidentRepository.findAll()).hasSize(1);
        assertThat(mockTicketClient.listCreated()).hasSize(ticketsBefore + 1);

        mocksProperties.setLedgerForceDown(false);

        mockMvc.perform(post("/api/health-checks/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downCount").value(0));

        List<Incident> afterRecovery = incidentRepository.findAll();
        assertThat(afterRecovery).hasSize(1);
        assertThat(afterRecovery.get(0).getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(afterRecovery.get(0).getResolvedAt()).isNotNull();
        assertThat(afterRecovery.get(0).getTicketKey()).isEqualTo(incident.getTicketKey());

        mockMvc.perform(get("/api/incidents?status=RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.incidents", hasSize(1)));

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("HEALTHY"))
                .andExpect(jsonPath("$.incidents.openCount").value(0));
    }
}
