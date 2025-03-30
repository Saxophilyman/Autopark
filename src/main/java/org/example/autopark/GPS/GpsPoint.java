package org.example.autopark.GPS;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.example.autopark.entity.Vehicle;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "gps_points")
public class GpsPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_gps_id")
    private Long GpsPointId;

    @NonNull
    @Column(name = "guid", nullable = false, unique = true, updatable = false)
    private UUID guid = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "vehicle_id_for_gps", referencedColumnName = "vehicle_id", nullable = false)
    private Vehicle vehicleIdForGps;

    @Column(name = "location", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "timestamp", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private Instant timestamp; // Время в UTC
}
