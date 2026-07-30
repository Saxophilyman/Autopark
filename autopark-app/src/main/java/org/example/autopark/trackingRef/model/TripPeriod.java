package org.example.autopark.trackingRef.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Временной интервал поездки.
 *
 * <p>Длительность не хранится отдельно в прикладной модели: она выводится
 * из начала и окончания, а в БД остаётся вычисляемым полем.</p>
 */
public record TripPeriod(Instant start, Instant end) {

    public TripPeriod {
        Objects.requireNonNull(start, "Начало поездки обязательно");
        Objects.requireNonNull(end, "Окончание поездки обязательно");

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Начало поездки должно быть раньше окончания"
            );
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    /**
     * Текущее правило проекта для связи поездки и GPS-точек:
     * обе временные границы включены.
     */
    public boolean containsInclusive(Instant timestamp) {
        Objects.requireNonNull(timestamp, "Время GPS-точки обязательно");
        return !timestamp.isBefore(start) && !timestamp.isAfter(end);
    }
}
