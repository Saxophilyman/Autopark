package org.example.autopark.exportAndImport.byID;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointsRepository;
import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;
import org.example.autopark.appUtil.trackGeneration.TrackGenService;
import org.example.autopark.entity.Brand;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.example.autopark.exportAndImport.util.TripImportHelper;
import org.example.autopark.repository.BrandRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.service.VehicleService;
import org.example.autopark.trip.Trip;
import org.example.autopark.trip.TripDTO;
import org.example.autopark.trip.TripRepository;
import org.example.autopark.trip.TripService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportServiceById {
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final TrackGenService trackGenService;
    private final BrandRepository brandRepository;
    private final TripImportHelper tripImportHelper;
    private final GpsPointsRepository gpsPointsRepository;


    @Transactional
    public void importFromDtoById(VehicleExportDtoById dto) {
        Enterprise enterprise = saveEnterprise(dto.getEnterprise());
        Vehicle vehicle = saveVehicle(dto.getVehicle(), enterprise);
        tripImportHelper.importTripsByDto(dto.getTrips(), vehicle);
    }

    private Enterprise saveEnterprise(VehicleExportDtoById.EnterpriseShortDTO dto) {
        return enterpriseRepository.findById(dto.getId())
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setCityOfEnterprise(dto.getCity());
                    existing.setTimeZone(dto.getTimeZone());
                    return enterpriseRepository.save(existing); // обновляем
                })
                .orElseGet(() -> {
                    Enterprise newEnterprise = new Enterprise();
                    newEnterprise.setEnterpriseId(dto.getId());
                    newEnterprise.setName(dto.getName());
                    newEnterprise.setCityOfEnterprise(dto.getCity());
                    newEnterprise.setTimeZone(dto.getTimeZone());
                    return enterpriseRepository.save(newEnterprise); // создаём
                });
    }

    private Vehicle saveVehicle(VehicleExportDtoById.VehicleShortDTO dto, Enterprise enterprise) {
        Vehicle vehicle = new Vehicle();

        // Устанавливаем поля из DTO
        vehicle.setVehicleId(dto.getId());
        vehicle.setVehicleName(dto.getName());
        vehicle.setLicensePlate(dto.getLicensePlate());
        vehicle.setVehicleCost(dto.getCost());
        vehicle.setVehicleYearOfRelease(dto.getYearOfRelease());
        vehicle.setEnterpriseOwnerOfVehicle(enterprise);

        // Находим Brand по имени
        Brand brand = brandRepository.findByBrandName(dto.getBrand())
                .orElseThrow(() -> new IllegalArgumentException("Бренд с названием '" + dto.getBrand() + "' не найден"));

        vehicle.setBrandOwner(brand);

        return vehicleRepository.save(vehicle);
    }


//    private void saveTripsWithGps(List<TripDTO> trips, Vehicle vehicle) {
//        for (TripDTO tripDTO : trips) {
//            // 1. Геокодирование адресов в координаты
//            GpsPointCoord startCoord = geocodeAddress(tripDTO.getStartLocationInString());
//            GpsPointCoord endCoord = geocodeAddress(tripDTO.getEndLocationInString());
//
//            if (startCoord == null || endCoord == null) {
//                System.out.println("Не удалось геокодировать один из адресов");
//                continue;
//            }
//
//            // 2. Получаем маршрут между двумя точками
//            List<GpsPointCoord> track = trackGenService.getRouting(
//                    startCoord.getLng(), startCoord.getLat(),
//                    endCoord.getLng(), endCoord.getLat()
//            );
//
//            if (track.isEmpty()) {
//                System.out.println("Пустой маршрут — трек не будет сохранён");
//                continue;
//            }
//
//            // 3. Генерация и сохранение GPS точек
//            Instant startTime = tripDTO.getStartDate().atZone(ZoneOffset.UTC).toInstant();
//            Instant currentTime = startTime;
//            Instant lastPointTime = startTime;
//
//            for (GpsPointCoord coord : track) {
//                Point location = geometryFactory.createPoint(new Coordinate(coord.getLng(), coord.getLat()));
//                location.setSRID(4326);
//
//                GpsPoint gpsPoint = new GpsPoint();
//                gpsPoint.setVehicleIdForGps(vehicle);
//                gpsPoint.setTimestamp(currentTime);
//                gpsPoint.setLocation(location);
//
//                gpsPointsRepository.save(gpsPoint);
//
//                lastPointTime = currentTime;              // ← сохраняем последнее время перед сдвигом
//                currentTime = currentTime.plusSeconds(10);
//            }
//
//            Trip trip = new Trip(vehicle, startTime, lastPointTime);
//            tripRepository.save(trip);
//        }
//    }
//
//    private GpsPointCoord geocodeAddress(String address) {
//        try {
//            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
//            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedAddress;
//
//            HttpGet request = new HttpGet(url);
//            request.addHeader("User-Agent", "AutoparkApp/1.0 (saxophilyman@gmail.com)");
//
//            try (CloseableHttpClient client = HttpClients.createDefault();
//                 CloseableHttpResponse response = client.execute(request)) {
//
//                String responseBody = EntityUtils.toString(response.getEntity());
//                JSONArray jsonArray = new JSONArray(responseBody);
//
//                if (jsonArray.length() > 0) {
//                    JSONObject result = jsonArray.getJSONObject(0);
//                    double lat = Double.parseDouble(result.getString("lat"));
//                    double lon = Double.parseDouble(result.getString("lon"));
//                    return new GpsPointCoord(lat, lon);
//                } else {
//                    System.out.println("Геокодер не нашёл координаты для: " + address);
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null; // в случае ошибки
//    }
}
