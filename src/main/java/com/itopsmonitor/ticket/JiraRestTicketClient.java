package com.itopsmonitor.ticket;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Posts a Jira Cloud-style create-issue payload to {@code ops.ticket.jira.base-url}.
 * Point that URL at a real Jira site or at this app's {@code /mocks/jira} stub.
 */
@Service
@ConditionalOnProperty(name = "ops.ticket.provider", havingValue = "jira")
public class JiraRestTicketClient implements TicketClient {

    private static final Logger log = LoggerFactory.getLogger(JiraRestTicketClient.class);

    private final TicketProperties properties;
    private final RestTemplate restTemplate;

    public JiraRestTicketClient(TicketProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public TicketRef createTicket(TicketRequest request) {
        String base = trimTrailingSlash(properties.getJira().getBaseUrl());
        String createUrl = base + "/rest/api/2/issue";

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("project", Map.of("key", properties.getProjectKey()));
        fields.put("summary", request.summary());
        fields.put("description", request.description());
        fields.put("issuetype", Map.of("name", "Bug"));

        Map<String, Object> body = Map.of("fields", fields);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String username = properties.getJira().getUsername();
        String token = properties.getJira().getApiToken();
        if (username != null && !username.isBlank() && token != null && !token.isBlank()) {
            String basic = Base64.getEncoder().encodeToString(
                    (username + ":" + token).getBytes(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basic);
        }

        ResponseEntity<Map> response = restTemplate.postForEntity(
                createUrl,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<String, Object> responseBody = response.getBody() != null ? response.getBody() : Map.of();
        String key = String.valueOf(responseBody.getOrDefault("key", "UNKNOWN"));
        String self = String.valueOf(responseBody.getOrDefault("self", createUrl + "/" + key));
        TicketRef ref = new TicketRef(key, self, request.summary(), "jira");
        log.info("Jira ticket created key={} incidentId={} url={}", key, request.incidentId(), self);
        return ref;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
