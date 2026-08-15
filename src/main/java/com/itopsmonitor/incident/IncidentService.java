package com.itopsmonitor.incident;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itopsmonitor.health.HealthCheckResult;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    /**
     * Opens an incident only when none is already OPEN for this target (avoids spam on every poll).
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
        return Optional.of(saved);
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
        log.info("Incident RESOLVED id={} target={}", saved.getId(), saved.getTargetName());
        return Optional.of(saved);
    }

    @Transactional(readOnly = true)
    public List<Incident> listAll() {
        return incidentRepository.findAllByOrderByDetectedAtDesc();
    }
}
