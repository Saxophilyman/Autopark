package org.example.autopark.exportAndImport.util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;
import org.example.autopark.appUtil.trackGeneration.TrackGenService;

import org.example.autopark.entity.Vehicle;
import org.example.autopark.exportAndImport.byGuid.guidDto.TripGuidExportDto;

import org.example.autopark.trip.Trip;
import org.example.autopark.trip.TripDTO;
import org.example.autopark.trip.TripRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!reactive")
@Slf4j
public class TripImportHelper {

    private final AddressGeocodingService geocodingService;
    private final GpsTrackService gpsTrackService;
    private final TrackGenService trackGenService;
    private final TripRepository tripRepository;

    public void importTripsByDto(List<TripDTO> trips, Vehicle vehicle) {
        for (TripDTO dto : trips) {
            GpsPointCoord start = geocodingService.geocode(dto.getStartLocationInString());
            GpsPointCoord end = geocodingService.geocode(dto.getEndLocationInString());

            if (start == null || end == null) {
                log.warn("Пропущена поездка: не удалось геокодировать адреса: {} / {}", dto.getStartLocationInString(), dto.getEndLocationInString());
                continue;
            }

            List<GpsPointCoord> track = trackGenService.getRouting(start.getLng(), start.getLat(), end.getLng(), end.getLat());
            if (track.isEmpty()) {
                log.warn("Пропущена поездка: пустой маршрут между {} и {}", start, end);
                continue;
            }

            Instant startTime = dto.getStartDate().atZone(ZoneOffset.UTC).toInstant();
            Instant endTime = gpsTrackService.generateAndSaveTrack(track, startTime, vehicle);

            Trip trip = new Trip(vehicle, startTime, endTime);
            tripRepository.save(trip);
        }
    }

    public void importTripsByGuid(List<TripGuidExportDto> trips, Vehicle vehicle) {
        for (TripGuidExportDto dto : trips) {
            GpsPointCoord start = geocodingService.geocode(dto.getStartLocationInString());
            GpsPointCoord end = geocodingService.geocode(dto.getEndLocationInString());

            if (start == null || end == null) {
                log.warn("Пропущена поездка (GUID): не удалось геокодировать адреса: {} / {}", dto.getStartLocationInString(), dto.getEndLocationInString());
                continue;
            }

            List<GpsPointCoord> track = trackGenService.getRouting(start.getLng(), start.getLat(), end.getLng(), end.getLat());
            if (track.isEmpty()) {
                log.warn("Пропущена поездка (GUID): пустой маршрут между {} и {}", start, end);
                continue;
            }

            Instant startTime = dto.getStartTime().atZone(ZoneOffset.UTC).toInstant();
            Instant endTime = gpsTrackService.generateAndSaveTrack(track, startTime, vehicle);

            Trip trip = new Trip(vehicle, startTime, endTime);
            trip.setGuid(dto.getGuid());
            tripRepository.save(trip);
        }
    }
}
