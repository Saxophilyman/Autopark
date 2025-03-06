package org.example.autopark.trip;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.autopark.entity.Vehicle;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trips")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trips_id")
    private Long id;

    //связь с vihicle - м.б. много поездок у 1 автомобиля
    @ManyToOne
    @JoinColumn(name = "vehicle_of_trip", referencedColumnName = "vehicle_id", nullable = false)
    private Vehicle vehicleOfTrip;

    @Column(name = "startDate", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    @NotNull
    private Instant startDate;

    @NotNull
    @Column(name = "endDate", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private Instant endDate;

}
