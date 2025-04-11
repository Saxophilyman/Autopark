package org.example.autopark.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.autopark.dto.VehicleApiDto;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.dto.mapper.VehicleMapper;
import org.example.autopark.dto.mapper.VehiclePageDTO;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exception.VehicleNotCreatedException;
import org.example.autopark.repository.DriverRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.specifications.VehicleSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final BrandsService brandService;
    private final EnterpriseService enterpriseService;
    private final DriverRepository driverRepository;
    private final VehicleMapper vehicleMapper;

    @Autowired
    public VehicleService(VehicleRepository vehicleRepository, BrandsService brandService, EnterpriseService enterpriseService, DriverRepository driverRepository, VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.brandService = brandService;
        this.enterpriseService = enterpriseService;
        this.driverRepository = driverRepository;
        this.vehicleMapper = vehicleMapper;
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
        if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new VehicleNotCreatedException("Номер уже используется: " + vehicle.getLicensePlate());
        }
        vehicle.setBrandOwner(brandService.findOne(brandId));
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public void save(Vehicle vehicle) {
        if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new VehicleNotCreatedException("Номер уже используется: " + vehicle.getLicensePlate());
        }
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
        if (vehicleRepository.existsByLicensePlateAndVehicleIdNot(updatedVehicle.getLicensePlate(), id)) {
            throw new VehicleNotCreatedException("Номер уже используется: " + updatedVehicle.getLicensePlate());
        }
        updatedVehicle.setVehicleId(id);
        updatedVehicle.setBrandOwner(brandService.findOne(updatedBrandId));
        updatedVehicle.setEnterpriseOwnerOfVehicle(enterpriseService.findOne(enterpriseId));
        vehicleRepository.save(updatedVehicle);
    }

    @Transactional
    public void update(Long id, Vehicle updatedVehicle) {
        if (vehicleRepository.existsByLicensePlateAndVehicleIdNot(updatedVehicle.getLicensePlate(), id)) {
            throw new VehicleNotCreatedException("Номер уже используется: " + updatedVehicle.getLicensePlate());
        }
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

    /**
     * Получение списка машин с учётом таймзоны предприятия и пагинации.
     */
    public VehiclePageDTO getVehiclesForEnterprise(
            Long managerId, Long enterpriseId, Long brandId, Integer minPrice, Integer maxPrice, Integer year,
            String sortField, String sortDir, int page, int size) {

        // Проверяем, имеет ли менеджер доступ к предприятию
        if (!enterpriseService.managerHasEnterprise(managerId, enterpriseId)) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        // Получаем предприятие и его таймзону
        Enterprise enterprise = enterpriseService.findOne(enterpriseId);
        String enterpriseTimezone = enterprise.getTimeZone();

        // Создаём объект пагинации и сортировки
        Pageable pageRequest = createPageRequest(sortField, sortDir, page, size);

        // Запрашиваем список машин с пагинацией
        Page<Vehicle> vehiclesPage = findVehiclesForManager(
                managerId, enterpriseId, brandId, minPrice, maxPrice, year, pageRequest);

        // Конвертируем машины в DTO с учётом таймзоны предприятия
        List<VehicleApiDto> VehicleApiDtoList = vehiclesPage.getContent()
                .stream()
                .map(vehicle -> vehicleMapper.convertToVehicleApiDto(vehicle, enterpriseTimezone))
                .collect(Collectors.toList());

        return new VehiclePageDTO(
                VehicleApiDtoList,
                vehiclesPage.getNumber() + 1, // Thymeleaf использует индексацию с 1, а Spring с 0
                vehiclesPage.getTotalPages(),
                vehiclesPage.hasNext(),
                vehiclesPage.hasPrevious(),
                enterpriseTimezone
        );
    }

    /**
     * Создаёт объект `PageRequest` для сортировки и пагинации.
     */
    private Pageable createPageRequest(String sortField, String sortDir, int page, int size) {
        String[] sortFields = sortField.split(",");
        return PageRequest.of(
                page, size, Sort.by(
                        Arrays.stream(sortFields)
                                .map(field -> Sort.Order.by(field).with(Sort.Direction.fromString(sortDir)))
                                .toList()
                )
        );
    }

    public Vehicle findByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate)
                .orElse(null);
    }
    public List<Vehicle> findByLicensePlateContaining(String query) {
        return vehicleRepository.findByLicensePlateContainingIgnoreCase(query);
    }


}
