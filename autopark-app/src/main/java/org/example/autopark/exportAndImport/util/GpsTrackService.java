package org.example.autopark.exportAndImport.util;

import lombok.RequiredArgsConstructor;
import org.example.autopark.gps.GpsPoint;
import org.example.autopark.gps.GpsPointsRepository;
import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;
import org.example.autopark.entity.Vehicle;
import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class GpsTrackService {

    private final GpsPointsRepository gpsPointsRepository;
    private final GeometryUtils geometryUtils;

    public Instant generateAndSaveTrack(List<GpsPointCoord> track, Instant startTime, Vehicle vehicle) {
        Instant currentTime = startTime;
        Instant lastTime = startTime;

        List<GpsPoint> gpsPoints = new ArrayList<>();

        for (GpsPointCoord coord : track) {
            Point point = geometryUtils.toPoint(coord);

            GpsPoint gpsPoint = new GpsPoint();
            gpsPoint.setVehicleIdForGps(vehicle);
            gpsPoint.setTimestamp(currentTime);
            gpsPoint.setLocation(point);

            gpsPoints.add(gpsPoint);

            lastTime = currentTime;
            currentTime = currentTime.plusSeconds(10);
        }

        gpsPointsRepository.saveAll(gpsPoints); // сохраняем одним запросом
        return lastTime;
    }
}

