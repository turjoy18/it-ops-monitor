package com.itopsmonitor.ticket;

public record TicketRequest(
        Long incidentId,
        String targetName,
        String targetUrl,
        Integer httpStatus,
        String message
) {
    public String summary() {
        return "[ops-monitor] " + targetName + " unhealthy";
    }

    public String description() {
        return "Incident #" + incidentId
                + "\nTarget: " + targetName
                + "\nURL: " + targetUrl
                + "\nHTTP status: " + httpStatus
                + "\nMessage: " + message;
    }
}
