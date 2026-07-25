package org.example.autopark.monitoring;

import java.util.Map;

public record MonitoringDomainEvent(
        Type type,
        Severity severity,
        String message,
        Map<String, String> attributes
) {
    public enum Type {
        SQL_ERRORS,
        LATENCY,
        HTTP_CLIENT_ERRORS
    }

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }
}