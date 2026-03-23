package org.example.autopark.telegrambot.tg;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.report.PeriodType;
import org.example.autopark.report.ReportService;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Slf4j
@Hidden
@Profile("!reactive")
@RestController
@RequestMapping("/internal/tg")
@RequiredArgsConstructor
public class TgReportController {

    private final VehicleService vehicleService;
    private final ReportService reportService;
    private final EnterpriseService enterpriseService;

    @Value("${internal.api.token:dev-token}")
    private String internalToken;

    // GET /internal/tg/report/mileage?managerId=1&licensePlate=А123ВС77&from=2025-01-01&to=2025-01-31&period=DAY
    @GetMapping("/report/mileage")
    public String mileage(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestParam Long managerId,
            @RequestParam String licensePlate,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam PeriodType period
    ) {
        // 1) сервисная аутентификация
        if (token == null || !internalToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid X-Internal-Token");
        }

        // 2) находим машину
        Vehicle v = vehicleService.findByLicensePlate(licensePlate);
        if (v == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found: " + licensePlate);
        }

        // 3) проверяем доступ менеджера к предприятию машины (как в ReportController)
        Enterprise e = v.getEnterpriseOwnerOfVehicle();
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle has no enterprise bound");
        }
        boolean allowed = enterpriseService.managerHasEnterprise(managerId, e.getEnterpriseId());
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this vehicle");
        }

        // 4) парсим даты
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        if (fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from > to");
        }

        // 5) генерим отчёт
        var report = reportService.generateMileageReport(v.getVehicleId(), fromDate, toDate, period);

        var sb = new StringBuilder("Отчёт о пробеге:\n");
        report.getResult().forEach(entry -> {
            if (entry.getValue() != null && entry.getValue() >= 1.0) {
                sb.append(entry.getPeriod())
                        .append(": ")
                        .append(Math.round(entry.getValue()))
                        .append(" км\n");
            }
        });

        return sb.length() == "Отчёт о пробеге:\n".length()
                ? "Нет значимого пробега."
                : sb.toString();
    }
}

