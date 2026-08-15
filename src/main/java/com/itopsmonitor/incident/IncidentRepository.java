package com.itopsmonitor.incident;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findFirstByTargetNameAndStatusOrderByDetectedAtDesc(
            String targetName,
            IncidentStatus status
    );

    List<Incident> findAllByOrderByDetectedAtDesc();

    List<Incident> findByStatusOrderByDetectedAtDesc(IncidentStatus status);

    long countByStatus(IncidentStatus status);
}
