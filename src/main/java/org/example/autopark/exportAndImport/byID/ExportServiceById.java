package org.example.autopark.exportAndImport.byID;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointDto;
import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trip.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class ExportServiceById {
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final TripService tripService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final TripMapper tripMapper;

    public VehicleExportDtoById getVehicleExportDataById(Long vehicleId, LocalDate fromDate, LocalDate toDate){
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new RuntimeException("Vehicle not found"));

        Enterprise enterprise = vehicle.getEnterpriseOwnerOfVehicle();

        Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);


        List<Trip> tripsList = tripRepository.findTripsWithinRange(vehicleId, from, to);
        List<TripDTO> tripsDTOList = new ArrayList<>();

        for (Trip trip : tripsList) {
            List<GpsPointDto> gpsPoints = tripService.getTrackByTripId(trip.getId());
            if (gpsPoints.size() >= 2) {
                GpsPointDto startDto = gpsPoints.get(0);
                GpsPointDto endDto = gpsPoints.get(gpsPoints.size() - 1);

                GpsPoint start = new GpsPoint();
                start.setTimestamp(startDto.getTimestamp().atZone(ZoneOffset.UTC).toInstant());
                Point startPoint = geometryFactory.createPoint(new Coordinate(startDto.getLongitude(), startDto.getLatitude()));
                start.setLocation(startPoint);

                GpsPoint end = new GpsPoint();
                end.setTimestamp(endDto.getTimestamp().atZone(ZoneOffset.UTC).toInstant());
                Point endPoint = geometryFactory.createPoint(new Coordinate(endDto.getLongitude(), endDto.getLatitude()));
                end.setLocation(endPoint);

                TripDTO dto = tripMapper.toDTO(trip, enterprise.getTimeZone(), start, end);
                tripsDTOList.add(dto);
            }
        }

        return VehicleExportDtoById.fromEntities(vehicle, enterprise, tripsDTOList);
    }


}
