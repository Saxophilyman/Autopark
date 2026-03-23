package org.example.autopark.exportAndImport.byGuid;

import lombok.RequiredArgsConstructor;
import org.example.autopark.gps.GpsPointsRepository;
import org.example.autopark.appUtil.trackGeneration.TrackGenService;
import org.example.autopark.entity.Brand;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.util.TripImportHelper;
import org.example.autopark.repository.BrandRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trip.TripRepository;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!reactive")
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
}
