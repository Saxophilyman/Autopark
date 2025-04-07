package org.example.autopark.Report;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
    private ReportType reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double mileage;
    private List<ReportEntry> result;
}

/*
//вариант для сущности
@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Название отчёта

    @Enumerated(EnumType.STRING)
    private ReportType type; // Тип отчёта (пробег, топливо и т.п.)

    @Enumerated(EnumType.STRING)
    private ReportPeriod period; // Период разбиения: ДЕНЬ, МЕСЯЦ, ГОД

    private LocalDate startDate;
    private LocalDate endDate;

    // Результат отчёта: список пар "дата — значение"
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportEntry> result;
}
*/