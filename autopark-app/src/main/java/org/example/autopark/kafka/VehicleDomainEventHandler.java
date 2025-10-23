package org.example.autopark.kafka;

import lombok.RequiredArgsConstructor;
import org.example.events.VehicleEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@RequiredArgsConstructor
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
