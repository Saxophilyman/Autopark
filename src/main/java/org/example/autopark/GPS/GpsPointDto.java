package org.example.autopark.GPS;

import jakarta.persistence.*;
import lombok.*;
import org.example.autopark.entity.Vehicle;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GpsPointDto {

    private Long GpsPointId;

    private String vehicleId;

    private double latitude;  // Извлекаем координаты (широта)
    private double longitude; // Извлекаем координаты (долгота)

    private LocalDateTime timestamp; // Время в соответствии с enterprise
}
