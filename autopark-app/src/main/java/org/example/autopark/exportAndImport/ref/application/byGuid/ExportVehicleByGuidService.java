package org.example.autopark.exportAndImport.ref.application.byGuid;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exception.ResourceNotFoundException;

import org.example.autopark.exportAndImport.ref.document.byGuid.TripGuidExportDto;
import org.example.autopark.exportAndImport.ref.document.byGuid.TripGuidExportMapper;
import org.example.autopark.exportAndImport.ref.document.byGuid.VehicleExportDtoByGuid;
import org.example.autopark.gps.GpsPoint;
import org.example.autopark.gps.GpsPointDto;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.trip.Trip;
import org.example.autopark.trip.TripRepository;
import org.example.autopark.trip.TripService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class ExportVehicleByGuidService {
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final TripService tripService;
    private final TripGuidExportMapper tripGuidExportMapper;
    private final EnterpriseService enterpriseService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Transactional(readOnly = true)
    public VehicleExportDtoByGuid execute(ExportVehicleByGuidCommand command) {
        Vehicle vehicle = vehicleRepository.findByGuid(command.vehicleGuid())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle with GUID=" + command.vehicleGuid() + " not found"));

        Enterprise enterprise = requireEnterprise(vehicle);
        enterpriseService.findEnterpriseForManager(command.managerId(), enterprise.getEnterpriseId());

        Instant from = command.fromDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = command.toDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<Trip> trips = tripRepository.findTripsWithinRange(vehicle.getVehicleId(), from, to);
        List<TripGuidExportDto> tripDocuments = new ArrayList<>();

        for (Trip trip : trips) {
            TripGuidExportDto tripDocument = command.trackMode() == TrackMode.FULL
                    ? createFullTripDocument(trip, enterprise)
                    : createSummaryTripDocument(trip, enterprise);

            if (tripDocument != null) {
                tripDocuments.add(tripDocument);
            }
        }

        return VehicleExportDtoByGuid.fromEntities(vehicle, enterprise, tripDocuments);
    }

    private TripGuidExportDto createFullTripDocument(Trip trip, Enterprise enterprise) {
        List<GpsPoint> points = tripService.getFullTrackEntitiesByTripId(trip.getId());
        return points.size() < 2 ? null : tripGuidExportMapper.toDtoWithGps(trip, points, enterprise.getTimeZone());
    }

    private TripGuidExportDto createSummaryTripDocument(Trip trip, Enterprise enterprise) {
        List<GpsPointDto> points = tripService.getTrackByTripId(trip.getId());
        if (points.size() < 2) {
            return null;
        }

        GpsPoint start = toEntity(points.get(0));
        GpsPoint end = toEntity(points.get(points.size() - 1));
        return tripGuidExportMapper.toDto(trip, start, end, enterprise.getTimeZone());
    }

    private Enterprise requireEnterprise(Vehicle vehicle) {
        Enterprise enterprise = vehicle.getEnterpriseOwnerOfVehicle();
        if (enterprise == null) {
            throw new IllegalStateException("Автомобиль не привязан к предприятию");
        }
        return enterprise;
    }

    private GpsPoint toEntity(GpsPointDto source) {
        GpsPoint point = new GpsPoint();
        point.setTimestamp(source.getTimestamp().atZone(ZoneOffset.UTC).toInstant());
        Point location = geometryFactory.createPoint(new Coordinate(source.getLongitude(), source.getLatitude()));
        location.setSRID(4326);
        point.setLocation(location);
        return point;
    }
}
