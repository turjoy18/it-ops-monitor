package com.itopsmonitor.ticket;

public record TicketRef(
        String key,
        String url,
        String summary,
        String provider
) {
}
