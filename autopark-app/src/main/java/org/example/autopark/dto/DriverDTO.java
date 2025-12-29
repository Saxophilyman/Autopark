package org.example.autopark.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Водитель автопарка")
public class DriverDTO {

    @Schema(
            description = "Уникальный идентификатор водителя",
            example = "10",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long driverId;

    @NotEmpty(message = "Обязательное поле")
    @Schema(
            description = "Имя водителя",
            example = "Иван Иванов"
    )
    private String name;

    @NotEmpty(message = "Обязательное поле")
    @Schema(
            description = "Оклад водителя (строкой, как в текущей модели)",
            example = "55000"
    )
    private String salary;

    @Schema(
            description = "Предприятие, к которому относится водитель." +
                    " В API обычно определяется через path-параметр enterpriseId."
    )
    private EnterpriseDTO enterprise;

    @Schema(
            description = "Активен ли водитель",
            example = "true"
    )
    private boolean isActive;

    @Schema(
            description = "Текущий активный автомобиль водителя (если назначен)"
    )
    private VehicleDTOForDriver activeVehicle;
}
