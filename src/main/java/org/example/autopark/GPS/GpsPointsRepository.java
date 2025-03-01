package org.example.autopark.GPS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface GpsPointsRepository extends JpaRepository<GpsPoint, Long> {

    @Query("SELECT g FROM GpsPoint g WHERE g.vehicleIdForGps.vehicleId = :vehicleId " +
            "AND g.timestamp BETWEEN :start AND :end ORDER BY g.timestamp")
    List<GpsPoint> findTrackByVehicleAndTimeRange(
            @Param("vehicleId") Long vehicleId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
