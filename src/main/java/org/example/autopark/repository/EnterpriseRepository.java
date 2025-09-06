package org.example.autopark.repository;

import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Manager;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("!reactive")
public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    Optional<Enterprise> findByGuid(UUID guid);

    List<Enterprise> findEnterprisesByManagerList_managerId(Long id);

    boolean existsByManagerListContainsAndEnterpriseId(Manager manager, Long id);

    @Query("SELECT e.timeZone FROM Enterprise e JOIN Vehicle v ON v.enterpriseOwnerOfVehicle = e WHERE v.vehicleId = :vehicleId")
    String findTimeZoneByVehicleId(@Param("vehicleId") Long vehicleId);

}
