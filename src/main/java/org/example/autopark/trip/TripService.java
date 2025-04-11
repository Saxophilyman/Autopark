package org.example.autopark.trip;

import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointDto;
import org.example.autopark.GPS.GpsPointMapper;
import org.example.autopark.GPS.GpsPointsRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class TripService {
    private final TripRepository tripRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final GpsPointsRepository gpsPointsRepository;
    private final GpsPointMapper gpsPointMapper;
    private final TripMapper tripMapper;

    @Autowired
    public TripService(TripRepository tripRepository, EnterpriseRepository enterpriseRepository, GpsPointsRepository gpsPointsRepository, GpsPointMapper gpsPointMapper, TripMapper tripMapper) {
        this.tripRepository = tripRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.gpsPointsRepository = gpsPointsRepository;
        this.gpsPointMapper = gpsPointMapper;
        this.tripMapper = tripMapper;
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

//    private Instant convertToUTC(String localDateTimeStr, String timezone) {
//        ZoneId enterpriseZone = ZoneId.of(timezone);
//        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//
//        return localDateTime.atZone(enterpriseZone).toInstant();
//    }

    //можно вынести в отдельный класс ка утилиту
    private Instant convertToUTC(String localDateTimeStr, String timezone) {
        ZoneId enterpriseZone = ZoneId.of(timezone);

        LocalDateTime localDateTime;
        if (localDateTimeStr.length() == 10) { // Если формат yyyy-MM-dd, добавляем время
            localDateTime = LocalDate.parse(localDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        } else {
            localDateTime = LocalDateTime.parse(localDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        return localDateTime.atZone(enterpriseZone).toInstant();
    }


    public List<TripDTO> getOnlyTrips(Long vehicleId, String startTripDate, String endTripDate) {
        // Получаем временную зону предприятия по vehicleId
        String timeZone = Optional.ofNullable(enterpriseRepository.findTimeZoneByVehicleId(vehicleId))
                .orElse("UTC");
        //конвертируем заданное время в UTC, чтобы корректно найти во времени данные по предприятию
        Instant startTime = convertToUTC(startTripDate, timeZone);
        Instant endTime = convertToUTC(endTripDate, timeZone);

        // 1. Загружаем все поездки разом
        List<Trip> tripList = tripRepository.findTripsWithinRange(vehicleId, startTime, endTime);
        if (tripList.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Извлекаем startDate и endDate из всех поездок
        Set<Instant> timestamps = tripList.stream()
                .flatMap(trip -> Stream.of(trip.getStartDate(), trip.getEndDate()))
                .collect(Collectors.toSet());

        // 3. Загружаем только нужные GPS точки одним SQL-запросом
        List<GpsPoint> gpsPoints = gpsPointsRepository.findGpsPointsForTrips(vehicleId, timestamps);

        // 4. Создаем мапу для быстрого поиска точек
        Map<Instant, GpsPoint> gpsPointsMap = gpsPoints.stream()
                .collect(Collectors.toMap(GpsPoint::getTimestamp, point -> point));

        // 5. Создаем DTO из всех поездок
        return tripList.stream()
                .map(trip -> tripMapper.toDTO(
                        trip,
                        timeZone,
                        gpsPointsMap.get(trip.getStartDate()), // GPS точка старта
                        gpsPointsMap.get(trip.getEndDate())    // GPS точка конца
                ))
                .collect(Collectors.toList());
    }

    public List<GpsPointDto> getTrackByTripId(Long tripId) {
        // Ищем поездку
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Поездка не найдена"));

        // Получаем временную зону предприятия (если её нет, используем UTC)
        String timeZone = Optional.ofNullable(
                trip.getVehicleOfTrip().getEnterpriseOwnerOfVehicle().getTimeZone()
        ).orElse("UTC");

        // Запрашиваем GPS-точки по времени поездки
        List<GpsPoint> gpsPoints = gpsPointsRepository.findTrackByVehicleAndTimeRange(
                trip.getVehicleOfTrip().getVehicleId(),
                trip.getStartDate(),
                trip.getEndDate()
        );

        if (gpsPoints.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GPS-данные для поездки не найдены");
        }

        return gpsPoints.stream()
                .map(point -> gpsPointMapper.toDto(point, timeZone))
                .collect(Collectors.toList());
    }


    public List<GpsPoint> getFullTrackEntitiesByTripId(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        return gpsPointsRepository.findByVehicleAndTimeRange(
                trip.getVehicleOfTrip().getVehicleId(),
                trip.getStartDate(),
                trip.getEndDate()
        );
    }


    @Transactional
    public void save(Trip trip) {
        tripRepository.save(trip);
    }
}
