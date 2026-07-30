package org.example.autopark.trackingRef.application.generation;


import org.example.autopark.geo.GeoPoint;
import org.example.autopark.trackingRef.model.TelemetryPoint;
import org.example.autopark.trackingRef.model.TelemetrySeries;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Превращает геометрию маршрута в синтетическую временную телеметрию.
 * Интервал между точками является явной политикой вызывающего сценария.
 */
@Component
public class FixedIntervalTelemetryBuilder {

    public TelemetrySeries build(
            List<GeoPoint> route,
            Instant startTime,
            Duration pointInterval
    ) {
        if (route == null || route.isEmpty()) {
            throw new IllegalArgumentException(
                    "Маршрут не содержит координат"
            );
        }

        if (route.size() < 2) {
            throw new IllegalArgumentException(
                    "Для поездки необходимы минимум две точки маршрута"
            );
        }

        if (startTime == null) {
            throw new IllegalArgumentException(
                    "Время начала маршрута обязательно"
            );
        }

        if (pointInterval == null
                || pointInterval.isZero()
                || pointInterval.isNegative()) {
            throw new IllegalArgumentException(
                    "Интервал между GPS-точками должен быть положительным"
            );
        }

        List<TelemetryPoint> points = new ArrayList<>(route.size());
        Instant currentTime = startTime;

        for (GeoPoint coordinate : route) {
            if (coordinate == null) {
                throw new IllegalArgumentException(
                        "Маршрут содержит пустую координату"
                );
            }

            points.add(
                    new TelemetryPoint(
                            currentTime,
                            coordinate.getLat(),
                            coordinate.getLng()
                    )
            );

            currentTime = currentTime.plus(pointInterval);
        }

        return new TelemetrySeries(points);
    }
}
