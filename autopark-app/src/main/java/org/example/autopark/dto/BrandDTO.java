package org.example.autopark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Бренд (модель) транспортного средства")
public class BrandDTO {

    @Schema(
            description = "Уникальный идентификатор бренда",
            example = "1"
    )
    private Long brandId;

    @Schema(
            description = "Название бренда или модели",
            example = "Toyota Camry"
    )
    private String brandName;
}
