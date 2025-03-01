package org.example.autopark.GPS;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class GpsPointMapper {
    private final ModelMapper modelMapper;

    @Autowired
    public GpsPointMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

        //настраиваем маппинг, потому что modelMapper не умеет конвертировать сложные объекты
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


}
