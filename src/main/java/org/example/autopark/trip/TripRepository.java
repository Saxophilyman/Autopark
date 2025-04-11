package org.example.autopark.trip;

import org.example.autopark.GPS.GpsPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByGuid(UUID guid);

    @Query("""
            SELECT t FROM Trip t
            WHERE t.vehicleOfTrip.vehicleId = :vehicleId
            AND t.duration BETWEEN :minDuration AND :maxDuration
            """)
    List<Trip> findTripsByVehicleAndDuration(
            @Param("vehicleId") Long vehicleId,
            @Param("minDuration") Duration minDuration,
            @Param("maxDuration") Duration maxDuration
    );

    @Query(value = """
                SELECT t.trips_id, t.vehicle_of_trip, t.start_date, t.end_date, t.duration, t.guid
                FROM trips t
                WHERE t.vehicle_of_trip = :vehicleId
                AND t.start_date >= :startTripDate
                AND tstzrange(t.start_date, t.end_date, '[]')
             && tstzrange(:startTripDate, :endTripDate, '[]')
            """, nativeQuery = true)
    List<Trip> findTripsWithinRange(@Param("vehicleId") Long vehicleId,
                                    @Param("startTripDate") Instant startTripDate,
                                    @Param("endTripDate") Instant endTripDate);


//    @Query(value = """
//    SELECT t FROM trips t
//    WHERE t.vehicle_of_trip = :vehicleId
//    AND t.start_date >= :startTripDate
//    AND tstzrange(t.start_date, t.end_date, '[]')
//        && tstzrange(:startTripDate, :endTripDate, '[]')
//""", nativeQuery = true)
//    List<Trip> findTripsWithinRange(
//            @Param("vehicleId") Long vehicleId,
//            @Param("startTripDate") Instant startTripDate,
//            @Param("endTripDate") Instant endTripDate
//    );
    // Найти поездки для заданного авто в указанном временном диапазоне


}
//@Query("""
//            SELECT t FROM Trip t WHERE t.vehicleOfTrip.vehicleId = :vehicleId
//            AND t.startDate >= :startTripDate AND t.endDate <= :endTripDate
//            """)
//List<Trip> findTripsWithinRange(
//        @Param("vehicleId") Long vehicleId,
//        @Param("startTripDate") Instant startTripDate,
//        @Param("endTripDate") Instant endTripDate
//);

/// / Найти поездки в интервале X - Y
//@Query("""
//        SELECT t FROM Trip t WHERE t.vehicleOfTrip.vehicleId = :vehicleId AND t.duration BETWEEN :minDuration AND :maxDuration
//        """)
//List<Trip> findTripsWithinRange(
//        @Param("vehicleId") Long vehicleId,
//        @Param("minDuration") String minDuration,
//        @Param("maxDuration") String maxDuration
//);

//можно использовать аналог:
//    List<Trip> findByVehicleOfTripAndStartDateAfterAndEndDateBefore
//            (Vehicle vehicleOfTrip, @NotNull Instant startDateAfter, @NotNull Instant endDateBefore);
