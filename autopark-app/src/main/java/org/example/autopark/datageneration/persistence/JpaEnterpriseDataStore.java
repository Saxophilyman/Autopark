package org.example.autopark.datageneration.persistence;

import org.example.autopark.datageneration.contract.EnterpriseDataStore;
import org.example.autopark.datageneration.model.DriverDraft;
import org.example.autopark.datageneration.model.DriverVehicleAssignment;
import org.example.autopark.datageneration.model.GeneratedEnterpriseData;
import org.example.autopark.datageneration.model.VehicleDraft;
import org.example.autopark.entity.Brand;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exception.ResourceNotFoundException;
import org.example.autopark.repository.BrandRepository;
import org.example.autopark.repository.DriverRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("!reactive")
public class JpaEnterpriseDataStore implements EnterpriseDataStore {

    private final EnterpriseRepository enterpriseRepository;
    private final BrandRepository brandRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public JpaEnterpriseDataStore(
            EnterpriseRepository enterpriseRepository,
            BrandRepository brandRepository,
            VehicleRepository vehicleRepository,
            DriverRepository driverRepository
    ) {
        this.enterpriseRepository = enterpriseRepository;
        this.brandRepository = brandRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    @Transactional
    public void save(GeneratedEnterpriseData data) {
        Enterprise enterprise = enterpriseRepository.findById(data.enterpriseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Предприятие с id " + data.enterpriseId() + " не найдено"
                ));

        Map<Long, Brand> brands = loadBrands(data.vehicles());
        List<Vehicle> vehicles = createVehicles(data.vehicles(), enterprise, brands);
        List<Vehicle> savedVehicles = vehicleRepository.saveAll(vehicles);

        List<Driver> drivers = createDrivers(data.drivers(), enterprise);
        applyAssignments(data.assignments(), savedVehicles, drivers);
        driverRepository.saveAll(drivers);
    }

    private Map<Long, Brand> loadBrands(List<VehicleDraft> drafts) {
        Map<Long, Brand> brands = new HashMap<>();
        for (VehicleDraft draft : drafts) {
            brands.computeIfAbsent(draft.brandId(), brandId ->
                    brandRepository.findById(brandId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Бренд с id " + brandId + " не найден"
                            ))
            );
        }
        return brands;
    }

    private List<Vehicle> createVehicles(
            List<VehicleDraft> drafts,
            Enterprise enterprise,
            Map<Long, Brand> brands
    ) {
        List<Vehicle> vehicles = new ArrayList<>(drafts.size());
        for (VehicleDraft draft : drafts) {
            Vehicle vehicle = new Vehicle();
            vehicle.setEnterpriseOwnerOfVehicle(enterprise);
            vehicle.setBrandOwner(brands.get(draft.brandId()));
            vehicle.setVehicleName(draft.name());
            vehicle.setLicensePlate(draft.licensePlate());
            vehicle.setVehicleCost(draft.cost());
            vehicle.setVehicleYearOfRelease(draft.yearOfRelease());
            vehicles.add(vehicle);
        }
        return vehicles;
    }

    private List<Driver> createDrivers(List<DriverDraft> drafts, Enterprise enterprise) {
        List<Driver> drivers = new ArrayList<>(drafts.size());
        for (DriverDraft draft : drafts) {
            Driver driver = new Driver();
            driver.setEnterpriseOwnerOfDriver(enterprise);
            driver.setDriverName(draft.name());
            driver.setDriverSalary(draft.salary());
            driver.setActive(false);
            drivers.add(driver);
        }
        return drivers;
    }

    private void applyAssignments(
            List<DriverVehicleAssignment> assignments,
            List<Vehicle> vehicles,
            List<Driver> drivers
    ) {
        for (DriverVehicleAssignment assignment : assignments) {
            Driver driver = drivers.get(assignment.driverIndex());
            Vehicle vehicle = vehicles.get(assignment.vehicleIndex());

            driver.setActive(true);
            driver.setActiveVehicle(vehicle);
            vehicle.setActiveDriver(driver);
        }
    }
}
