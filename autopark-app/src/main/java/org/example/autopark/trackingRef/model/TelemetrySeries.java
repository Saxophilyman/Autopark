package org.example.autopark.trackingRef.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Упорядоченная неизменяемая последовательность GPS-наблюдений автомобиля.
 * Это не сущность БД и не коллекция, принадлежащая Trip.
 */
public record TelemetrySeries(List<TelemetryPoint> points) {

    public TelemetrySeries {
        Objects.requireNonNull(points, "Список GPS-точек обязателен");

        if (points.isEmpty()) {
            throw new IllegalArgumentException(
                    "Телеметрия должна содержать хотя бы одну точку"
            );
        }

        if (points.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Список телеметрии содержит null"
            );
        }

        points = List.copyOf(
                points.stream()
                        .sorted(Comparator.comparing(TelemetryPoint::timestamp))
                        .toList()
        );
    }

    public Instant firstTimestamp() {
        return points.get(0).timestamp();
    }

    public Instant lastTimestamp() {
        return points.get(points.size() - 1).timestamp();
    }
}
