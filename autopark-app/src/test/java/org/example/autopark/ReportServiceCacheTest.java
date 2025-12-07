package org.example.autopark;

import org.example.autopark.report.ModelReport;
import org.example.autopark.report.PeriodType;
import org.example.autopark.report.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReportServiceCacheTest {

    @Autowired
    private ReportService reportService;

    private final Long testVehicleId = 1L;
    private final LocalDate start = LocalDate.of(2025, 1, 1);
    private final LocalDate end = LocalDate.of(2025, 1, 31);
    private final PeriodType period = PeriodType.DAY;

    @Test
    public void testReportCachePerformance() {
        System.out.println("== Первый вызов (без кэша):");
        long startTime1 = System.nanoTime();
        ModelReport firstReport = reportService.generateMileageReport(testVehicleId, start, end, period);
        long duration1 = System.nanoTime() - startTime1;
        System.out.println("Время первого вызова: " + duration1 / 1_000_000 + " ms");

        System.out.println("== Второй вызов (с кэшем):");
        long startTime2 = System.nanoTime();
        ModelReport cachedReport = reportService.generateMileageReport(testVehicleId, start, end, period);
        long duration2 = System.nanoTime() - startTime2;
        System.out.println("Время второго вызова: " + duration2 + " ns (" + duration2 / 1_000_000.0 + " ms)");

        assertEquals(firstReport.getMileage(), cachedReport.getMileage(), 0.01);
    }
}
