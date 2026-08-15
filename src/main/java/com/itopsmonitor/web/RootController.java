package com.itopsmonitor.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
                "service", "it-ops-monitor",
                "status", "up",
                "message", "IT ops monitor scaffold — health checks and ticketing coming next"
        );
    }
}
