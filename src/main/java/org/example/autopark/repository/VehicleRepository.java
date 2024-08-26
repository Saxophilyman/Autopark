package org.example.autopark.repository;

import org.example.autopark.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // List<Vehicle> findVehiclesByEnterpriseOwnerOfVehicle_VehicleId(Long id);

    List<Vehicle> findVehiclesByEnterpriseOwnerOfVehicle_EnterpriseId(Long id);
}
