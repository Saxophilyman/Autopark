package org.example.autopark.trackingRef.application.generation;

import lombok.RequiredArgsConstructor;

import org.example.autopark.geo.GeoPoint;
import org.example.autopark.routing.port.RouteProvider;
import org.example.autopark.trackingRef.application.register.RegisterVehicleMovementCommand;
import org.example.autopark.trackingRef.application.register.RegisterVehicleMovementUseCase;
import org.example.autopark.trackingRef.model.TelemetrySeries;
import org.example.autopark.trackingRef.model.TripPeriod;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * Служебный сценарий генерации тестового движения.
 *
 * <p>Сервис выбирает границы маршрута и подготавливает синтетическую
 * телеметрию, но не создаёт JPA-сущности и не работает с репозиториями.</p>
 */
@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class TrackGenService {

    private static final Duration POINT_INTERVAL = Duration.ofSeconds(10);

    private final RouteProvider routeProvider;
    private final FixedIntervalTelemetryBuilder telemetryBuilder;
    private final RegisterVehicleMovementUseCase registerVehicleMovement;

    double centerLongitude = 37.614720;
    double centerLatitude = 55.757071;
    double radius = 6; // в км

    public void generate(GenerateTrackCommand command) {
        GeoPoint start = generateStart();
        GeoPoint end = generateEnd(start, command.lengthOfTrack());

        List<GeoPoint> route = routeProvider.getRoute(
                start.getLng(),
                start.getLat(),
                end.getLng(),
                end.getLat()
        );

        if (route == null || route.isEmpty()) {
            throw new IllegalStateException(
                    "Не удалось построить маршрут для генерации трека "
                            + "(пустой ответ от сервиса маршрутизации)"
            );
        }

        Instant startTimestamp =
                GenerateRandomTimeForDate.generateRandomTimeForDate(
                        command.date()
                );

        TelemetrySeries telemetry = telemetryBuilder.build(
                route,
                startTimestamp,
                POINT_INTERVAL
        );

        TripPeriod tripPeriod = new TripPeriod(
                telemetry.firstTimestamp(),
                telemetry.lastTimestamp()
        );

        registerVehicleMovement.execute(
                new RegisterVehicleMovementCommand(
                        command.vehicleId(),
                        tripPeriod,
                        telemetry
                )
        );
    }

    public GeoPoint generateStart() {
        Random random = new Random();
        double angle = 2 * Math.PI * random.nextDouble();
        double r = radius * Math.sqrt(random.nextDouble());

        double latRad = Math.toRadians(centerLatitude);

        double longitude = centerLongitude
                + r * Math.cos(angle) / (111.32 * Math.cos(latRad));
        double latitude = centerLatitude
                + r * Math.sin(angle) / 111.32;

        return new GeoPoint(latitude, longitude);
    }

    public GeoPoint generateEnd(
            GeoPoint start,
            int lengthOfTrack
    ) {
        double degreesPerKm = 0.0089;
        double angle = Math.random() * 2 * Math.PI;

        double latRad = Math.toRadians(start.getLat());

        double deltaLatitude =
                lengthOfTrack * degreesPerKm * Math.cos(angle);
        double deltaLongitude =
                lengthOfTrack * degreesPerKm * Math.sin(angle)
                        / Math.cos(latRad);

        double latitude = start.getLat() + deltaLatitude;
        double longitude = start.getLng() + deltaLongitude;

        return new GeoPoint(latitude, longitude);
    }
}
