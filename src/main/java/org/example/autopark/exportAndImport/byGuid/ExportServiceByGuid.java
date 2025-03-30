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
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportServiceByGuid {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final TripService tripService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final TripGuidExportMapper tripGuidExportMapper;

    public VehicleExportDtoByGuid getVehicleExportDataByGuid(UUID vehicleGuid, LocalDate fromDate, LocalDate toDate) {

        Vehicle vehicle = vehicleRepository.findByGuid(vehicleGuid)
                .orElseThrow(() -> new RuntimeException("Vehicle not found by GUID"));

        Enterprise enterprise = vehicle.getEnterpriseOwnerOfVehicle();

        Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);



        List<Trip> tripsList = tripRepository.findTripsWithinRange(vehicle.getVehicleId(), from, to);
        List<TripGuidExportDto> tripsDTOList = new ArrayList<>();
        for (Trip trip : tripsList) {
            List<GpsPointDto> gpsPoints = tripService.getTrackByTripId(trip.getId());
            if (gpsPoints.size() >= 2) {
                GpsPointDto startDto = gpsPoints.get(0);
                GpsPointDto endDto = gpsPoints.get(gpsPoints.size() - 1);

                // Временные GpsPoint для передачи в маппер
                GpsPoint start = new GpsPoint();
                start.setTimestamp(startDto.getTimestamp().atZone(ZoneOffset.UTC).toInstant());
                Point startPoint = geometryFactory.createPoint(new Coordinate(startDto.getLongitude(), startDto.getLatitude()));
                start.setLocation(startPoint);

                GpsPoint end = new GpsPoint();
                end.setTimestamp(endDto.getTimestamp().atZone(ZoneOffset.UTC).toInstant());
                Point endPoint = geometryFactory.createPoint(new Coordinate(endDto.getLongitude(), endDto.getLatitude()));
                end.setLocation(endPoint);

                TripGuidExportDto dto = tripGuidExportMapper.toDto(trip, start, end, enterprise.getTimeZone());
                tripsDTOList.add(dto);
            }
        }

        return VehicleExportDtoByGuid.fromEntities(vehicle,enterprise,tripsDTOList);

    }
}
