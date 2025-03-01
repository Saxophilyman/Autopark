package org.example.autopark.repository;

import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    List<Enterprise> findEnterprisesByManagerList_managerId(Long id);

    boolean existsByManagerListContainsAndEnterpriseId(Manager manager, Long id);

    @Query("SELECT e.timeZone FROM Enterprise e JOIN Vehicle v ON v.enterpriseOwnerOfVehicle = e WHERE v.vehicleId = :vehicleId")
    String findTimeZoneByVehicleId(@Param("vehicleId") Long vehicleId);

}
