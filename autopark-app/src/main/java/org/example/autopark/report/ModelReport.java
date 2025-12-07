package org.example.autopark.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Отчёт о пробеге автомобиля за период")
public class ModelReport {

    @Schema(description = "Человекочитаемое название отчёта", example = "Пробег автомобиля")
    private String reportName;

    @Schema(
            description = "Тип отчёта (на будущее: пробег авто, пробег предприятия и т.п.)",
            example = "VEHICLE_MILEAGE"
    )
    private ReportType reportType;

    @Schema(
            description = "Тип периода агрегации: DAY — по дням, MONTH — по месяцам, YEAR — по годам",
            example = "DAY"
    )
    private PeriodType periodType;

    @Schema(description = "Дата начала периода отчёта", example = "2025-01-01")
    private LocalDate startDate;

    @Schema(description = "Дата окончания периода отчёта", example = "2025-01-31")
    private LocalDate endDate;

    @Schema(description = "Суммарный пробег за весь период, км", example = "1234.567")
    private Double mileage;

    @Schema(description = "Разбивка пробега по выбранным периодам (дни/месяцы/годы)")
    private List<ReportEntry> result;
}
