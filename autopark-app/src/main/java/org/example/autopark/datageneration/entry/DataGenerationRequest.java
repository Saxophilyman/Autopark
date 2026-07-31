package org.example.autopark.datageneration.entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Запрос на генерацию данных автомобилей и водителей")
public record DataGenerationRequest(
        @JsonProperty("enterprisesID")
        @Schema(
                description = "Список ID предприятий, для которых нужно сгенерировать данные",
                example = "[1, 2, 3]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "Список предприятий не может быть пустым")
        List<@NotNull(message = "ID предприятия не может быть null") Long> enterpriseIds,

        @JsonProperty("numberOfVehicle")
        @Schema(description = "Количество автомобилей для каждого предприятия", example = "10")
        @Min(value = 0, message = "Число автомобилей не может быть отрицательным")
        int vehicleCount,

        @JsonProperty("numberOfDriver")
        @Schema(description = "Количество водителей для каждого предприятия", example = "15")
        @Min(value = 0, message = "Число водителей не может быть отрицательным")
        int driverCount,

        @JsonProperty("indicatorOfActiveVehicle")
        @Schema(
                description = "Шаг назначения водителей. Значение 2 сохраняет прежнее правило назначения с шагом два автомобиля",
                example = "2"
        )
        @Min(value = 1, message = "Шаг назначения должен быть не меньше 1")
        int assignmentStep
) {
}
