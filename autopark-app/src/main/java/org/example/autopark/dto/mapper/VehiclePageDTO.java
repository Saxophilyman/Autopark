package org.example.autopark.dto.mapper;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.autopark.dto.VehicleApiDto;
import org.example.autopark.dto.VehicleDTO;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Страница автомобилей предприятия")
public class VehiclePageDTO {
    @Schema(description = "Список автомобилей на текущей странице")
    private List<VehicleApiDto> vehicles;

    @Schema(description = "Номер текущей страницы (0..N)", example = "0")
    private int currentPage;

    @Schema(description = "Общее количество страниц", example = "5")
    private int totalPages;

    @Schema(description = "Есть ли следующая страница", example = "true")
    private boolean hasNext;

    @Schema(description = "Есть ли предыдущая страница", example = "false")
    private boolean hasPrevious;

    @Schema(
            description = "Часовой пояс предприятия, в котором представлены даты",
            example = "Europe/Moscow"
    )
    private String enterpriseTimezone;
}
