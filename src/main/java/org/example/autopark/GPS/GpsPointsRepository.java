package org.example.autopark.GPS;

import org.example.autopark.trip.Trip;
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

//    @Query("""
//                SELECT g FROM GpsPoint g
//                WHERE g.vehicleIdForGps.vehicleId = :vehicleId
//                AND EXISTS (
//                    SELECT 1 FROM Trip t
//                    WHERE t.vehicleOfTrip.vehicleId = g.vehicleIdForGps.vehicleId
//                    AND t.startDate <= g.timestamp
//                    AND t.endDate >= g.timestamp
//                    AND t.id IN :trips
//                )
//            """)
//    List<GpsPoint> findPointsByTripsAndVehicle(List<Trip> trips, Long vehicleId);
@Query("""
    SELECT g FROM GpsPoint g
    WHERE g.vehicleIdForGps.vehicleId = :vehicleId
    AND EXISTS (
        SELECT 1 FROM Trip t
        WHERE t.vehicleOfTrip.vehicleId = g.vehicleIdForGps.vehicleId
        AND t.startDate <= g.timestamp
        AND t.endDate >= g.timestamp
        AND t.id IN :tripIds
    )
""")
List<GpsPoint> findPointsByTripsAndVehicle(@Param("tripIds") List<Long> tripIds, @Param("vehicleId") Long vehicleId);

}
