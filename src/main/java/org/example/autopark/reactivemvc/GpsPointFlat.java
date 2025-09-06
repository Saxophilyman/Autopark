//package org.example.autopark.reactivemvc;
//
//import org.springframework.data.annotation.Id;
//import org.springframework.data.relational.core.mapping.Column;
//import org.springframework.data.relational.core.mapping.Table;
//
//// DTO под кастомный @Query (алиасы в SQL должны совпадать с @Column)
//@Table("gps_points")
//public class GpsPointFlat {
//    @Id
//    @Column("id")
//    private Long id;
//
//    @Column("vehicle_id_for_gps")
//    private Long vehicleId;
//
//    @Column("latitude")
//    private double latitude;
//
//    @Column("longitude")
//    private double longitude;
//
//    @Column("timestamp")
//    private java.time.Instant timestamp;
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public Long getVehicleId() { return vehicleId; }
//    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
//
//    public double getLatitude() { return latitude; }
//    public void setLatitude(double latitude) { this.latitude = latitude; }
//
//    public double getLongitude() { return longitude; }
//    public void setLongitude(double longitude) { this.longitude = longitude; }
//
//    public java.time.Instant getTimestamp() { return timestamp; }
//    public void setTimestamp(java.time.Instant timestamp) { this.timestamp = timestamp; }
//}
