package org.example.autopark.kafka;

import java.util.UUID;

public record VehicleDomainEvent(
        UUID vehicleGuid,
        UUID enterpriseGuid,
        Long managerId,
        Action action,
        String licensePlate,
        String vehicleName,
        String enterpriseName
) {
    public enum Action { CREATED, UPDATED, DELETED }

    // ✔️ Перегруженный конструктор для старых вызовов
    public VehicleDomainEvent(UUID vehicleGuid,
                              UUID enterpriseGuid,
                              Long managerId,
                              Action action) {
        this(vehicleGuid, enterpriseGuid, managerId, action, null, null, null);
    }
}