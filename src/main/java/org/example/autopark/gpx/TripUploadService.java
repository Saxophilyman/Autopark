package org.example.autopark.gpx;

import lombok.RequiredArgsConstructor;
import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointDto;
import org.example.autopark.GPS.GpsPointMapper;
import org.example.autopark.GPS.GpsPointsRepository;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trip.Trip;
import org.example.autopark.trip.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripUploadService {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final GpsPointsRepository gpsPointsRepository;
    private final GpsPointMapper gpsPointMapper;

    @Transactional
    public void uploadTripFromGpx(String licensePlate, LocalDateTime start, LocalDateTime end, MultipartFile gpxFile) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new IllegalArgumentException("Дата начала должна быть раньше даты окончания");
        }

        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new IllegalArgumentException("Машина с номером %s не найдена".formatted(licensePlate)));

        Instant startInstant = start.toInstant(ZoneOffset.UTC);
        Instant formEndInstant = end.toInstant(ZoneOffset.UTC); // исходное "UI"-время окончания

        if (tripRepository.existsByVehicleAndPeriodOverlap(vehicle.getVehicleId(), startInstant, formEndInstant)) {
            throw new IllegalArgumentException("Поездка пересекается с уже существующей поездкой");
        }

        List<GpsPointDto> points;
        try (InputStream is = gpxFile.getInputStream()) {
            points = GpxParser.parseGpx(is, start, end);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения GPX-файла: " + e.getMessage(), e);
        }

        if (points.isEmpty()) {
            throw new IllegalArgumentException("GPX-файл не содержит допустимых точек маршрута");
        }

        List<GpsPoint> gpsEntities = points.stream()
                .map(dto -> gpsPointMapper.toEntity(dto, vehicle))
                .toList();

        gpsPointsRepository.saveAll(gpsEntities);

        // Находим фактическое время окончания по последней GPS-точке
        Instant actualEndInstant = gpsEntities.stream()
                .map(GpsPoint::getTimestamp)
                .max(Instant::compareTo)
                .orElse(formEndInstant); // fallback если вдруг нет точек

        Trip trip = new Trip(vehicle, startInstant, actualEndInstant);
        tripRepository.save(trip);
    }

}
