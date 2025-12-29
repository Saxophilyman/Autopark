package org.example.autopark.gps;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "GPS-точка трека в локальном времени предприятия")
public class GpsPointDto {

    @Schema(
            description = "ID GPS-точки",
            example = "1001",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long GpsPointId;

    @Schema(
            description = "ID автомобиля (строкой, для удобства отображения)",
            example = "1"
    )
    private String vehicleId;

    @Schema(
            description = "Широта точки",
            example = "55.757071"
    )
    private double latitude;

    @Schema(
            description = "Долгота точки",
            example = "37.614720"
    )
    private double longitude;

    @Schema(
            description = "Время точки в часовом поясе предприятия",
            example = "2025-12-05T10:15:30"
    )
    private LocalDateTime timestamp;
}
