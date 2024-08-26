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

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Brand")
public class Brand {
    @Id
    @Column(name = "brand_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long brandId;

    @Column(name = "brand_name")
    @NotBlank
    private String brandName;

    @Enumerated
    @NotBlank
    @Min(value = 0)
    @Max(value = 4)
    @Column(name = "type")
    private TypeVehicle brandType;

    @NotNull
    @Min(value = 0)
    @Column(name = "capacity_fuel_tank")
    private int capacityFuelTank;

    @NotNull
    @Min(value = 100)
    @Column(name = "load_capacity")
    private int loadCapacity;

    @NotNull
    @Min(value = 0)
    @Column(name = "number_of_seats")
    private int numberOfSeats;

    @JsonIgnore
    @OneToMany(mappedBy = "brandOwner", cascade = CascadeType.PERSIST)
    private List<Vehicle> vehicles;

}
