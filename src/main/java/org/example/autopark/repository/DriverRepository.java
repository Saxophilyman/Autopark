package org.example.autopark.repository;

import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByGuid(UUID guid);

    List<Driver> findDriversByEnterpriseOwnerOfDriver_EnterpriseId(Long id);

    List<Driver> findByActiveVehicle(Vehicle vehicle);

    @Query("SELECT d FROM Driver d LEFT JOIN FETCH d.activeVehicle WHERE d.driverId = :id")
    Optional<Driver> findByIdWithActiveVehicle(Long id);

    @Query("SELECT d FROM Driver d LEFT JOIN FETCH d.activeVehicle")
    List<Driver> findAllWithActiveVehicle();

    @Query("SELECT d FROM Driver d LEFT JOIN FETCH d.activeVehicle WHERE d.enterpriseOwnerOfDriver.enterpriseId = :enterpriseId")
    List<Driver> findDriversWithVehiclesByEnterpriseId(Long enterpriseId);


}
