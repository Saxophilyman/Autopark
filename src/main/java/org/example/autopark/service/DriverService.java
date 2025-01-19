package org.example.autopark.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
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
        Optional<Driver> foundDriver = driverRepository.findById(id);
        return foundDriver.orElse(null);
    }

    @Transactional
    public void saveAll(List<Driver> drivers) {
        driverRepository.saveAll(drivers);
    }

    public List<Driver> findDriversForManager(Long managerId) {
        List<Enterprise> enterprises = enterpriseService.findEnterprisesForManager(managerId);
        List<Driver> drivers = new ArrayList<Driver>();
        for(Enterprise enterprise : enterprises) {
            drivers.addAll(driverRepository.findDriversByEnterpriseOwnerOfDriver_EnterpriseId(enterprise.getEnterpriseId()));
        }
        return drivers;
    }
    @Transactional
    public void save(Driver driver) {
        driverRepository.save(driver);
    }

    @Transactional
    public void update(Long id, Driver updatedDriver) {
        updatedDriver.setDriverId(id);
        driverRepository.save(updatedDriver);
    }

    @Transactional
    public void delete(Long id) {
        // Проверяем наличие водителя в базе данных
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + id + " not found"));

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
//    public List<Driver> findAllDrivers(Long managerId) {
//        List<Enterprise> enterprises = enterpriseService.findEnterprisesForManager(managerId);
//        List<Driver> drivers = new ArrayList<Driver>();
//        for(Enterprise enterprise : enterprises) {
//            drivers.addAll(driverRepository.findDriversByEnterpriseOwnerOfDriver_EnterpriseId(enterprise.getEnterpriseId()));
//        }
//        return drivers;
//    }
