package org.example.autopark.repository;

import org.example.autopark.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    // List<Vehicle> findVehiclesByEnterpriseOwnerOfVehicle_VehicleId(Long id);
    Page<Vehicle> findVehiclesByEnterpriseOwnerOfVehicle_EnterpriseId(Long id, Pageable pageable);
    List<Vehicle> findVehiclesByEnterpriseOwnerOfVehicle_EnterpriseId(Long id);

    Page<Vehicle> findAllByEnterpriseOwnerOfVehicle_EnterpriseIdIn(List<Long> enterpriseIds, Pageable pageable);
}
