package org.example.autopark.appUtil.trackGeneration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Параметры генерации тестового GPS-трека для автомобиля")
public class TrackGenDTO {

    @Schema(
            description = "ID автомобиля, для которого нужно сгенерировать трек",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "ID автомобиля обязателен")
    private Long idVehicle;

    @Schema(
            description = "Длина маршрута в километрах (по прямой, реальный маршрут строится по дорогам)",
            example = "10",
            minimum = "1"
    )
    @Min(value = 1, message = "Длина маршрута должна быть не меньше 1 км")
    private int lengthOfTrack;

    @Schema(
            description = """
                    Дата поездки, для которой нужно сгенерировать случайное время старта.
                    Формат: dd-MM-yyyy (например, 01-12-2025).
                    """,
            example = "01-12-2025"
    )
    @NotBlank(message = "Дата обязательна")
    private String date;
}
