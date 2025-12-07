package org.example.autopark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Предприятие автопарка")
public class EnterpriseDTO {
    @Schema(
            description = "Уникальный идентификатор предприятия",
            example = "1"
    )
    private Long enterpriseId;

    @NotEmpty
    @Schema(
            description = "Название предприятия",
            example = "Автоваз"
    )
    private String name;

    @NotEmpty
    @Schema(
            description = "Город, в котором находится предприятие",
            example = "Тольятти"
    )
    private String cityOfEnterprise;

    @Schema(
            description = "Часовой пояс предприятия в формате IANA",
            example = "Europe/Moscow"
    )
    private String timeZone; // Если null/пусто — маппер/сущность поставят UTC
}
