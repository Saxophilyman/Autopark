package org.example.autopark.exportAndImport.byGuid;

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
import org.example.autopark.exportAndImport.byGuid.guidDto.TripGuidExportDto;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.example.autopark.exportAndImport.util.TripImportHelper;
import org.example.autopark.repository.BrandRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trip.Trip;
import org.example.autopark.trip.TripRepository;
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
public class ImportServiceByGuid {
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final TrackGenService trackGenService;
    private final BrandRepository brandRepository;
    private final GpsPointsRepository gpsPointsRepository;
    private final TripImportHelper tripImportHelper;

    @Transactional
    public void importFromDtoByGuid(VehicleExportDtoByGuid dto) {
        Enterprise enterprise = saveEnterprise(dto.getEnterprise());
        Vehicle vehicle = saveVehicle(dto.getVehicle(), enterprise);
        tripImportHelper.importTripsByGuid(dto.getTrips(), vehicle);
    }

    private Enterprise saveEnterprise(VehicleExportDtoByGuid.EnterpriseShortDTOByGuid dto) {
        return enterpriseRepository.findByGuid(dto.getGuid())
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setCityOfEnterprise(dto.getCity());
                    existing.setTimeZone(dto.getTimeZone());
                    return enterpriseRepository.save(existing); // обновляем
                })
                .orElseGet(() -> {
                    Enterprise newEnterprise = new Enterprise();
                    newEnterprise.setGuid(dto.getGuid());
                    newEnterprise.setName(dto.getName());
                    newEnterprise.setCityOfEnterprise(dto.getCity());
                    newEnterprise.setTimeZone(dto.getTimeZone());
                    return enterpriseRepository.save(newEnterprise); // создаём
                });
    }

    private Vehicle saveVehicle(VehicleExportDtoByGuid.VehicleShortDTOByGuid dto, Enterprise enterprise) {
        // Пытаемся найти существующий Vehicle по GUID
        Vehicle vehicle = vehicleRepository.findByGuid(dto.getGuid()).orElseGet(() -> {
            Vehicle v = new Vehicle();
            v.setGuid(dto.getGuid());
            return v;
        });

        // Обновляем поля
        vehicle.setVehicleName(dto.getName());
        vehicle.setLicensePlate(dto.getLicensePlate());
        vehicle.setVehicleCost(dto.getCost());
        vehicle.setVehicleYearOfRelease(dto.getYearOfRelease());
        vehicle.setEnterpriseOwnerOfVehicle(enterprise);
        //пока не трогаем
        //vehicle.setActiveDriver(driver);

        // Находим бренд по имени
        Brand brand = brandRepository.findByBrandName(dto.getBrand())
                .orElseThrow(() -> new IllegalArgumentException("Бренд с названием '" + dto.getBrand() + "' не найден"));

        vehicle.setBrandOwner(brand);

        return vehicleRepository.save(vehicle);
    }

//    private void saveTripsWithGps(List<TripGuidExportDto> trips, Vehicle vehicle) {
//        for (TripGuidExportDto tripDto : trips) {
//            // 1. Геокодирование
//            GpsPointCoord startCoord = geocodeAddress(tripDto.getStartLocationInString());
//            GpsPointCoord endCoord = geocodeAddress(tripDto.getEndLocationInString());
//
//            if (startCoord == null || endCoord == null) {
//                System.out.println("Не удалось геокодировать один из адресов: "
//                        + tripDto.getStartLocationInString() + " / " + tripDto.getEndLocationInString());
//                continue;
//            }
//
//            // 2. Получение маршрута
//            List<GpsPointCoord> track = trackGenService.getRouting(
//                    startCoord.getLng(), startCoord.getLat(),
//                    endCoord.getLng(), endCoord.getLat()
//            );
//
//            if (track.isEmpty()) {
//                System.out.println("Пустой маршрут — поездка не будет сохранена");
//                continue;
//            }
//
//            // 3. Установка времени
//            Instant startTime = tripDto.getStartTime().atZone(ZoneOffset.UTC).toInstant();
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
//                gpsPointsRepository.save(gpsPoint);
//
//                lastPointTime = currentTime;
//                currentTime = currentTime.plusSeconds(10);
//            }
//
//            // 4. Сохранение поездки
//            Trip trip = new Trip(vehicle, startTime, lastPointTime);
//            trip.setGuid(tripDto.getGuid()); // устанавливаем GUID из DTO
//            tripRepository.save(trip);
//        }
//    }
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
