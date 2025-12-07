package org.example.autopark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Минимальная информация об автомобиле, привязанном к водителю")
public class VehicleDTOForDriver {

    @Schema(
            description = "Уникальный идентификатор автомобиля",
            example = "42"
    )
    private Long vehicleId;
}
