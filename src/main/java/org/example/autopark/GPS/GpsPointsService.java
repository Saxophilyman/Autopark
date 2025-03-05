package org.example.autopark.GPS;

import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GpsPointsService {
    private final VehicleRepository vehicleRepository;
    private final GpsPointsRepository gpsPointsRepository;
    private final GpsPointMapper gpsPointMapper;
    private final EnterpriseRepository enterpriseRepository;

    @Autowired
    public GpsPointsService(VehicleRepository vehicleRepository, GpsPointsRepository gpsPointsRepository, GpsPointMapper gpsPointMapper, EnterpriseRepository enterpriseRepository) {
        this.vehicleRepository = vehicleRepository;
        this.gpsPointsRepository = gpsPointsRepository;
        this.gpsPointMapper = gpsPointMapper;
        this.enterpriseRepository = enterpriseRepository;
    }

    /**
     * Получение трека автомобиля с автоматическим определением временной зоны предприятия.
     *
     * @param vehicleId ID автомобиля
     * @param start     Начало интервала (UTC)
     * @param end       Конец интервала (UTC)
     * @return Список точек трека в локальном времени
     * смотри объяснение в классе
     */
    public TrackResponse getTrack(Long vehicleId, String start, String end, String format) {
        // Получаем временную зону предприятия по vehicleId
        String timeZone = Optional.ofNullable(enterpriseRepository.findTimeZoneByVehicleId(vehicleId))
                .orElse("UTC");
        //конвертируем заданное время в UTC, чтобы корректно найти во времени данные по предприятию
        Instant startInstant = convertToUTC(start, timeZone);
        Instant endInstant = convertToUTC(end, timeZone);

        //ищем конкретные точки с UTC временем
        List<GpsPoint> points = gpsPointsRepository.findTrackByVehicleAndTimeRange(vehicleId, startInstant, endInstant);

        // Конвертируем в DTO с учетом временной зоны предприятия
        List<GpsPointDto> dtoPoints = points.stream()
                .map(point -> gpsPointMapper.toDto(point, timeZone))
                .collect(Collectors.toList());

        //проверяем формат. Если он geoJson конвертируем его в geojson
        if ("geojson".equalsIgnoreCase(format)) {
            return new TrackResponse("geojson", GeoJsonConverter.convertToGeoJSON(dtoPoints));
        }
        return new TrackResponse("json", dtoPoints);
    }


    private Instant convertToUTC(String localDateTimeStr, String timezone) {
        ZoneId enterpriseZone = ZoneId.of(timezone);
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return localDateTime.atZone(enterpriseZone).toInstant();
    }

    @Transactional
    public void saveAll(List<GpsPoint> pointsGps) {
        gpsPointsRepository.saveAll(pointsGps);
    }

    @Transactional
    public void save(GpsPoint pointsGps) {
        gpsPointsRepository.save(pointsGps);
    }
}







