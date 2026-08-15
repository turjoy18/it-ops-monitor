package com.itopsmonitor.incident;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String targetName;

    @Column(nullable = false, length = 512)
    private String targetUrl;

    private Integer httpStatus;

    private Long latencyMs;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentStatus status;

    @Column(nullable = false)
    private Instant detectedAt;

    private Instant resolvedAt;

    protected Incident() {
    }

    public Incident(
            String targetName,
            String targetUrl,
            Integer httpStatus,
            Long latencyMs,
            String message,
            IncidentStatus status,
            Instant detectedAt
    ) {
        this.targetName = targetName;
        this.targetUrl = targetUrl;
        this.httpStatus = httpStatus;
        this.latencyMs = latencyMs;
        this.message = message;
        this.status = status;
        this.detectedAt = detectedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public String getMessage() {
        return message;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void resolve(Instant at) {
        this.status = IncidentStatus.RESOLVED;
        this.resolvedAt = at;
    }
}
