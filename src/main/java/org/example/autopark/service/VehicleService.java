package org.example.autopark.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.DriverRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.specifications.VehicleSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final BrandsService brandService;
    private final EnterpriseService enterpriseService;
    private final DriverRepository driverRepository;

    @Autowired
    public VehicleService(VehicleRepository vehicleRepository, BrandsService brandService, EnterpriseService enterpriseService, DriverRepository driverRepository) {
        this.vehicleRepository = vehicleRepository;
        this.brandService = brandService;
        this.enterpriseService = enterpriseService;
        this.driverRepository = driverRepository;
    }


    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle findOne(Long id) {
        Optional<Vehicle> foundVehicle = vehicleRepository.findById(id);

        return foundVehicle.orElseThrow();
    }

    @Transactional
    public void save(Vehicle vehicle, Long brandId) {
        vehicle.setBrandOwner(brandService.findOne(brandId));
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public void save(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public void saveAll(List<Vehicle> vehicles) {
        vehicleRepository.saveAll(vehicles);
    }

    @Transactional
    public void update(Long id, Vehicle updatedVehicle, Long updatedBrandId) {
        updatedVehicle.setVehicleId(id);
        updatedVehicle.setBrandOwner(brandService.findOne(updatedBrandId));
        updatedVehicle.setEnterpriseOwnerOfVehicle(enterpriseService.findOne(updatedVehicle.getEnterpriseOwnerOfVehicle().getEnterpriseId()));
        vehicleRepository.save(updatedVehicle);
    }
    @Transactional
    public void update(Long id, Vehicle updatedVehicle, Long updatedBrandId, Long enterpriseId) {
        updatedVehicle.setVehicleId(id);
        updatedVehicle.setBrandOwner(brandService.findOne(updatedBrandId));
        updatedVehicle.setEnterpriseOwnerOfVehicle(enterpriseService.findOne(enterpriseId));
        vehicleRepository.save(updatedVehicle);
    }

    @Transactional
    public void update(Long id, Vehicle updatedVehicle) {
        updatedVehicle.setVehicleId(id);
        vehicleRepository.save(updatedVehicle);
    }

    @Transactional
    public void delete(Long vehicleId) {
        // 1. Проверяем, есть ли такой автомобиль
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with id " + vehicleId + " not found"));

        // 2. Находим всех водителей, у которых это `Vehicle` установлено как активное
        List<Driver> driversWithThisVehicle = driverRepository.findByActiveVehicle(vehicle);

        // 3. Обнуляем у этих водителей активное транспортное средство
        for (Driver driver : driversWithThisVehicle) {
            driver.setActiveVehicle(null);
            driverRepository.save(driver); // Сохраняем изменения
        }

        // 4. Удаляем автомобиль
        vehicleRepository.delete(vehicle);
        //vehicleRepository.deleteById(id);
    }

    public List<Vehicle> findVehiclesForManager(Long managerId) {

        List<Enterprise> enterprises = enterpriseService.findEnterprisesForManager(managerId);

        List<Vehicle> vehicles = new ArrayList<Vehicle>();

        for (Enterprise enterprise : enterprises) {
            vehicles.addAll(vehicleRepository.findVehiclesByEnterpriseOwnerOfVehicle_EnterpriseId(enterprise.getEnterpriseId()));
        }

        return vehicles;
    }

//    public Page<Vehicle> getPaginationOfVehicles(Long id, int page, int size, String sortField, String sortDirection) {
//        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
//        return vehicleRepository.findAll(pageable);
//    }
//
//    public Page<Vehicle> findVehiclesForManager(Long managerId, Pageable pageable) {
//        List<Enterprise> enterprises = enterpriseService.findEnterprisesForManager(managerId);
//
//        List<Long> enterpriseIds = enterprises.stream()
//                .map(Enterprise::getEnterpriseId)
//                .collect(Collectors.toList());
//
//        if (enterpriseIds.isEmpty()) {
//            return Page.empty(); // Если у менеджера нет предприятий, возвращаем пустую страницу
//        }
//
//        return vehicleRepository.findAllByEnterpriseOwnerOfVehicle_EnterpriseIdIn(enterpriseIds, pageable);
//    }

//    public Page<Vehicle> findVehiclesForManager(
//            Long managerId, Long enterpriseId, Long brandId, Integer minPrice, Integer maxPrice, Integer year,
//            String sortField, String sortDir, int page, int size) {
//
//        // Получаем все предприятия менеджера
//        List<Long> enterpriseIds = enterpriseService.findEnterprisesForManager(managerId)
//                .stream()
//                .map(Enterprise::getEnterpriseId)
//                .toList();
//
//        Specification<Vehicle> spec = Stream.of(
//                        (enterpriseId != null) ? VehicleSpecification.hasEnterprise(enterpriseId)
//                                : VehicleSpecification.hasAnyEnterprise(enterpriseIds), // ⬅ Фильтр для всех предприятий
//                        brandId == null ? null : VehicleSpecification.hasBrand(brandId),
//                        minPrice == null ? null : VehicleSpecification.hasMinPrice(minPrice),
//                        maxPrice == null ? null : VehicleSpecification.hasMaxPrice(maxPrice),
//                        year == null ? null : VehicleSpecification.hasYear(year)
//                )
//                .filter(Objects::nonNull)
//                .reduce(Specification::and)
//                .orElse(Specification.where(null));
//
//        // Настройка сортировки
//        Sort sort = Sort.by(Sort.Direction.fromString(sortDir == null ? "ASC" : sortDir),
//                sortField == null ? "vehicleName" : sortField);
//
//        Pageable pageable = PageRequest.of(page, size, sort);
//        return vehicleRepository.findAll(spec, pageable);
//    }

    public Page<Vehicle> findVehiclesForManager(
            Long managerId, Long enterpriseId, Long brandId, Integer minPrice, Integer maxPrice, Integer year,
            Pageable pageable) {  // <-- Теперь используем готовый Pageable

        // Получаем все предприятия менеджера
        List<Long> enterpriseIds = enterpriseService.findEnterprisesForManager(managerId)
                .stream()
                .map(Enterprise::getEnterpriseId)
                .toList();

        // Создаём спецификацию с динамическими фильтрами
        Specification<Vehicle> spec = Stream.of(
                        (enterpriseId != null) ? VehicleSpecification.hasEnterprise(enterpriseId)
                                : VehicleSpecification.hasAnyEnterprise(enterpriseIds),
                        brandId == null ? null : VehicleSpecification.hasBrand(brandId),
                        minPrice == null ? null : VehicleSpecification.hasMinPrice(minPrice),
                        maxPrice == null ? null : VehicleSpecification.hasMaxPrice(maxPrice),
                        year == null ? null : VehicleSpecification.hasYear(year)
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(Specification.where(null));

        return vehicleRepository.findAll(spec, pageable);
    }




    private void enrichVehicle(Vehicle vehicle) {
        vehicle.setEnterpriseOwnerOfVehicle(null);
    }
}
