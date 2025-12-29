package org.example.autopark.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Строка отчёта: период → значение")
public class ReportEntry {

    @Schema(
            description = "Период. Для DAY — дата (yyyy-MM-dd), для MONTH — строка вида \"Март 2025\", для YEAR — год",
            example = "2025-01-01"
    )
    private String period;

    @Schema(description = "Пробег за указанный период, км", example = "123.456")
    private Double value;
}
