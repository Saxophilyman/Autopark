package org.example.autopark.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@Profile("!reactive")
@Transactional(readOnly = true)
public class DriverService {
    private final DriverRepository driverRepository;
    private final EnterpriseService enterpriseService;

    @Autowired
    public DriverService(DriverRepository driverRepository, EnterpriseService enterpriseService) {
        this.driverRepository = driverRepository;
        this.enterpriseService = enterpriseService;
    }


    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    public Driver findOne(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + id + " not found"));
    }

    @Transactional
    public void saveAll(List<Driver> drivers) {
        driverRepository.saveAll(drivers);
    }

    public List<Driver> findDriversForManager(Long managerId) {
        List<Enterprise> enterprises = enterpriseService.findEnterprisesForManager(managerId);
        List<Driver> drivers = new ArrayList<Driver>();
        for (Enterprise enterprise : enterprises) {
            drivers.addAll(driverRepository.findDriversByEnterpriseOwnerOfDriver_EnterpriseId(enterprise.getEnterpriseId()));
        }
        return drivers;
    }

    public List<Driver> findDriversForEnterprise(Long enterpriseId) {
        return driverRepository.findDriversByEnterpriseOwnerOfDriver_EnterpriseId(enterpriseId);
    }

    @Transactional
    public void save(Driver driver) {
        driverRepository.save(driver);
    }

    @Transactional
    public void update(Long id, Driver updatedDriver) {
        Driver existing = findOne(id);

        if (updatedDriver.getDriverName() != null) {
            existing.setDriverName(updatedDriver.getDriverName());
        }

        // зарплата — примитив, его можно просто перезаписать
        existing.setDriverSalary(updatedDriver.getDriverSalary());

        existing.setActive(updatedDriver.isActive());

        // Enterprise и связи с машинами тут специально не трогаем.
        driverRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        // Проверяем наличие водителя в базе данных
        Driver driver = findOne(id);

        // Удаляем водителя из связей (если это необходимо)
        if (driver.getActiveVehicle() != null) {
            driver.setActiveVehicle(null); // Убираем активное транспортное средство
        }
        if (driver.getVehicleList() != null) {
            driver.getVehicleList().clear(); // Очищаем список транспортных средств
        }
        if (driver.getEnterpriseOwnerOfDriver() != null) {
            driver.setEnterpriseOwnerOfDriver(null); // Убираем связь с предприятием
        }
        // Удаляем водителя из базы данных
        driverRepository.delete(driver);
    }

}