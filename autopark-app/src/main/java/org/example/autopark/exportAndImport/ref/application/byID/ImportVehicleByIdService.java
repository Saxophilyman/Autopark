package org.example.autopark.exportAndImport.ref.application.byID;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Brand;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exportAndImport.ref.document.byID.VehicleExportDtoById;
import org.example.autopark.exportAndImport.ref.format.exception.InvalidExchangeDocumentException;
import org.example.autopark.repository.BrandRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.exportAndImport.ref.util.TripImportHelper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class ImportVehicleByIdService {
    private final VehicleRepository vehicleRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final BrandRepository brandRepository;
    private final EnterpriseService enterpriseService;
    private final TripImportHelper tripImportHelper;

    @Transactional
    public void execute(ImportVehicleByIdCommand command) {
        importDocument(command.document(), command.managerId());
    }

    @Transactional
    public void executeSystem(VehicleExportDtoById document) {
        importDocument(document, null);
    }

    private void importDocument(VehicleExportDtoById document, Long managerId) {
        validateDocument(document);
        Enterprise enterprise = saveEnterprise(document.getEnterprise(), managerId);
        Vehicle vehicle = saveVehicle(document.getVehicle(), enterprise);
        tripImportHelper.importTripsByDto(document.getTrips(), vehicle);
    }

    private Enterprise saveEnterprise(VehicleExportDtoById.EnterpriseShortDTO source, Long managerId) {
        return enterpriseRepository.findById(source.getId())
                .map(existing -> updateExistingEnterprise(existing, source, managerId))
                .orElseGet(() -> createEnterprise(source, managerId));
    }

    private Enterprise updateExistingEnterprise(Enterprise enterprise, VehicleExportDtoById.EnterpriseShortDTO source, Long managerId) {
        if (managerId != null) {
            enterpriseService.findEnterpriseForManager(managerId, enterprise.getEnterpriseId());
        }
        enterprise.setName(source.getName());
        enterprise.setCityOfEnterprise(source.getCity());
        enterprise.setTimeZone(source.getTimeZone());
        return enterpriseRepository.save(enterprise);
    }

    private Enterprise createEnterprise(VehicleExportDtoById.EnterpriseShortDTO source, Long managerId) {
        Enterprise enterprise = new Enterprise();
        enterprise.setEnterpriseId(source.getId());
        enterprise.setName(source.getName());
        enterprise.setCityOfEnterprise(source.getCity());
        enterprise.setTimeZone(source.getTimeZone());

        if (managerId == null) {
            return enterpriseRepository.save(enterprise);
        }

        enterpriseService.save(enterprise, managerId);
        return enterprise;
    }

    private Vehicle saveVehicle(VehicleExportDtoById.VehicleShortDTO source, Enterprise enterprise) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(source.getId());
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

    private void validateDocument(VehicleExportDtoById document) {
        if (document == null || document.getEnterprise() == null || document.getVehicle() == null) {
            throw new InvalidExchangeDocumentException("Документ по ID должен содержать предприятие и автомобиль");
        }
        if (document.getTrips() == null) {
            document.setTrips(java.util.List.of());
        }
    }
}
