package org.example.autopark.monitoring;

/**
 * Отправка доменного события мониторинга "наружу"
 * (в notify-service — через Kafka или HTTP, в зависимости от реализации).
 */
public interface MonitoringAlertSender {
    void send(MonitoringDomainEvent event);
}
