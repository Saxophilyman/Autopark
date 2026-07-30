package org.example.autopark.trackingRef.model;

import java.util.Objects;

/**
 * Минимальная ссылка на автомобиль для прикладного сценария движения.
 */
public record VehicleRef(Long id) {

    public VehicleRef {
        Objects.requireNonNull(id, "ID автомобиля обязателен");
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID автомобиля должен быть положительным"
            );
        }
    }
}
