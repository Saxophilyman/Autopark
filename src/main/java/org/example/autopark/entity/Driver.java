package org.example.autopark.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Driver")
public class Driver {
    @Id
    @Column(name = "driver_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverId;

    @NonNull
    @Column(name = "guid", nullable = false, unique = true, updatable = false)
    private UUID guid = UUID.randomUUID();

    @Column(name = "driver_name")
    @NotBlank
    private String driverName;

    @NotNull
    @Min(value = 1)
    @Column(name = "driver_salary")
    private int driverSalary;

    @Column(name = "is_active")
    private boolean isActive;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "enterprise_owner_of_driver", referencedColumnName = "enterprise_id")
    private Enterprise enterpriseOwnerOfDriver;
    //Предприятию могут принадлежать несколько водителей (один ко многим).


    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_vehicle", referencedColumnName = "vehicle_id")
    private Vehicle activeVehicle;

    //Активный водитель может работать только на одной машине (не может быть назначен активным на второй автомобиль).


    @ManyToMany
    @JoinTable(name = "driver_vehicle",
            joinColumns = @JoinColumn(name = "driver_id"),
            inverseJoinColumns = @JoinColumn(name = "vehicle_id"))
    @JsonIgnore
    private List<Vehicle> vehicleList;
}
