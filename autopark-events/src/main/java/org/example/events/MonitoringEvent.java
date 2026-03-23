package org.example.events;



import java.time.Instant;
import java.util.Map;

public record MonitoringEvent (
    Type type,
    Severity severity, //уровень значимости уведомления
    Instant occurredAt,
    String source,
    String message,  // человекочитаемый текст
    Map<String, String> attributes // произвольные детали: counts, thresholds, endpoint и т.п.
){
    public enum Type {
        SQL_ERRORS,
        LATENCY,
        HTTP_CLIENT_ERRORS,
    }

    //TODO пока обозначим так, но после привяжем к реальным уровням ошибок в SQL
    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }
}
