package org.example.autopark.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Vehicles_Drivers")
public class VehiclesDrivers {
    @Id
    //@Column(name = )
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
