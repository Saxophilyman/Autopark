package org.example.autopark.exportAndImport.byGuid.guidDto;

import org.example.autopark.gps.GpsPoint;
import org.example.autopark.exportAndImport.util.ReverseGeocodingService;
import org.example.autopark.trip.Trip;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@Profile("!reactive")
public class TripGuidExportMapper {

    private final ReverseGeocodingService reverseGeocodingService;

    public TripGuidExportMapper(ReverseGeocodingService reverseGeocodingService) {
        this.reverseGeocodingService = reverseGeocodingService;
    }

    //временную зону не нужно?
    public TripGuidExportDto toDto(Trip trip, GpsPoint gpsStart, GpsPoint gpsEnd, String enterpriseTimeZone) {
        TripGuidExportDto dto = new TripGuidExportDto();
        dto.setGuid(trip.getGuid());

        ZoneId zoneId = ZoneId.of(enterpriseTimeZone);

        ZonedDateTime startTimeZoned = ZonedDateTime.ofInstant(trip.getStartDate(), zoneId);
        ZonedDateTime endTimeZoned = ZonedDateTime.ofInstant(trip.getEndDate(), zoneId);

        dto.setStartTime(startTimeZoned.toLocalDateTime());
        dto.setEndTime(endTimeZoned.toLocalDateTime());
        dto.setDuration(trip.getDuration());
        dto.setStartLocationInString(
                gpsStart != null && gpsStart.getLocation() != null
                        ? reverseGeocodingService.reverseGeocode(gpsStart.getLocation())
                        : "Не определено"
        );

        dto.setEndLocationInString(
                gpsEnd != null && gpsEnd.getLocation() != null
                        ? reverseGeocodingService.reverseGeocode(gpsEnd.getLocation())
                        : "Не определено"
        );

       //dto.setGpsPoints(null); // если нужно — добавим трек позже

        return dto;
    }

    public TripGuidExportDto toDtoWithGps(Trip trip, List<GpsPoint> gpsPoints, String timeZone) {
        TripGuidExportDto dto = toDto(trip, gpsPoints.get(0), gpsPoints.get(gpsPoints.size() - 1), timeZone);

        List<VehicleExportDtoByGuid.GpsPointGuidDto> gpsPointDtos = gpsPoints.stream().map(point -> {
            VehicleExportDtoByGuid.GpsPointGuidDto d = new VehicleExportDtoByGuid.GpsPointGuidDto();
            d.setGuid(point.getGuid());
            d.setLatitude(point.getLocation().getY());
            d.setLongitude(point.getLocation().getX());
            d.setTimestamp(point.getTimestamp().toString());
            //d.setAddress(null); // можно добавить геокодер
            return d;
        }).toList();

        dto.setGpsPoints(gpsPointDtos);
        return dto;
    }

}
