package org.example.autopark.trip;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TripDTO {
    private int id;
    private String vehicleId;
    private LocalDateTime startDate; // Время в соответствии с enterprise
    private LocalDateTime endDate; // Время в соответствии с enterprise
    private String startLocationInString;
    private String endLocationInString;
    private String duration;
}
