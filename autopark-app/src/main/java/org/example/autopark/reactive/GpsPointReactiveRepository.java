package org.example.autopark.reactive;

import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Profile("reactive")
public interface GpsPointReactiveRepository extends R2dbcRepository<GpsPointFlat, Long> {

    @Query("""
        SELECT
            gp.point_gps_id        AS id,
            gp.vehicle_id_for_gps  AS vehicle_id,
            ST_Y(gp.location)      AS latitude,
            ST_X(gp.location)      AS longitude,
            gp."timestamp"         AS "timestamp"
        FROM gps_points gp
        WHERE gp.vehicle_id_for_gps = :vehicleId
          AND gp."timestamp" BETWEEN :from AND :to
        ORDER BY gp."timestamp" ASC
    """)
    Flux<GpsPointFlat> streamByVehicleIdBetween(Long vehicleId, Instant from, Instant to);
}
