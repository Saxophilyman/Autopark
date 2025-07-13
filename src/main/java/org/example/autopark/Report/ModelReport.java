package org.example.autopark.Report;


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
public class ModelReport {
    private String reportName;
    private PeriodType periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double mileage;
    private List<ReportEntry> result;
}
