package org.example.autopark.GPS;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PointsGPSService {

    private final PointsGPSRepository pointsGPSRepository;

    @Autowired
    public PointsGPSService(PointsGPSRepository pointsGPSRepository) {
        this.pointsGPSRepository = pointsGPSRepository;
    }

    public List<String> findAll() {
        return pointsGPSRepository.findAll().stream()
                .map(pointGPS -> convertPointToGeoJSON(pointGPS.getLocation()))
                .toList();
    }
    private String convertPointToGeoJSON(Point point) {
        return String.format("{\"type\": \"Point\", \"coordinates\": [%f, %f]}",
                point.getX(), point.getY());
    }
}
