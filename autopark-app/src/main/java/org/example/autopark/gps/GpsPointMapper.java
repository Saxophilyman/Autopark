package org.example.autopark.gps;

import org.example.autopark.entity.Vehicle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class GpsPointMapper {
    private final ModelMapper modelMapper;
    private final GeometryFactory geometryFactory;

    @Autowired
    public GpsPointMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326); // EPSG:4326 = WGS84

        //настраиваем маппинг, потому что modelMapper не умеет конвертировать сложные объекты
        // Маппинг GpsPoint → GpsPointDto
        modelMapper.addMappings(new PropertyMap<GpsPoint, GpsPointDto>() {
            @Override
            protected void configure() {
                map().setVehicleId(String.valueOf(source.getVehicleIdForGps().getVehicleId()));
                map().setLatitude(source.getLocation().getY());
                map().setLongitude(source.getLocation().getX());
            }
        });
    }

    /**
     * Конвертирует `GpsPoint` в `GpsPointDto`, учитывая локальную временную зону.
     * @param gpsPoint GPS-точка из базы (в UTC)
     * @param timeZone Временная зона предприятия
     * @return DTO с локальным временем
     */

    public GpsPointDto toDto(GpsPoint gpsPoint, String timeZone) {
        GpsPointDto dto = modelMapper.map(gpsPoint, GpsPointDto.class);

        // Преобразуем UTC → локальное время
        ZoneId zoneId  = ZoneId.of(timeZone);
        ZonedDateTime localTime = ZonedDateTime.ofInstant(gpsPoint.getTimestamp(), zoneId);
        dto.setTimestamp(localTime.toLocalDateTime()); // Теперь это время в локальной зоне предприятия
        return dto;
    }

    /**
     * Конвертирует GpsPointDto → GpsPoint с построением геометрии и UTC-временем.
     * @param dto DTO с координатами
     * @param vehicle сущность Vehicle
     * @return GpsPoint
     */
    public GpsPoint toEntity(GpsPointDto dto, Vehicle vehicle) {
        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));

        GpsPoint entity = new GpsPoint();
        entity.setVehicleIdForGps(vehicle);
        entity.setLocation(point);
        entity.setTimestamp(dto.getTimestamp().atZone(ZoneId.of("UTC")).toInstant());

        return entity;
    }

}
