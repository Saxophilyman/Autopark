package org.example.autopark.GPS;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.autopark.entity.Vehicle;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "track")
public class PointGPS {

    @Id
    @Column(name = "point_GPS_Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointGPSId;

    @ManyToOne
    @JoinColumn(name = "GPS_of_vehicle_id", referencedColumnName = "vehicle_id")
    private Vehicle vehicleGPS;

    @Column(name = "location", columnDefinition = "geometry(Point,4326)")
    private Point location;
}
