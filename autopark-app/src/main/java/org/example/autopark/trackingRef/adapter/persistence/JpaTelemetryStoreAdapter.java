package org.example.autopark.trackingRef.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.gps.GpsPoint;
import org.example.autopark.gps.GpsPointsRepository;

import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trackingRef.model.TelemetryPoint;
import org.example.autopark.trackingRef.model.TelemetrySeries;
import org.example.autopark.trackingRef.model.VehicleRef;
import org.example.autopark.trackingRef.port.TelemetryStore;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!reactive")
@RequiredArgsConstructor
public class JpaTelemetryStoreAdapter implements TelemetryStore {

    private final VehicleRepository vehicleRepository;
    private final GpsPointsRepository gpsPointsRepository;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public void append(VehicleRef vehicleRef, TelemetrySeries telemetry) {
        Vehicle vehicle = vehicleRepository.getReferenceById(vehicleRef.id());

        List<GpsPoint> entities = telemetry.points().stream()
                .map(point -> toEntity(point, vehicle))
                .toList();

        gpsPointsRepository.saveAll(entities);
    }

    private GpsPoint toEntity(TelemetryPoint source, Vehicle vehicle) {
        GpsPoint entity = new GpsPoint();
        entity.setVehicleIdForGps(vehicle);
        entity.setTimestamp(source.timestamp());
        entity.setLocation(
                geometryFactory.createPoint(
                        new Coordinate(
                                source.longitude(),
                                source.latitude()
                        )
                )
        );
        return entity;
    }
}
