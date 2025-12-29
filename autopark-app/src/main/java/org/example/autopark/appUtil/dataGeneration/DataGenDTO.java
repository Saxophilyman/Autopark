package org.example.autopark.appUtil.dataGeneration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Schema(description = "Запрос на генерацию тестовых данных (машины и водители)")
public class DataGenDTO {

    @Schema(
            description = "Список ID предприятий, для которых нужно сгенерировать данные",
            example = "[1, 2, 3]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "Список предприятий не может быть пустым")
    private List<@NotNull(message = "ID предприятия не может быть null") Long> enterprisesID;

    @Schema(
            description = "Количество машин, генерируемых для каждого предприятия",
            example = "10"
    )
    @Min(value = 0, message = "Число машин не может быть отрицательным")
    private int numberOfVehicle;

    @Schema(
            description = "Количество водителей, генерируемых для каждого предприятия",
            example = "15"
    )
    @Min(value = 0, message = "Число водителей не может быть отрицательным")
    private int numberOfDriver;

    @Schema(
            description = """
                    Коэффициент активных машин.
                    Например, 2 — значит каждая вторая машина будет иметь активного водителя.
                    """,
            example = "2"
    )
    @Min(value = 1, message = "indicatorOfActiveVehicle должен быть >= 1")
    private int indicatorOfActiveVehicle;
}
