package org.example.notify.monitoringalert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.MonitoringEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"app.kafka.enabled", "notify.features.monitoring-events.enabled"},
        havingValue = "true"
)
public class MonitoringEventListener {

    private final MonitoringAlertService alertService;

    @KafkaListener(
            topics = "monitoring.alerts",
            groupId = "notify-service",
            properties = {
                    "spring.json.value.default.type=org.example.events.MonitoringEvent",
                    "spring.json.trusted.packages=org.example.events"
            }
    )
    public void onMonitoringEvent(MonitoringEvent evt) {
        log.info("monitoring.alerts: {}", evt);
        alertService.handle(evt);
    }
}
