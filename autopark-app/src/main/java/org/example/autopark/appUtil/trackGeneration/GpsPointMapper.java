package org.example.autopark.appUtil.trackGeneration;

import org.example.autopark.gps.GpsPoint;
import org.example.autopark.entity.Vehicle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
@Deprecated
public class GpsPointMapper {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static GpsPoint toEntity(GpsPointCoord dto, Vehicle vehicle) {
        GpsPoint entity = new GpsPoint();
        entity.setVehicleIdForGps(vehicle);

        // Создаём геометрический объект Point
        Coordinate coordinate = new Coordinate(dto.getLng(), dto.getLat()); // lng (X), lat (Y)
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(4326); // Устанавливаем SRID (пространственная привязка)

        entity.setLocation(point);
        entity.setTimestamp(Instant.now()); // Время в UTC
        return entity;
    }

    public static GpsPointCoord toDto(GpsPoint entity) {
        Point location = entity.getLocation();
        return new GpsPointCoord(location.getY(), location.getX()); // lat (Y), lng (X)
    }

}
