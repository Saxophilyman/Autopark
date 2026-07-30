package org.example.autopark.exportAndImport.ref.application.byGuid;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Brand;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exportAndImport.ref.document.byGuid.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.ref.format.exception.InvalidExchangeDocumentException;
import org.example.autopark.exportAndImport.ref.util.TripImportHelper;
import org.example.autopark.repository.BrandRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.service.EnterpriseService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class ImportVehicleByGuidService {
    private final VehicleRepository vehicleRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final BrandRepository brandRepository;
    private final EnterpriseService enterpriseService;
    private final TripImportHelper tripImportHelper;

    @Transactional
    public void execute(ImportVehicleByGuidCommand command) {
        importDocument(command.document(), command.managerId());
    }

    @Transactional
    public void executeSystem(VehicleExportDtoByGuid document) {
        importDocument(document, null);
    }

    private void importDocument(VehicleExportDtoByGuid document, Long managerId) {
        validateDocument(document);
        Enterprise enterprise = saveEnterprise(document.getEnterprise(), managerId);
        Vehicle vehicle = saveVehicle(document.getVehicle(), enterprise);
        tripImportHelper.importTripsByGuid(document.getTrips(), vehicle);
    }

    private Enterprise saveEnterprise(VehicleExportDtoByGuid.EnterpriseShortDTOByGuid source, Long managerId) {
        return enterpriseRepository.findByGuid(source.getGuid())
                .map(existing -> updateExistingEnterprise(existing, source, managerId))
                .orElseGet(() -> createEnterprise(source, managerId));
    }

    private Enterprise updateExistingEnterprise(Enterprise enterprise, VehicleExportDtoByGuid.EnterpriseShortDTOByGuid source, Long managerId) {
        if (managerId != null) {
            enterpriseService.findEnterpriseForManager(managerId, enterprise.getEnterpriseId());
        }
        enterprise.setName(source.getName());
        enterprise.setCityOfEnterprise(source.getCity());
        enterprise.setTimeZone(source.getTimeZone());
        return enterpriseRepository.save(enterprise);
    }

    private Enterprise createEnterprise(VehicleExportDtoByGuid.EnterpriseShortDTOByGuid source, Long managerId) {
        Enterprise enterprise = new Enterprise();
        enterprise.setGuid(source.getGuid());
        enterprise.setName(source.getName());
        enterprise.setCityOfEnterprise(source.getCity());
        enterprise.setTimeZone(source.getTimeZone());

        if (managerId == null) {
            return enterpriseRepository.save(enterprise);
        }

        enterpriseService.save(enterprise, managerId);
        return enterprise;
    }

    private Vehicle saveVehicle(VehicleExportDtoByGuid.VehicleShortDTOByGuid source, Enterprise enterprise) {
        Vehicle vehicle = vehicleRepository.findByGuid(source.getGuid()).orElseGet(() -> {
            Vehicle created = new Vehicle();
            created.setGuid(source.getGuid());
            return created;
        });

        vehicle.setVehicleName(source.getName());
        vehicle.setLicensePlate(source.getLicensePlate());
        vehicle.setVehicleCost(source.getCost());
        vehicle.setVehicleYearOfRelease(source.getYearOfRelease());
        vehicle.setEnterpriseOwnerOfVehicle(enterprise);

        Brand brand = brandRepository.findByBrandName(source.getBrand())
                .orElseThrow(() -> new InvalidExchangeDocumentException("Бренд с названием '" + source.getBrand() + "' не найден"));
        vehicle.setBrandOwner(brand);
        return vehicleRepository.save(vehicle);
    }

    private void validateDocument(VehicleExportDtoByGuid document) {
        if (document == null || document.getEnterprise() == null || document.getVehicle() == null) {
            throw new InvalidExchangeDocumentException("Документ по GUID должен содержать предприятие и автомобиль");
        }
        if (document.getTrips() == null) {
            document.setTrips(java.util.List.of());
        }
    }
}
