package org.example.autopark.trackingRef.application.register;



import org.example.autopark.trackingRef.model.TelemetrySeries;
import org.example.autopark.trackingRef.model.TripPeriod;

import java.util.Objects;

/**
 * Входные данные одной прикладной операции: зарегистрировать период поездки
 * и пакет телеметрии для одного автомобиля.
 */
public record RegisterVehicleMovementCommand(
        Long vehicleId,
        TripPeriod tripPeriod,
        TelemetrySeries telemetry
) {

    public RegisterVehicleMovementCommand {
        Objects.requireNonNull(vehicleId, "ID автомобиля обязателен");
        Objects.requireNonNull(tripPeriod, "Период поездки обязателен");
        Objects.requireNonNull(telemetry, "Телеметрия обязательна");

        if (vehicleId <= 0) {
            throw new IllegalArgumentException(
                    "ID автомобиля должен быть положительным"
            );
        }
    }
}
