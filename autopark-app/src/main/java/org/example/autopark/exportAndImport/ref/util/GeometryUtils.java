package org.example.autopark.exportAndImport.ref.util;

import lombok.RequiredArgsConstructor;
import org.example.autopark.geo.GeoPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeometryUtils {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    public Point toPoint(GeoPoint coord) {
        Point point = geometryFactory.createPoint(new Coordinate(coord.getLng(), coord.getLat()));
        point.setSRID(4326);
        return point;
    }
}

