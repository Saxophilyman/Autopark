package org.example.autopark.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.example.autopark.gps.GpsPoint;
import org.example.autopark.trip.Trip;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Vehicle")
public class Vehicle {
    @Id
    @Column(name = "vehicle_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @NonNull
    @Column(name = "guid", nullable = false, unique = true, updatable = false)
    private UUID guid = UUID.randomUUID();

    @NotBlank
    @Column(name = "vehicle_name")
    private String vehicleName;

    @NotBlank
    @Pattern(regexp = "^[А-Я]\\d{3}[А-Я]{2}$", message = "Формат номера должен быть А123БВ")
    @Column(name = "license_plate", unique = true)
    private String licensePlate;


    @NotNull
    @Column(name = "vehicle_cost")
    @Min(value = 0)
    private int vehicleCost;

    @NotNull
    @Min(value = 1900)
    @Max(value = 2026)
    @Column(name = "vehicle_year_of_release")
    private int vehicleYearOfRelease;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "brand_owner", referencedColumnName = "brand_id")
    private Brand brandOwner;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "enterprise_owner_of_vehicle", referencedColumnName = "enterprise_id")
    private Enterprise enterpriseOwnerOfVehicle;
    //Предприятию могут принадлежать несколько автомобилей (один ко многим).

    @JsonIgnore
    @OneToOne(mappedBy = "activeVehicle", fetch = FetchType.LAZY)
    private Driver activeDriver;


    @ManyToMany(mappedBy = "vehicleList")
    @JsonIgnore
    private List<Driver> driverList;
    //Каждому автомобилю может быть назначено несколько водителей.
    
    @Column(name = "purchase_date_utc", nullable = false)
    private Instant purchaseDateUtc; // Дата покупки в UTC

    @PrePersist
    protected void onCreate() {
        if (this.purchaseDateUtc == null) { // Только если дата не была установлена вручную
            this.purchaseDateUtc = Instant.now();
        }
    }

    @OneToMany(mappedBy = "vehicleIdForGps")
    @JsonIgnore
    private List<GpsPoint> PointsGPS;

    @org.hibernate.annotations.BatchSize(size = 32)
    @OneToMany(mappedBy = "vehicleOfTrip" )
    @JsonIgnore
    private List<Trip> tripList;
}
