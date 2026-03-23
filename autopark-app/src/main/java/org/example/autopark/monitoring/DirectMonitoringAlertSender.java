package org.example.autopark.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.MonitoringEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

/**
 * Отправитель алертов напрямую в notify-service по HTTP.
 * Используется, когда Kafka выключена (app.kafka.enabled=false).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class DirectMonitoringAlertSender implements MonitoringAlertSender {

    private final RestTemplate restTemplate;

    @Value("${notify.base-url:http://localhost:8082}")
    private String notifyBaseUrl;

    @Override
    public void send(MonitoringDomainEvent d) {
        MonitoringEvent evt = new MonitoringEvent(
                MonitoringEvent.Type.valueOf(d.type().name()),
                MonitoringEvent.Severity.valueOf(d.severity().name()),
                Instant.now(),
                "autopark-app",
                d.message(),
                d.attributes()
        );

        String url = notifyBaseUrl + "/internal/monitoring/alert";

        try {
            ResponseEntity<Void> resp =
                    restTemplate.postForEntity(url, evt, Void.class);
            log.debug("Sent monitoring alert directly to notify-service: status={}",
                    resp.getStatusCode());
        } catch (RestClientException ex) {
            // мониторинг не должен падать приложению
            log.warn("Failed to send monitoring alert directly to {}", url, ex);
        }
    }
}
