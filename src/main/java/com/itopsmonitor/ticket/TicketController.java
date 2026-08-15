package com.itopsmonitor.ticket;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketProperties properties;
    private final ObjectProvider<MockTicketClient> mockTicketClient;

    public TicketController(TicketProperties properties, ObjectProvider<MockTicketClient> mockTicketClient) {
        this.properties = properties;
        this.mockTicketClient = mockTicketClient;
    }

    @GetMapping
    public Map<String, Object> list() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("provider", properties.getProvider());
        MockTicketClient mock = mockTicketClient.getIfAvailable();
        List<TicketRef> tickets = mock != null ? mock.listCreated() : List.of();
        body.put("count", tickets.size());
        body.put("tickets", tickets);
        body.put("note", mock != null
                ? "In-process mock tickets created this JVM run"
                : "Provider is not mock; tickets are created via Jira REST (see incident.ticketKey)");
        return body;
    }
}
