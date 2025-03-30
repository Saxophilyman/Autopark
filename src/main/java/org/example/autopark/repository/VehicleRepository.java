package org.example.autopark.repository;

import org.example.autopark.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    Optional<Vehicle> findByGuid(UUID guid);

    List<Vehicle> findVehiclesByEnterpriseOwnerOfVehicle_EnterpriseId(Long id);
}
