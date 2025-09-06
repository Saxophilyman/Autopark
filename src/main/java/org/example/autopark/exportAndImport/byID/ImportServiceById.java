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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Profile("!reactive")
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

}
