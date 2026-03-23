package org.example.autopark.appUtil.trackGeneration;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.gps.GenerateRandomTimeForDate;
import org.example.autopark.gps.GpsPoint;
import org.example.autopark.gps.GpsPointsService;
import org.example.autopark.service.VehicleService;
import org.example.autopark.trip.Trip;
import org.example.autopark.trip.TripService;
import org.locationtech.jts.geom.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Profile("!reactive")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackGenService {

    private final VehicleService vehicleService;
    private final GpsPointsService gpsPointsService;
    private final TripService tripService;

    private final OpenRouteServiceClient orsClient;

    double centerLongitude = 37.614720;
    double centerLatitude = 55.757071;
    double radius = 6; // в км

    // Переиспользуем GeometryFactory
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Совместимый со старым кодом метод.
     * Делегирует реальный HTTP-вызов клиенту OpenRouteServiceClient.
     *
     * Используется в импортерах (TripImportHelper) и других местах,
     * где нужен просто маршрут по двум точкам.
     */
    public List<GpsPointCoord> getRouting(double startLong,
                                          double startLat,
                                          double endLong,
                                          double endLat) {
        return orsClient.getRoute(startLong, startLat, endLong, endLat);
    }

    @Transactional
    public void generate(TrackGenDTO request) {
        int lengthOfTrack = request.getLengthOfTrack();

        GpsPointCoord start = generateStart();
        GpsPointCoord end = generateEnd(start, lengthOfTrack);

        Vehicle vehicle = vehicleService.findOne(request.getIdVehicle());

        // Внешний вызов теперь через клиент (RestTemplate -> interceptor метрик)
        List<GpsPointCoord> track = orsClient.getRoute(
                start.getLng(), start.getLat(),
                end.getLng(), end.getLat()
        );

        if (track == null || track.isEmpty()) {
            throw new IllegalStateException(
                    "Не удалось построить маршрут для генерации трека (пустой ответ от сервиса маршрутизации)"
            );
        }

        List<GpsPoint> points = new ArrayList<>();

        Instant startTimestamp = GenerateRandomTimeForDate.generateRandomTimeForDate(request.getDate());
        Instant currentTimestamp = startTimestamp;

        for (GpsPointCoord pointOfTrack : track) {
            GpsPoint point = new GpsPoint();
            point.setVehicleIdForGps(vehicle);

            Coordinate coordinate = new Coordinate(pointOfTrack.getLng(), pointOfTrack.getLat());
            Point coord = geometryFactory.createPoint(coordinate);
            point.setLocation(coord);

            point.setTimestamp(currentTimestamp);

            points.add(point);
            currentTimestamp = currentTimestamp.plusSeconds(10);
        }

        gpsPointsService.saveAll(points);

        Instant timeOfStart = points.get(0).getTimestamp();
        Instant timeOfEnd = points.get(points.size() - 1).getTimestamp();

        Trip trip = new Trip(vehicle, timeOfStart, timeOfEnd);
        tripService.save(trip);
    }

    public GpsPointCoord generateStart() {
        Random random = new Random();
        double angle = 2 * Math.PI * random.nextDouble();
        double r = radius * Math.sqrt(random.nextDouble());

        double latRad = Math.toRadians(centerLatitude);

        double longitude = centerLongitude + r * Math.cos(angle) / (111.32 * Math.cos(latRad));
        double latitude = centerLatitude + r * Math.sin(angle) / 111.32;

        return new GpsPointCoord(latitude, longitude);
    }

    public GpsPointCoord generateEnd(GpsPointCoord start, int lengthOfTrack) {
        double degreesPerKm = 0.0089;
        double angle = Math.random() * 2 * Math.PI;

        double latRad = Math.toRadians(start.getLat());

        double deltaLatitude = lengthOfTrack * degreesPerKm * Math.cos(angle);
        double deltaLongitude = lengthOfTrack * degreesPerKm * Math.sin(angle) / Math.cos(latRad);

        double latitude = start.getLat() + deltaLatitude;
        double longitude = start.getLng() + deltaLongitude;

        return new GpsPointCoord(latitude, longitude);
    }
}
