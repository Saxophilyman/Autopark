package org.example.autopark.tracking.application.generation;

import org.example.autopark.geo.GeoPoint;
import org.example.autopark.trackingRef.application.generation.FixedIntervalTelemetryBuilder;
import org.example.autopark.trackingRef.model.TelemetrySeries;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedIntervalTelemetryBuilderTest {

    private final FixedIntervalTelemetryBuilder builder =
            new FixedIntervalTelemetryBuilder();

    @Test
    void assignsExplicitIntervalToEveryRoutePoint() {
        Instant start = Instant.parse("2026-04-10T10:00:00Z");

        TelemetrySeries telemetry = builder.build(
                List.of(
                        new GeoPoint(55.75, 37.61),
                        new GeoPoint(55.76, 37.62),
                        new GeoPoint(55.77, 37.63)
                ),
                start,
                Duration.ofSeconds(10)
        );

        assertEquals(3, telemetry.points().size());
        assertEquals(start, telemetry.points().get(0).timestamp());
        assertEquals(
                start.plusSeconds(10),
                telemetry.points().get(1).timestamp()
        );
        assertEquals(
                start.plusSeconds(20),
                telemetry.points().get(2).timestamp()
        );
    }

    @Test
    void rejectsRouteThatCannotDescribeMovement() {
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(
                        List.of(new GeoPoint(55.75, 37.61)),
                        Instant.parse("2026-04-10T10:00:00Z"),
                        Duration.ofSeconds(10)
                )
        );
    }
}
