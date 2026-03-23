package org.example.autopark.monitoring;

import lombok.RequiredArgsConstructor;
import org.example.autopark.kafka.publisher.MonitoringEventPublisher;
import org.example.events.MonitoringEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaMonitoringAlertSender implements MonitoringAlertSender {

    private final MonitoringEventPublisher publisher;

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
        publisher.publish(evt);
    }
}
