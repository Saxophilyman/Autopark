package org.example.autopark.exportAndImport;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointDto;
import org.example.autopark.GPS.GpsPointsRepository;
import org.example.autopark.GPS.GpsPointsService;
import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;
import org.example.autopark.appUtil.trackGeneration.TrackGenService;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exception.VehicleNotFoundException;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trip.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportAndImportService {
    private final EnterpriseRepository enterpriseRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final TripService tripService;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final TrackGenService trackGenService;
    private final GpsPointsRepository gpsPointsRepository;

    public VehicleExportDto getVehicleExportData(Long vehicleId, LocalDate fromDate, LocalDate toDate){
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new RuntimeException("Vehicle not found"));

        Enterprise enterprise = vehicle.getEnterpriseOwnerOfVehicle();

        Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);


        List<Trip> tripsList = tripRepository.findTripsWithinRange(vehicleId, from, to);
        List<TripDTO> tripsDTOList = new ArrayList<>();

        for (Trip trip : tripsList) {
            List<GpsPointDto> gpsPoints = tripService.getTrackByTripId(trip.getId());
            if (gpsPoints.size() >= 2) {
                GpsPointDto startDto = gpsPoints.get(0);
                GpsPointDto endDto = gpsPoints.get(gpsPoints.size() - 1);

                GpsPoint start = new GpsPoint();
                start.setTimestamp(startDto.getTimestamp().atZone(ZoneOffset.UTC).toInstant());
                Point startPoint = geometryFactory.createPoint(new Coordinate(startDto.getLongitude(), startDto.getLatitude()));
                start.setLocation(startPoint);

                GpsPoint end = new GpsPoint();
                end.setTimestamp(endDto.getTimestamp().atZone(ZoneOffset.UTC).toInstant());
                Point endPoint = geometryFactory.createPoint(new Coordinate(endDto.getLongitude(), endDto.getLatitude()));
                end.setLocation(endPoint);

                TripDTO dto = tripMapper.toDTO(trip, enterprise.getTimeZone(), start, end);
                tripsDTOList.add(dto);
            }
        }

        return VehicleExportDto.fromEntities(vehicle, enterprise, tripsDTOList);
    }

    private Enterprise saveEnterprise(VehicleExportDto.EnterpriseShortDTO dto) {
        Enterprise enterprise = new Enterprise();
        enterprise.setEnterpriseId(dto.getId());
        enterprise.setName(dto.getName());
        enterprise.setCityOfEnterprise(dto.getCity());
        enterprise.setTimeZone(dto.getTimeZone());
        return enterpriseRepository.save(enterprise);
    }

    private Vehicle saveVehicle(VehicleExportDto.VehicleShortDTO dto, Enterprise enterprise) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(dto.getId());
        vehicle.setVehicleName(dto.getName());
        vehicle.setVehicleCost(dto.getCost());
        vehicle.setVehicleYearOfRelease(dto.getYearOfRelease());
        vehicle.setEnterpriseOwnerOfVehicle(enterprise);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void importFromDto(VehicleExportDto dto) {
        Enterprise enterprise = saveEnterprise(dto.getEnterprise());
        Vehicle vehicle = saveVehicle(dto.getVehicle(), enterprise);
        saveTripsWithGps(dto.getTrips(), vehicle);
    }

    private void saveTripsWithGps(List<TripDTO> trips, Vehicle vehicle) {
        for (TripDTO tripDTO : trips) {
            // 1. Геокодирование адресов в координаты
            GpsPointCoord startCoord = geocodeAddress(tripDTO.getStartLocationInString());
            GpsPointCoord endCoord = geocodeAddress(tripDTO.getEndLocationInString());

            if (startCoord == null || endCoord == null) {
                System.out.println("Не удалось геокодировать один из адресов");
                continue;
            }

            // 2. Получаем маршрут между двумя точками
            List<GpsPointCoord> track = trackGenService.getRouting(
                    startCoord.getLng(), startCoord.getLat(),
                    endCoord.getLng(), endCoord.getLat()
            );

            if (track.isEmpty()) {
                System.out.println("Пустой маршрут — трек не будет сохранён");
                continue;
            }

            // 3. Генерация и сохранение GPS точек
            Instant startTime = tripDTO.getStartDate().atZone(ZoneOffset.UTC).toInstant();
            Instant currentTime = startTime;

            for (GpsPointCoord coord : track) {
                Point location = geometryFactory.createPoint(new Coordinate(coord.getLng(), coord.getLat()));
                location.setSRID(4326);

                GpsPoint gpsPoint = new GpsPoint();
                gpsPoint.setVehicleIdForGps(vehicle);
                gpsPoint.setTimestamp(currentTime);
                gpsPoint.setLocation(location);

                gpsPointsRepository.save(gpsPoint);
                currentTime = currentTime.plusSeconds(10); // шаг 10 секунд
            }

            // 4. Сохранение поездки
            Trip trip = new Trip(vehicle, startTime, currentTime);
            tripRepository.save(trip);
        }
    }

    private GpsPointCoord geocodeAddress(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedAddress;

            HttpGet request = new HttpGet(url);
            request.addHeader("User-Agent", "AutoparkApp/1.0 (saxophilyman@gmail.com)");

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(request)) {

                String responseBody = EntityUtils.toString(response.getEntity());
                JSONArray jsonArray = new JSONArray(responseBody);

                if (jsonArray.length() > 0) {
                    JSONObject result = jsonArray.getJSONObject(0);
                    double lat = Double.parseDouble(result.getString("lat"));
                    double lon = Double.parseDouble(result.getString("lon"));
                    return new GpsPointCoord(lat, lon);
                } else {
                    System.out.println("Геокодер не нашёл координаты для: " + address);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // в случае ошибки
    }

    public void importFromCsv(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        VehicleExportDto dto = new VehicleExportDto();
        List<TripDTO> trips = new ArrayList<>();

        boolean readingTrips = false;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            if (line.startsWith("Enterprise ID")) {
                // Пропускаем заголовок
                line = reader.readLine();
                String[] parts = line.split(";");
                dto.setEnterprise(new VehicleExportDto.EnterpriseShortDTO(
                        Long.parseLong(parts[0]), parts[1], parts[2], parts[3]
                ));
                dto.setVehicle(new VehicleExportDto.VehicleShortDTO(
                        Long.parseLong(parts[4]), parts[5], Integer.parseInt(parts[6]),
                        Integer.parseInt(parts[7])
                ));
            } else if (line.startsWith("Trip Start")) {
                readingTrips = true;
            } else if (readingTrips) {
                String[] parts = line.split(";");
                TripDTO trip = new TripDTO();
                trip.setStartDate(LocalDateTime.parse(parts[0]));
                trip.setEndDate(LocalDateTime.parse(parts[1]));
                trip.setStartLocationInString(parts[2]);
                trip.setEndLocationInString(parts[3]);
                // продолжительность нам не нужна для импорта
                trips.add(trip);
            }
        }

        dto.setTrips(trips);
        importFromDto(dto);
    }


}
