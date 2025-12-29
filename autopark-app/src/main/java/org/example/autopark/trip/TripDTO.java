package org.example.autopark.trip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Schema(description = "Поездка автомобиля в локальном времени предприятия")
public class TripDTO {

    @Schema(
            description = "ID поездки",
            example = "42",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private int id;

    @Schema(
            description = "ID автомобиля (строкой для удобства отображения)",
            example = "1"
    )
    private String vehicleId;

    @Schema(
            description = "Время начала поездки в часовом поясе предприятия",
            example = "2025-12-05T10:00:00"
    )
    private LocalDateTime startDate; // локальное время enterprise

    @Schema(
            description = "Время окончания поездки в часовом поясе предприятия",
            example = "2025-12-05T11:15:30"
    )
    private LocalDateTime endDate; // локальное время enterprise

    @Schema(
            description = "Адрес/описание начальной точки поездки",
            example = "Россия, Москва, Красная площадь"
    )
    private String startLocationInString;

    @Schema(
            description = "Адрес/описание конечной точки поездки",
            example = "Россия, Москва, ВДНХ"
    )
    private String endLocationInString;

    @Schema(
            description = "Длительность поездки (формат зависит от БД, обычно HH:mm:ss)",
            example = "01:15:30"
    )
    private String duration;
}
