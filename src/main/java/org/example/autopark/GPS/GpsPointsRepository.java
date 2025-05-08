package org.example.autopark.GPS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GpsPointsRepository extends JpaRepository<GpsPoint, Long> {

    @Query("""
            SELECT g FROM GpsPoint g WHERE g.vehicleIdForGps.vehicleId = :vehicleId
            AND g.timestamp BETWEEN :start AND :end ORDER BY g.timestamp
            """)
    List<GpsPoint> findTrackByVehicleAndTimeRange(
            @Param("vehicleId") Long vehicleId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT g FROM GpsPoint g WHERE g.vehicleIdForGps.vehicleId = :vehicleId AND g.timestamp BETWEEN :start AND :end ORDER BY g.timestamp ASC")
    List<GpsPoint> findByVehicleAndTimeRange(@Param("vehicleId") Long vehicleId,
                                             @Param("start") Instant start,
                                             @Param("end") Instant end);

    @Query("""
    SELECT g FROM GpsPoint g 
    WHERE g.vehicleIdForGps.vehicleId = :vehicleId 
    AND g.timestamp IN :timestamps
""")
    List<GpsPoint> findGpsPointsForTrips(
            @Param("vehicleId") Long vehicleId,
            @Param("timestamps") Set<Instant> timestamps
    );


    @Query("SELECT p FROM GpsPoint p WHERE p.vehicleIdForGps.vehicleId = :vehicleId AND p.timestamp = :time")
    Optional<GpsPoint> findFirstByVehicleIdAndTimeOfPointGps(@Param("vehicleId") Long vehicleId, @Param("time") Instant time);

    @Query(value = """
    SELECT g.* FROM gps_points g
    WHERE g.vehicle_id_for_gps = :vehicleId
    AND g.timestamp BETWEEN :startTripDate AND :endTripDate
    AND EXISTS (
        SELECT 1 FROM trips t
        WHERE t.vehicle_of_trip = g.vehicle_id_for_gps
        AND t.start_date >= :startTripDate
        AND t.end_date <= :endTripDate
        AND tstzrange(t.start_date, t.end_date, '[]')
            && tstzrange(g.timestamp, g.timestamp, '[]')
    )
""", nativeQuery = true)
    List<GpsPoint> findPointsByTripsAndVehicle(
            @Param("vehicleId") Long vehicleId,
            @Param("startTripDate") Instant startTripDate,
            @Param("endTripDate") Instant endTripDate
    );


}


