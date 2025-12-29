// src/main/java/org/example/events/VehicleEvent.java
package org.example.events;

import java.time.Instant;
import java.util.UUID;

public record VehicleEvent(
        UUID vehicleGuid,
        UUID enterpriseGuid,
        Long managerId,
        Action action,
        Instant occurredAt,

        // ↓ Снапшот полей, особенно нужен для DELETED
        String licensePlate,
        String vehicleName,
        String enterpriseName
) {
    public enum Action { CREATED, UPDATED, DELETED }
}
