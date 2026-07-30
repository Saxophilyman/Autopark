package org.example.autopark.trackingRef.port;


import org.example.autopark.trackingRef.model.VehicleRef;

/**
 * Граница поиска автомобиля для сценариев движения.
 */
public interface VehicleLookupPort {

    VehicleRef requireById(Long vehicleId);

    VehicleRef requireByLicensePlate(String licensePlate);
}
