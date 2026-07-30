package org.example.autopark.trackingRef.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Независимое телеметрическое наблюдение автомобиля.
 * Объект не связан с JPA, GPX или конкретным внешним сервисом маршрутизации.
 */
public record TelemetryPoint(
        Instant timestamp,
        double latitude,
        double longitude
) {

    public TelemetryPoint {
        Objects.requireNonNull(timestamp, "Время GPS-точки обязательно");

        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(
                    "Некорректная широта: " + latitude
            );
        }

        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "Некорректная долгота: " + longitude
            );
        }
    }
}
