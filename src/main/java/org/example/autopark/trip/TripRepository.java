package org.example.autopark.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    // Найти поездки для заданного авто в указанном временном диапазоне
    @Query("""
            SELECT t FROM Trip t WHERE t.vehicleOfTrip.vehicleId = :vehicleId
            AND t.startDate >= :startTripDate AND t.endDate <= :endTripDate
            """)
    List<Trip> findTripsWithinRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startTripDate") Instant startTripDate,
            @Param("endTripDate") Instant endTripDate
    );
//можно использовать аналог:
//    List<Trip> findByVehicleOfTripAndStartDateAfterAndEndDateBefore
//            (Vehicle vehicleOfTrip, @NotNull Instant startDateAfter, @NotNull Instant endDateBefore);

}
