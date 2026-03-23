package org.example.autopark.kafka.eventhandler;

import lombok.RequiredArgsConstructor;
import org.example.autopark.monitoring.MonitoringAlertSender;
import org.example.autopark.monitoring.MonitoringDomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonitoringDomainEventHandler {

    private final MonitoringAlertSender alertSender;

    @EventListener
    public void on(MonitoringDomainEvent event) {
        alertSender.send(event);
    }
}
