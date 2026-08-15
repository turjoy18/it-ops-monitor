package com.itopsmonitor.ticket;

public interface TicketClient {

    TicketRef createTicket(TicketRequest request);
}
