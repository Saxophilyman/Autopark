package org.example.autopark.trip;

import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointDto;
import org.example.autopark.GPS.GpsPointMapper;
import org.example.autopark.GPS.GpsPointsRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TripService {
    private final TripRepository tripRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final GpsPointsRepository gpsPointsRepository;
    private final GpsPointMapper gpsPointMapper;

    @Autowired
    public TripService(TripRepository tripRepository, EnterpriseRepository enterpriseRepository, GpsPointsRepository gpsPointsRepository, GpsPointMapper gpsPointMapper) {
        this.tripRepository = tripRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.gpsPointsRepository = gpsPointsRepository;
        this.gpsPointMapper = gpsPointMapper;
    }

    public List<GpsPointDto> getTrips(Long vehicleId, String start, String end) {
        // Получаем временную зону предприятия по vehicleId
        String timeZone = Optional.ofNullable(enterpriseRepository.findTimeZoneByVehicleId(vehicleId))
                .orElse("UTC");
        //конвертируем заданное время в UTC, чтобы корректно найти во времени данные по предприятию
        Instant startTime = convertToUTC(start, timeZone);
        Instant endTime = convertToUTC(end, timeZone);

        List<GpsPoint> pointsOfTrips = gpsPointsRepository.findPointsByTripsAndVehicle(vehicleId, startTime, endTime);

        // Конвертируем в DTO с учетом временной зоны предприятия
        return  pointsOfTrips.stream()
                .map(point -> gpsPointMapper.toDto(point, timeZone))
                .collect(Collectors.toList());
    }

    private Instant convertToUTC(String localDateTimeStr, String timezone) {
        ZoneId enterpriseZone = ZoneId.of(timezone);
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return localDateTime.atZone(enterpriseZone).toInstant();
    }

    @Transactional
    public void save(Trip trip) {
        tripRepository.save(trip);
    }
}
