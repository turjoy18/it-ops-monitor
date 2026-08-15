package com.itopsmonitor.ticket;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal Jira REST create-issue stub for local demos without a Jira Cloud license.
 */
@RestController
@RequestMapping("/mocks/jira")
public class MockJiraController {

    private final TicketProperties properties;
    private final AtomicLong sequence = new AtomicLong(1000);

    public MockJiraController(TicketProperties properties) {
        this.properties = properties;
    }

    @PostMapping("/rest/api/2/issue")
    public ResponseEntity<Map<String, Object>> createIssue(@RequestBody Map<String, Object> body) {
        long id = sequence.incrementAndGet();
        String key = properties.getProjectKey() + "-" + id;
        String self = "http://127.0.0.1:8080/mocks/jira/rest/api/2/issue/" + id;
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", String.valueOf(id),
                "key", key,
                "self", self
        ));
    }
}
