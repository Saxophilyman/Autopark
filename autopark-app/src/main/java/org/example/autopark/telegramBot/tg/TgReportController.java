package org.example.autopark.telegramBot.tg;

import lombok.RequiredArgsConstructor;
import org.example.autopark.Report.PeriodType;
import org.example.autopark.Report.ReportService;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.VehicleService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/internal/tg")
@RequiredArgsConstructor
public class TgReportController {

    private final VehicleService vehicleService;
    private final ReportService reportService;

    // GET /internal/tg/report/mileage?licensePlate=А123ВС77&from=2025-01-01&to=2025-01-31&period=DAY
    @GetMapping("/report/mileage")
    public String mileage(@RequestParam String licensePlate,
                          @RequestParam String from,
                          @RequestParam String to,
                          @RequestParam PeriodType period) {
        Vehicle v = vehicleService.findByLicensePlate(licensePlate);
        var fromDate = LocalDate.parse(from);
        var toDate   = LocalDate.parse(to);

        var report = reportService.generateMileageReport(v.getVehicleId(), fromDate, toDate, period);

        var sb = new StringBuilder("Отчёт о пробеге:\n");
        report.getResult().forEach(e -> {
            if (e.getValue() >= 1.0) {
                sb.append(e.getPeriod())
                        .append(": ")
                        .append(Math.round(e.getValue()))
                        .append(" км\n");
            }
        });
        return sb.length() == "Отчёт о пробеге:\n".length()
                ? "Нет значимого пробега."
                : sb.toString();
    }
}
