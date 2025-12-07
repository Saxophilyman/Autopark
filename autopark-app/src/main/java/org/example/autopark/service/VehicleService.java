package org.example.autopark.service;

import jakarta.persistence.EntityNotFoundException;
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
import org.example.autopark.kafka.VehicleDomainEvent;           // ⬅ добавлено
import org.geolatte.geom.V;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;      // ⬅ добавлено
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
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BrandsService brandService;
    private final EnterpriseService enterpriseService;
    private final DriverRepository driverRepository;
    private final VehicleMapper vehicleMapper;

    private final ApplicationEventPublisher events;            // ⬅ добавлено

    @Autowired
    public VehicleService(VehicleRepository vehicleRepository,
                          BrandsService brandService,
                          EnterpriseService enterpriseService,
                          DriverRepository driverRepository,
                          VehicleMapper vehicleMapper,
                          ApplicationEventPublisher events) {  // ⬅ добавлено
        this.vehicleRepository = vehicleRepository;
        this.brandService = brandService;
        this.enterpriseService = enterpriseService;
        this.driverRepository = driverRepository;
        this.vehicleMapper = vehicleMapper;
        this.events = events;                                   // ⬅ добавлено
    }

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    @Cacheable(value = "vehicles", key = "#id")
    public Vehicle findOne(Long id) {
        log.info("Поиск автомобиля по id: {}", id);
        Optional<Vehicle> foundVehicle = vehicleRepository.findById(id);
        return foundVehicle.orElseThrow(()->new VehicleNotFoundException(id));
    }

    // --------- CREATE ---------

    @Transactional
    public void save(Vehicle vehicle, Long brandId) {
        if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new VehicleNotCreatedException("Номер уже используется: " + vehicle.getLicensePlate());
        }
        vehicle.setBrandOwner(brandService.findOne(brandId));
        Vehicle saved = vehicleRepository.save(vehicle);

        // публикация доменного события после коммита
        publishDomainEvent(saved, VehicleDomainEvent.Action.CREATED, getAuthenticatedManagerIdOrNull());
    }

    @Transactional
    public void save(Vehicle vehicle) {
        if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new VehicleNotCreatedException("Номер уже используется: " + vehicle.getLicensePlate());
        }
        Vehicle saved = vehicleRepository.save(vehicle);
        publishDomainEvent(saved, VehicleDomainEvent.Action.CREATED, getAuthenticatedManagerIdOrNull());
    }

    @Transactional
    public void saveAll(List<Vehicle> vehicles) {
        List<Vehicle> saved = vehicleRepository.saveAll(vehicles);
        // при массовом сохранении отправим события по каждому авто
        for (Vehicle v : saved) {
            publishDomainEvent(v, VehicleDomainEvent.Action.CREATED, getAuthenticatedManagerIdOrNull());
        }
    }

    // --------- UPDATE ---------

    @Transactional
    public void update(Long id, Vehicle updatedVehicle, Long updatedBrandId) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        // проверка уникальности номера
        String newPlate = updatedVehicle.getLicensePlate();
        if (newPlate != null
                && !newPlate.equals(existing.getLicensePlate())
                && vehicleRepository.existsByLicensePlateAndVehicleIdNot(newPlate, id)) {
            throw new VehicleNotCreatedException("Номер уже используется: " + newPlate);
        }

        // обновляем только редактируемые поля
        if (updatedVehicle.getVehicleName() != null) {
            existing.setVehicleName(updatedVehicle.getVehicleName());
        }
        if (newPlate != null) {
            existing.setLicensePlate(newPlate);
        }
        existing.setVehicleCost(updatedVehicle.getVehicleCost());
        existing.setVehicleYearOfRelease(updatedVehicle.getVehicleYearOfRelease());

        // НЕ затираем дату, если из формы пришёл null
        if (updatedVehicle.getPurchaseDateUtc() != null) {
            existing.setPurchaseDateUtc(updatedVehicle.getPurchaseDateUtc());
        }

        // бренд реально меняется — его всегда берём из параметра
        existing.setBrandOwner(brandService.findOne(updatedBrandId));

        Vehicle saved = vehicleRepository.save(existing);
        publishDomainEvent(saved, VehicleDomainEvent.Action.UPDATED, getAuthenticatedManagerIdOrNull());
    }

    @Transactional
    public void update(Long id, Vehicle updatedVehicle, Long updatedBrandId, Long enterpriseId) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        String newPlate = updatedVehicle.getLicensePlate();
        if (newPlate != null
                && !newPlate.equals(existing.getLicensePlate())
                && vehicleRepository.existsByLicensePlateAndVehicleIdNot(newPlate, id)) {
            throw new VehicleNotCreatedException("Номер уже используется: " + newPlate);
        }

        if (updatedVehicle.getVehicleName() != null) {
            existing.setVehicleName(updatedVehicle.getVehicleName());
        }
        if (newPlate != null) {
            existing.setLicensePlate(newPlate);
        }
        existing.setVehicleCost(updatedVehicle.getVehicleCost());
        existing.setVehicleYearOfRelease(updatedVehicle.getVehicleYearOfRelease());

        if (updatedVehicle.getPurchaseDateUtc() != null) {
            existing.setPurchaseDateUtc(updatedVehicle.getPurchaseDateUtc());
        }

        existing.setBrandOwner(brandService.findOne(updatedBrandId));
        existing.setEnterpriseOwnerOfVehicle(enterpriseService.findOne(enterpriseId));

        Vehicle saved = vehicleRepository.save(existing);
        publishDomainEvent(saved, VehicleDomainEvent.Action.UPDATED, getAuthenticatedManagerIdOrNull());
    }

//    @Transactional
//    public void update(Long id, Vehicle updatedVehicle) {
//        if (vehicleRepository.existsByLicensePlateAndVehicleIdNot(updatedVehicle.getLicensePlate(), id)) {
//            throw new VehicleNotCreatedException("Номер уже используется: " + updatedVehicle.getLicensePlate());
//        }
//        updatedVehicle.setVehicleId(id);
//        Vehicle saved = vehicleRepository.save(updatedVehicle);
//        publishDomainEvent(saved, VehicleDomainEvent.Action.UPDATED, getAuthenticatedManagerIdOrNull());
//    }

    /**
     * Обновление машины без смены бренда/предприятия.
     * Используется в e2e-тесте через /api/managers/{id}/vehicles/{idVehicle}.
     */
    @Transactional
    public void update(Long id, Vehicle updatedVehicle) {
        // 1. Забираем текущую машину из БД
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        // 2. Проверяем уникальность номера, если он меняется
        String newPlate = updatedVehicle.getLicensePlate();
        if (newPlate != null
                && !newPlate.equals(existing.getLicensePlate())
                && vehicleRepository.existsByLicensePlateAndVehicleIdNot(newPlate, id)) {
            throw new VehicleNotCreatedException("Номер уже используется: " + newPlate);
        }

        // 3. Копируем редактируемые поля из updatedVehicle в existing

        // имя — если пришло (на всякий случай через проверку)
        if (updatedVehicle.getVehicleName() != null) {
            existing.setVehicleName(updatedVehicle.getVehicleName());
        }

        // номер — если пришёл
        if (newPlate != null) {
            existing.setLicensePlate(newPlate);
        }

        // стоимость и год — примитивы (int), их можно просто перезаписать
        existing.setVehicleCost(updatedVehicle.getVehicleCost());
        existing.setVehicleYearOfRelease(updatedVehicle.getVehicleYearOfRelease());

        // purchaseDateUtc в БД NOT NULL — не затираем его null'ом.
        // Если DTO пришлёт новое значение — обновим.
        if (updatedVehicle.getPurchaseDateUtc() != null) {
            existing.setPurchaseDateUtc(updatedVehicle.getPurchaseDateUtc());
        }

        // brandOwner, enterpriseOwnerOfVehicle, guid и прочие важные поля
        // специально не трогаем — остаются как в БД.

        // 4. Сохраняем
        Vehicle saved = vehicleRepository.save(existing);

        // 5. Публикуем доменное событие
        publishDomainEvent(saved, VehicleDomainEvent.Action.UPDATED, getAuthenticatedManagerIdOrNull());
    }


    // --------- DELETE ---------

    @Transactional
    public void delete(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with id " + vehicleId + " not found"));

        // "снять" активную машину у водителей
        List<Driver> driversWithThisVehicle = driverRepository.findByActiveVehicle(vehicle);
        for (Driver driver : driversWithThisVehicle) {
            driver.setActiveVehicle(null);
            driverRepository.save(driver);
        }

        // удаление
        vehicleRepository.delete(vehicle);

        // событие удаление — публикуем по данным *удалённой* сущности
        publishDomainEvent(vehicle, VehicleDomainEvent.Action.DELETED, getAuthenticatedManagerIdOrNull());
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

    // ======================== вспомогательное ========================

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
}


