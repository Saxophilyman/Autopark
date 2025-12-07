package org.example.autopark.kafka;

import lombok.RequiredArgsConstructor;
import org.example.events.VehicleEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class VehicleDomainEventHandler {

    private final VehicleEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(VehicleDomainEvent d) {
        VehicleEvent evt = new VehicleEvent(
                d.vehicleGuid(),
                d.enterpriseGuid(),
                d.managerId(),
                VehicleEvent.Action.valueOf(d.action().name()),
                Instant.now(),
                d.licensePlate(),
                d.vehicleName(),
                d.enterpriseName()
        );
        publisher.publish(evt);
    }
}
