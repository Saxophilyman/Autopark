package org.example.autopark.trackingRef.application.generation;

import java.util.Objects;

/**
 * Прикладные параметры генерации синтетического движения автомобиля.
 */
public record GenerateTrackCommand(
        Long vehicleId,
        int lengthOfTrack,
        String date
) {

    public GenerateTrackCommand {
        Objects.requireNonNull(vehicleId, "ID автомобиля обязателен");
        Objects.requireNonNull(date, "Дата обязательна");

        if (vehicleId <= 0) {
            throw new IllegalArgumentException(
                    "ID автомобиля должен быть положительным"
            );
        }

        if (lengthOfTrack < 1) {
            throw new IllegalArgumentException(
                    "Длина маршрута должна быть не меньше 1 км"
            );
        }

        if (date.isBlank()) {
            throw new IllegalArgumentException("Дата обязательна");
        }
    }
}
