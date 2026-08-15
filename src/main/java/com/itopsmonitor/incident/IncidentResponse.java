package com.itopsmonitor.incident;

import java.time.Instant;

public record IncidentResponse(
        Long id,
        String targetName,
        String targetUrl,
        Integer httpStatus,
        Long latencyMs,
        String message,
        IncidentStatus status,
        Instant detectedAt,
        Instant resolvedAt,
        String ticketKey,
        String ticketUrl
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTargetName(),
                incident.getTargetUrl(),
                incident.getHttpStatus(),
                incident.getLatencyMs(),
                incident.getMessage(),
                incident.getStatus(),
                incident.getDetectedAt(),
                incident.getResolvedAt(),
                incident.getTicketKey(),
                incident.getTicketUrl()
        );
    }
}
