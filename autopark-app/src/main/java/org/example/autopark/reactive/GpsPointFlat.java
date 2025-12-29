package org.example.autopark.reactive;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Читаем в эту проекцию результат кастомного SQL.
 * @Table помогает Spring однозначно отнести тип к R2DBC (даже если SELECT не использует * из таблицы).
 */
@Table("gps_points") // это не обязательно для SELECT с алиасами, но помогает идентификации стора
public class GpsPointFlat {
    @Id
    @Column("id")          // алиас в SELECT
    private Long id;

    @Column("vehicle_id")  // алиас в SELECT
    private Long vehicleId;

    @Column("latitude")    // ST_Y(location) AS latitude
    private double latitude;

    @Column("longitude")   // ST_X(location) AS longitude
    private double longitude;

    @Column("timestamp")   // "timestamp" AS "timestamp"
    private Instant timestamp;

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
