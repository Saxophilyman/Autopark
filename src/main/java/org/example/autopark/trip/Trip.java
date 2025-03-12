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

    @Column(name = "start_date", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    @NotNull
    private Instant startDate;

    @NotNull
    @Column(name = "end_date", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private Instant endDate;

    @Column(name = "duration", insertable = false, updatable = false)
    private String duration;  // PostgreSQL интервал хранится как строка

    public Trip(Vehicle vehicleOfTrip, Instant startDate, Instant endDate) {
        this.vehicleOfTrip = vehicleOfTrip;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
