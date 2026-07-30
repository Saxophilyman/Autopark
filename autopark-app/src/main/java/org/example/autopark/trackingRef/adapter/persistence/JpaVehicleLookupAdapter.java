package org.example.autopark.trackingRef.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Vehicle;

import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trackingRef.model.VehicleRef;
import org.example.autopark.trackingRef.port.VehicleLookupPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!reactive")
@RequiredArgsConstructor
public class JpaVehicleLookupAdapter implements VehicleLookupPort {

    private final VehicleRepository vehicleRepository;

    @Override
    public VehicleRef requireById(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Автомобиль с ID %d не найден".formatted(vehicleId)
                ));

        return new VehicleRef(vehicle.getVehicleId());
    }

    @Override
    public VehicleRef requireByLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException(
                    "Государственный номер автомобиля обязателен"
            );
        }

        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Машина с номером %s не найдена".formatted(licensePlate)
                ));

        return new VehicleRef(vehicle.getVehicleId());
    }
}
