package com.itopsmonitor.ticket;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ops.ticket")
public class TicketProperties {

    /**
     * {@code mock} = in-process mock tickets; {@code jira} = HTTP client against Jira REST
     * (or the built-in {@code /mocks/jira} stub).
     */
    private String provider = "mock";
    private String projectKey = "OPS";
    private final Jira jira = new Jira();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public Jira getJira() {
        return jira;
    }

    public static class Jira {
        private String baseUrl = "http://127.0.0.1:8080/mocks/jira";
        private String username = "";
        private String apiToken = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }
    }
}
