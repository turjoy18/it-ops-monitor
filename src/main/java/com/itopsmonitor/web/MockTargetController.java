package com.itopsmonitor.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Built-in mock dependency endpoints so the monitor can demo UP and DOWN without external services.
 */
@RestController
@RequestMapping("/mocks")
public class MockTargetController {

    private final OpsMocksProperties mocksProperties;

    public MockTargetController(OpsMocksProperties mocksProperties) {
        this.mocksProperties = mocksProperties;
    }

    @GetMapping("/payments")
    public Map<String, String> payments() {
        return Map.of("service", "payments-api", "status", "UP");
    }

    @GetMapping("/fx")
    public Map<String, String> fx() {
        return Map.of("service", "fx-api", "status", "UP");
    }

    @GetMapping("/ledger")
    public ResponseEntity<Map<String, String>> ledger() {
        if (mocksProperties.isLedgerForceDown()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("service", "ledger-api", "status", "DOWN"));
        }
        return ResponseEntity.ok(Map.of("service", "ledger-api", "status", "UP"));
    }
}
