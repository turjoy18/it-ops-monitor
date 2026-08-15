package com.itopsmonitor.ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "ops.ticket.provider", havingValue = "mock", matchIfMissing = true)
public class MockTicketClient implements TicketClient {

    private static final Logger log = LoggerFactory.getLogger(MockTicketClient.class);

    private final TicketProperties properties;
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<TicketRef> created = new ArrayList<>();

    public MockTicketClient(TicketProperties properties) {
        this.properties = properties;
    }

    @Override
    public synchronized TicketRef createTicket(TicketRequest request) {
        long n = sequence.incrementAndGet();
        String key = properties.getProjectKey() + "-" + n;
        String url = "mock://jira/browse/" + key;
        TicketRef ref = new TicketRef(key, url, request.summary(), "mock");
        created.add(ref);
        log.info("Mock ticket created key={} incidentId={} summary={}",
                key, request.incidentId(), request.summary());
        return ref;
    }

    public synchronized List<TicketRef> listCreated() {
        return List.copyOf(created);
    }
}
