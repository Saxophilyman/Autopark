package org.example.autopark.exportAndImport.byGuid;

import lombok.RequiredArgsConstructor;
import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointDto;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exportAndImport.byGuid.guidDto.TripGuidExportDto;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.byGuid.guidDto.TripGuidExportMapper;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trip.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class ExportServiceByGuid {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final TripService tripService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final TripGuidExportMapper tripGuidExportMapper;


    public VehicleExportDtoByGuid exportDataByGuid(UUID vehicleGuid, LocalDate fromDate, LocalDate toDate, boolean withTrack) {
        Vehicle vehicle = vehicleRepository.findByGuid(vehicleGuid)
                .orElseThrow(() -> new RuntimeException("Vehicle not found by GUID"));

        Enterprise enterprise = vehicle.getEnterpriseOwnerOfVehicle();

        Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Trip> trips = tripRepository.findTripsWithinRange(vehicle.getVehicleId(), from, to);
        List<TripGuidExportDto> tripDtos = new ArrayList<>();

        for (Trip trip : trips) {
            if (withTrack) {
                List<GpsPoint> gpsPoints = tripService.getFullTrackEntitiesByTripId(trip.getId());
                if (gpsPoints.size() < 2) continue;
                TripGuidExportDto dto = tripGuidExportMapper.toDtoWithGps(trip, gpsPoints, enterprise.getTimeZone());
                tripDtos.add(dto);
            } else {
                List<GpsPointDto> gpsPoints = tripService.getTrackByTripId(trip.getId());
                if (gpsPoints.size() < 2) continue;

                GpsPoint start = gpsPointDtoToEntity(gpsPoints.get(0));
                GpsPoint end = gpsPointDtoToEntity(gpsPoints.get(gpsPoints.size() - 1));

                TripGuidExportDto dto = tripGuidExportMapper.toDto(trip, start, end, enterprise.getTimeZone());
                tripDtos.add(dto);
            }

        }

        VehicleExportDtoByGuid dto = VehicleExportDtoByGuid.fromEntities(vehicle, enterprise, tripDtos);
        return dto;
    }
    private GpsPoint gpsPointDtoToEntity(GpsPointDto dto) {
        GpsPoint gpsPoint = new GpsPoint();
        gpsPoint.setTimestamp(dto.getTimestamp().atZone(ZoneOffset.UTC).toInstant());

        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
        point.setSRID(4326);
        gpsPoint.setLocation(point);

        return gpsPoint;
    }

    // ... остальные методы импорта и экспорта ...
}

