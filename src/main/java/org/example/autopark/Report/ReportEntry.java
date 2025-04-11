package org.example.autopark.Report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportEntry {
    private String period; // Период в виде строки (например, "2025-04-01" или "2025-04")
    private Double value;  // Значение (пробег в км)
}
