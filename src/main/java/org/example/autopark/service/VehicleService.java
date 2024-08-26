package org.example.autopark.service;

import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final BrandService brandService;
    private final EnterpriseService enterpriseService;

    @Autowired
    public VehicleService(VehicleRepository vehicleRepository, BrandService brandService, EnterpriseService enterpriseService) {
        this.vehicleRepository = vehicleRepository;
        this.brandService = brandService;
        this.enterpriseService = enterpriseService;
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

        vehicleRepository.save(updatedVehicle);
    }

    @Transactional
    public void update(Long id, Vehicle updatedVehicle) {
        updatedVehicle.setVehicleId(id);
        vehicleRepository.save(updatedVehicle);
    }

    @Transactional
    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }

    public List<Vehicle> findVehiclesForManager(Long managerId) {

        List<Enterprise> enterprises = enterpriseService.findEnterprisesForManager(managerId);

        List<Vehicle> vehicles = new ArrayList<Vehicle>();

        for(Enterprise enterprise : enterprises) {
            vehicles.addAll(vehicleRepository.findVehiclesByEnterpriseOwnerOfVehicle_EnterpriseId(enterprise.getEnterpriseId()));
        }

        return vehicles;
    }

    private void enrichVehicle(Vehicle vehicle) {
        vehicle.setEnterpriseOwnerOfVehicle(null);
    }
}
