package org.example.autopark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.dto.VehicleApiDto;
import org.example.autopark.dto.mapper.VehicleMapper;
import org.example.autopark.dto.mapper.VehiclePageDTO;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exception.VehicleNotCreatedException;
import org.example.autopark.exception.VehicleNotFoundException;
import org.example.autopark.repository.DriverRepository;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.specifications.VehicleSpecification;
import org.example.autopark.kafka.VehicleDomainEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
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

import static org.example.autopark.security.SecurityUtil.getAuthenticatedManagerIdOrNull;

@Service
@Slf4j
@Profile("!reactive")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BrandsService brandService;
    private final EnterpriseService enterpriseService;
    private final DriverRepository driverRepository;
    private final VehicleMapper vehicleMapper;
    private final ApplicationEventPublisher events;

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    @Cacheable(value = "vehicles", key = "#id")
    public Vehicle findOne(Long id) {
        log.info("Поиск автомобиля по id: {}", id);
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    // --------- CREATE ---------

    @Transactional
    public void save(Vehicle vehicle, Long brandId) {
        validateLicensePlateForCreate(vehicle.getLicensePlate());
        vehicle.setBrandOwner(brandService.findOne(brandId));
        saveAndPublish(vehicle, VehicleDomainEvent.Action.CREATED);
    }

    @Transactional
    public void save(Vehicle vehicle) {
        validateLicensePlateForCreate(vehicle.getLicensePlate());
        saveAndPublish(vehicle, VehicleDomainEvent.Action.CREATED);
    }

    @Transactional
    public void saveAll(List<Vehicle> vehicles) {
        List<Vehicle> savedVehicles = vehicleRepository.saveAll(vehicles);
        for (Vehicle vehicle : savedVehicles) {
            publishDomainEvent(vehicle, VehicleDomainEvent.Action.CREATED, getAuthenticatedManagerIdOrNull());
        }
    }

    // --------- UPDATE ---------

    /**
     * Обновление машины без смены бренда/предприятия.
     * Используется в e2e-тесте через /api/managers/{id}/vehicles/{idVehicle}.
     */
    @Transactional
    public void update(Long id, Vehicle updatedVehicle) {
        Vehicle existing = findVehicleOrThrow(id);

        validateLicensePlateForUpdate(id, existing, updatedVehicle);
        applyEditableFields(existing, updatedVehicle);

        saveAndPublish(existing, VehicleDomainEvent.Action.UPDATED);
    }

    @Transactional
    public void update(Long id, Vehicle updatedVehicle, Long updatedBrandId) {
        Vehicle existing = findVehicleOrThrow(id);

        validateLicensePlateForUpdate(id, existing, updatedVehicle);
        applyEditableFields(existing, updatedVehicle);
        existing.setBrandOwner(brandService.findOne(updatedBrandId));

        saveAndPublish(existing, VehicleDomainEvent.Action.UPDATED);
    }

    @Transactional
    public void update(Long id, Vehicle updatedVehicle, Long updatedBrandId, Long enterpriseId) {
        Vehicle existing = findVehicleOrThrow(id);

        validateLicensePlateForUpdate(id, existing, updatedVehicle);
        applyEditableFields(existing, updatedVehicle);
        existing.setBrandOwner(brandService.findOne(updatedBrandId));
        existing.setEnterpriseOwnerOfVehicle(enterpriseService.findOne(enterpriseId));

        saveAndPublish(existing, VehicleDomainEvent.Action.UPDATED);
    }


    // --------- DELETE ---------

    @Transactional
    public void delete(Long vehicleId) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);

        clearDriversFromVehicle(vehicle);
        vehicleRepository.delete(vehicle);

        // событие удаление — публикуем по данным *удалённой* сущности
        publishDomainEvent(vehicle, VehicleDomainEvent.Action.DELETED, getAuthenticatedManagerIdOrNull());
    }

    //---------- вспомогательные методы для crud ---------

    private Vehicle findVehicleOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    private void validateLicensePlateForCreate(String licensePlate) {
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new VehicleNotCreatedException("Номер уже используется: " + licensePlate);
        }
    }

    private void validateLicensePlateForUpdate(Long id, Vehicle existing, Vehicle updatedVehicle) {
        String newPlate = updatedVehicle.getLicensePlate();
        if (newPlate != null
                && !newPlate.equals(existing.getLicensePlate())
                && vehicleRepository.existsByLicensePlateAndVehicleIdNot(newPlate, id)) {
            throw new VehicleNotCreatedException("Номер уже используется: " + newPlate);
        }
    }

    private void applyEditableFields(Vehicle existing, Vehicle updatedVehicle) {
        if (updatedVehicle.getVehicleName() != null) {
            existing.setVehicleName(updatedVehicle.getVehicleName());
        }

        if (updatedVehicle.getLicensePlate() != null) {
            existing.setLicensePlate(updatedVehicle.getLicensePlate());
        }

        existing.setVehicleCost(updatedVehicle.getVehicleCost());
        existing.setVehicleYearOfRelease(updatedVehicle.getVehicleYearOfRelease());

        if (updatedVehicle.getPurchaseDateUtc() != null) {
            existing.setPurchaseDateUtc(updatedVehicle.getPurchaseDateUtc());
        }
    }

    private void clearDriversFromVehicle(Vehicle vehicle) {
        List<Driver> driversWithThisVehicle = driverRepository.findByActiveVehicle(vehicle);
        for (Driver driver : driversWithThisVehicle) {
            driver.setActiveVehicle(null);
            driverRepository.save(driver);
        }
    }

    private void saveAndPublish(Vehicle vehicle, VehicleDomainEvent.Action action) {
        Vehicle saved = vehicleRepository.save(vehicle);
        publishDomainEvent(saved, action, getAuthenticatedManagerIdOrNull());
    }

    /**
     * Публикует доменное событие; обработчик отправит его в Kafka ТОЛЬКО после коммита.
     * Сейчас managerId и enterpriseGuid передаются как null (TODO: заполнить, когда появятся).
     */
    private void publishDomainEvent(Vehicle v,
                                    VehicleDomainEvent.Action action,
                                    Long managerId) {               // <-- Long, не UUID
        UUID vehicleGuid = v.getGuid();
        UUID enterpriseGuid = null; // если появится guid у Enterprise — подставишь здесь

        events.publishEvent(new VehicleDomainEvent(
                vehicleGuid, enterpriseGuid, managerId, action
        ));
    }

    // --------- прочие методы без изменений ---------

    public List<Vehicle> findVehiclesForManager(Long managerId) {
        List<Enterprise> enterprises = enterpriseService.findEnterprisesForManager(managerId);
        List<Vehicle> vehicles = new ArrayList<>();
        for (Enterprise enterprise : enterprises) {
            vehicles.addAll(vehicleRepository
                    .findVehiclesByEnterpriseOwnerOfVehicle_EnterpriseId(enterprise.getEnterpriseId()));
        }
        return vehicles;
    }

    public Page<Vehicle> findVehiclesForManager(
            Long managerId, Long enterpriseId, Long brandId, Integer minPrice, Integer maxPrice, Integer year,
            Pageable pageable) {

        List<Long> enterpriseIds = enterpriseService.findEnterprisesForManager(managerId)
                .stream().map(Enterprise::getEnterpriseId).toList();

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

    public VehiclePageDTO getVehiclesForEnterprise(
            Long managerId, Long enterpriseId, Long brandId, Integer minPrice, Integer maxPrice, Integer year,
            String sortField, String sortDir, int page, int size) {

        if (!enterpriseService.managerHasEnterprise(managerId, enterpriseId)) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        Enterprise enterprise = enterpriseService.findOne(enterpriseId);
        String enterpriseTimezone = enterprise.getTimeZone();

        Pageable pageRequest = createPageRequest(sortField, sortDir, page, size);

        Page<Vehicle> vehiclesPage = findVehiclesForManager(
                managerId, enterpriseId, brandId, minPrice, maxPrice, year, pageRequest);

        List<VehicleApiDto> VehicleApiDtoList = vehiclesPage.getContent()
                .stream()
                .map(vehicle -> vehicleMapper.convertToVehicleApiDto(vehicle, enterpriseTimezone))
                .collect(Collectors.toList());

        return new VehiclePageDTO(
                VehicleApiDtoList,
                vehiclesPage.getNumber() + 1,
                vehiclesPage.getTotalPages(),
                vehiclesPage.hasNext(),
                vehiclesPage.hasPrevious(),
                enterpriseTimezone
        );
    }

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

    @Cacheable(value = "vehicles", key = "#licensePlate")
    public Vehicle findByLicensePlate(String licensePlate) {
        log.info("Поиск автомобиля по номеру: {}", licensePlate);
        return vehicleRepository.findByLicensePlate(licensePlate).orElse(null);
    }

    @Cacheable(value = "vehicles", key = "#query")
    public List<Vehicle> findByLicensePlateContaining(String query) {
        log.info("Поиск автомобиля по номеру через запрос: {}", query);
        return vehicleRepository.findByLicensePlateContainingIgnoreCase(query);
    }
}


