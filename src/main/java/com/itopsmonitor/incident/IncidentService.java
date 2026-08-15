package com.itopsmonitor.incident;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itopsmonitor.health.HealthCheckResult;
import com.itopsmonitor.ticket.TicketClient;
import com.itopsmonitor.ticket.TicketRef;
import com.itopsmonitor.ticket.TicketRequest;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepository incidentRepository;
    private final TicketClient ticketClient;

    public IncidentService(IncidentRepository incidentRepository, TicketClient ticketClient) {
        this.incidentRepository = incidentRepository;
        this.ticketClient = ticketClient;
    }

    /**
     * Opens an incident only when none is already OPEN for this target (avoids spam on every poll).
     * On first open, creates a support ticket and stores the ticket key on the incident.
     */
    @Transactional
    public Optional<Incident> recordFailure(HealthCheckResult result) {
        Optional<Incident> existing = incidentRepository
                .findFirstByTargetNameAndStatusOrderByDetectedAtDesc(result.name(), IncidentStatus.OPEN);
        if (existing.isPresent()) {
            return Optional.empty();
        }

        Incident incident = new Incident(
                result.name(),
                result.url(),
                result.httpStatus(),
                result.latencyMs(),
                result.message(),
                IncidentStatus.OPEN,
                result.checkedAt() != null ? result.checkedAt() : Instant.now()
        );
        Incident saved = incidentRepository.save(incident);
        log.warn("Incident OPEN id={} target={} status={} message={}",
                saved.getId(), saved.getTargetName(), saved.getHttpStatus(), saved.getMessage());

        TicketRef ticket = ticketClient.createTicket(new TicketRequest(
                saved.getId(),
                saved.getTargetName(),
                saved.getTargetUrl(),
                saved.getHttpStatus(),
                saved.getMessage()
        ));
        saved.attachTicket(ticket.key(), ticket.url());
        Incident withTicket = incidentRepository.save(saved);
        log.info("Incident id={} linked to ticket key={}", withTicket.getId(), withTicket.getTicketKey());
        return Optional.of(withTicket);
    }

    @Transactional
    public Optional<Incident> recordRecovery(HealthCheckResult result) {
        Optional<Incident> open = incidentRepository
                .findFirstByTargetNameAndStatusOrderByDetectedAtDesc(result.name(), IncidentStatus.OPEN);
        if (open.isEmpty()) {
            return Optional.empty();
        }

        Incident incident = open.get();
        incident.resolve(Instant.now());
        Incident saved = incidentRepository.save(incident);
        log.info("Incident RESOLVED id={} target={} ticketKey={}",
                saved.getId(), saved.getTargetName(), saved.getTicketKey());
        return Optional.of(saved);
    }

    @Transactional(readOnly = true)
    public List<Incident> listAll() {
        return incidentRepository.findAllByOrderByDetectedAtDesc();
    }
}
