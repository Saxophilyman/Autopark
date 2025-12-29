package org.example.autopark.repository;

import org.example.autopark.entity.Vehicle;
import org.example.autopark.trip.VehicleTripsRowDto;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("!reactive")
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    Optional<Vehicle> findByGuid(UUID guid);

    List<Vehicle> findVehiclesByEnterpriseOwnerOfVehicle_EnterpriseId(Long id);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndVehicleIdNot(String licensePlate, Long vehicleId);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByLicensePlateContainingIgnoreCase(String licensePlatePart);


    // --- ДЕМО N+1 ---


    /**
     * Вариант B: Fetch Join — грузим машины с поездками одним запросом
     */
    @Query("""
            SELECT DISTINCT v
            FROM Vehicle v
            LEFT JOIN FETCH v.tripList
            """)
    List<Vehicle> findAllWithTripsFetch();

    /**
     * Вариант B (фильтр по предприятию, если нужно)
     */
    @Query("""
                    SELECT DISTINCT v
                    FROM Vehicle v
                    LEFT JOIN FETCH v.tripList
                    WHERE v.enterpriseOwnerOfVehicle.enterpriseId = :enterpriseId
            """)
    List<Vehicle> findAllByEnterpriseWithTripsFetch(Long enterpriseId);

//    связываемся именно с v.tripList (как в твоём Vehicle).
//    DISTINCT обязателен, чтобы убрать дубликаты машин после join fetch

    /** Вариант C: EntityGraph — декларативно подсказать, что подгружать */
    @EntityGraph(attributePaths = "tripList")
    @Query("SELECT v FROM Vehicle v")
    List<Vehicle> findAllWithTripsByGraph();


    @Query("""
            SELECT new org.example.autopark.trip.VehicleTripsRowDto(
            v.vehicleId, v.licensePlate, COUNT(t)
            )
            FROM Vehicle v
                       LEFT JOIN v.tripList t
                                   GROUP BY v.vehicleId, v.licensePlate
            """)
    List<VehicleTripsRowDto> listWithTripsCount();


    @Query("select v.vehicleId from Vehicle v")
    List<Long> findIds(Pageable pageable);
}
