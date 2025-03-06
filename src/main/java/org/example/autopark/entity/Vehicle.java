package org.example.autopark.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.trip.Trip;

import java.time.Instant;
import java.util.List;

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

    @NotBlank
    @Column(name = "vehicle_name")
    private String vehicleName;

    @NotNull
    @Column(name = "vehicle_cost")
    @Min(value = 0)
    private int vehicleCost;

    @NotNull
    @Min(value = 1900)
    @Max(value = 2024)
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

    @OneToOne(mappedBy = "activeVehicle")
    @JsonIgnore
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

    //new
    @OneToMany(mappedBy = "vehicleOfTrip" )
    @JsonIgnore
    private List<Trip> tripList;
}
