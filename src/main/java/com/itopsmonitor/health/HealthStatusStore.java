package com.itopsmonitor.health;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class HealthStatusStore {

    private final Map<String, HealthCheckResult> latestByName = new ConcurrentHashMap<>();

    public void save(HealthCheckResult result) {
        latestByName.put(result.name(), result);
    }

    public List<HealthCheckResult> findAll() {
        return new ArrayList<>(latestByName.values());
    }

    public void clear() {
        latestByName.clear();
    }
}
