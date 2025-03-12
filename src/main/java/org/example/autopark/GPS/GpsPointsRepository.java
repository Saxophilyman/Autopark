package org.example.autopark.GPS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

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
// Начальный Query
//@Query("""
//    SELECT g FROM GpsPoint g
//    WHERE g.vehicleIdForGps.vehicleId = :vehicleId
//    AND EXISTS (
//        SELECT 1 FROM Trip t
//        WHERE t.vehicleOfTrip.vehicleId = g.vehicleIdForGps.vehicleId
//        AND t.startDate <= g.timestamp
//        AND t.endDate >= g.timestamp
//        AND t.id IN :tripIds
//    )
//""")
//List<GpsPoint> findPointsByTripsAndVehicle(@Param("tripIds") List<Long> tripIds, @Param("vehicleId") Long vehicleId);

// С использованием Overlaps
//    @Query(value = """
//    SELECT g.* FROM gps_points g
//    WHERE g.vehicle_id_for_gps = :vehicleId
//    AND EXISTS (
//        SELECT 1 FROM trips t
//        WHERE t.vehicle_of_trip = g.vehicle_id_for_gps
//        AND (t.start_date, t.end_date) OVERLAPS (g.timestamp, g.timestamp)
//    )
//    """, nativeQuery = true)
//    List<GpsPoint> findPointsByTripsAndVehicle(@Param("tripIds") List<Long> tripIds, @Param("vehicleId") Long vehicleId);