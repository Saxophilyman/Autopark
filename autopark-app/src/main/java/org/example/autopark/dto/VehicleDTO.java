package org.example.autopark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Детальная модель автомобиля для создания/редактирования")
public class VehicleDTO {
    @Schema(
            description = "ID автомобиля (только для чтения, генерируется сервером)",
            example = "42",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long vehicleId;

    @Schema(description = "Название/описание автомобиля", example = "Camry тестовая")
    @NotEmpty(message = "Обязательное поле")
    private String vehicleName;

    @Schema(description = "Госномер", example = "Й123ЦЧ")
    @NotEmpty(message = "Госномер обязателен")
    private String licensePlate;


    @Schema(description = "Стоимость автомобиля", example = "1200000")
    @NotNull
    @Min(value = 0)
    private int vehicleCost;

    @Schema(description = "Год выпуска", example = "2020")
    @NotNull
    @Min(value = 1900, message = "Транспорт может быть только старше 1900 г.")
    @Max(value = 2024, message = "Транспорт не может быть сейчас старше 2024 г.")
    private int vehicleYearOfRelease;

    @Schema(description = "Бренд автомобиля (в запросе важен brandId)")
    private BrandDTO brand;

    @Schema(
            description = "Предприятие-владелец (в запросе используется enterpriseId из URL, а не из этого поля)"
    )
    private EnterpriseDTO enterprise;

    @Schema(
            description = "Дата покупки в часовом поясе предприятия",
            example = "2024-01-01T09:00:00"
    )
    private String purchaseDateEnterpriseTime;

    @Schema(
            description = "Дата покупки в UTC (для чтения/диагностики)",
            example = "2023-12-31T18:00:00Z"
    )
    private String purchaseDateUtc; // Оригинальное UTC время для корректного перевода в браузере

}
