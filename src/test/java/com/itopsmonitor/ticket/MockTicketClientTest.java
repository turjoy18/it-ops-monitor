package com.itopsmonitor.ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockTicketClientTest {

    private MockTicketClient client;

    @BeforeEach
    void setUp() {
        TicketProperties properties = new TicketProperties();
        properties.setProjectKey("OPS");
        client = new MockTicketClient(properties);
    }

    @Test
    void createTicketAssignsIncrementalKeysAndStoresRefs() {
        TicketRequest first = new TicketRequest(1L, "ledger-api", "http://x/ledger", 503, "down");
        TicketRequest second = new TicketRequest(2L, "fx-api", "http://x/fx", 500, "error");

        TicketRef a = client.createTicket(first);
        TicketRef b = client.createTicket(second);

        assertThat(a.key()).isEqualTo("OPS-1");
        assertThat(a.provider()).isEqualTo("mock");
        assertThat(a.summary()).contains("ledger-api");
        assertThat(a.url()).contains("OPS-1");

        assertThat(b.key()).isEqualTo("OPS-2");
        assertThat(client.listCreated()).containsExactly(a, b);
    }
}
