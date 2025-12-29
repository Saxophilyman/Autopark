package org.example.autopark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Краткая информация об автомобиле в списке (менеджерский API)")
public class VehicleApiDto {
    @Schema(description = "ID автомобиля", example = "42")
    private Long vehicleId;
    @Schema(description = "Название/описание автомобиля", example = "Camry тестовая")
    private String vehicleName;
    @Schema(description = "Госномер", example = "А123ВС")
    private String licensePlate;
    @Schema(description = "Стоимость, в базовой валюте", example = "1200000")
    private int vehicleCost;
    @Schema(description = "Год выпуска", example = "2020")
    private int vehicleYearOfRelease;
    @Schema(description = "ID бренда", example = "1")
    private Long brandId; // Передаём только ID бренда
    @Schema(description = "ID предприятия", example = "1")
    private Long enterpriseId; // Передаём только ID предприятия
    @Schema(
            description = "Дата покупки в часовом поясе предприятия",
            example = "2024-01-01T09:00:00"
    )
    private String purchaseDateEnterpriseTime;
}
