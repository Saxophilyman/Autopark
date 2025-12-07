package org.example.autopark.report;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@Profile("!reactive")
@Tag(
        name = "Reports (Manager API)",
        description = "Отчёты по пробегу автомобилей менеджера"
)
public class ReportController {

    private final ReportService reportService;
    private final VehicleService vehicleService;
    private final EnterpriseRepository enterpriseRepository;
    private final EnterpriseService enterpriseService;

    public ReportController(ReportService reportService,
                            VehicleService vehicleService,
                            EnterpriseRepository enterpriseRepository,
                            EnterpriseService enterpriseService) {
        this.reportService = reportService;
        this.vehicleService = vehicleService;
        this.enterpriseRepository = enterpriseRepository;
        this.enterpriseService = enterpriseService;
    }

    // ─────────────────────────────────────────────────────────────────────
    // UI: выбор типа отчёта
    // ─────────────────────────────────────────────────────────────────────
    @Hidden
    @GetMapping("managers/report/showTypeOfReport")
    public String showTypeOfReport(
            @CurrentManagerId Long managerId,
            Model model
    ) {
        List<String> reportTypes = Arrays.asList("Пробег автомобиля", "Отчёт 2", "Отчёт 3");
        model.addAttribute("reportTypes", reportTypes);
        return "/report/typeOfReport";
    }

    @Hidden
    @GetMapping("managers/report/typeOfGenerate")
    public String typeOfGenerate(@RequestParam("reportType") String reportType, Model model) {
        switch (reportType) {
            case "Пробег автомобиля":
                return "/report/vehiclesReport";
            case "Отчёт 2":
                return "redirect:/managers/report/enterpriseReport";
            case "Отчёт 3":
                return "redirect:/managers/report/driverReport";
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный тип отчёта");
        }
    }

    @Hidden
    @PostMapping("managers/report/generateVehicleReport")
    public String generateReport(
            @CurrentManagerId Long managerId,
            @RequestParam("licensePlate") String licensePlate,
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate,
            @RequestParam("period") String period,
            Model model,
            HttpSession session
    ) {
        Vehicle vehicle = vehicleService.findByLicensePlate(licensePlate);
        if (vehicle == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ТС с номером " + licensePlate + " не найдено");
        }

        ModelReport report = buildVehicleMileageReport(managerId, vehicle, fromDate, toDate, period);

        session.setAttribute("report", report);
        session.setAttribute("vehicle", vehicle);

        return "redirect:/managers/report/view";
    }

    @Hidden
    @GetMapping("managers/report/view")
    public String viewReport(
            @CurrentManagerId Long managerId,
            Model model,
            HttpSession session
    ) {
        ModelReport report = (ModelReport) session.getAttribute("report");
        Vehicle vehicle = (Vehicle) session.getAttribute("vehicle");

        if (report == null) {
            return "/report/vehiclesReport";
        }

        model.addAttribute("report", report);
        model.addAttribute("vehicle", vehicle);

        session.removeAttribute("report");
        session.removeAttribute("vehicle");

        return "/report/viewReport";
    }

    // ─────────────────────────────────────────────────────────────────────
    // REST API: отчёт по пробегу автомобиля
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("api/managers/report/generateVehicleReport")
    @Operation(
            summary = "Сгенерировать отчёт о пробеге автомобиля",
            description = """
                    Считает пробег автомобиля по GPS-точкам за указанный период и возвращает агрегированный отчёт.
                    Период может быть: DAY (по дням), MONTH (по месяцам), YEAR (по годам).
                    Даты передаются как строки: для DAY/YEAR — yyyy-MM-dd, для MONTH можно указать yyyy-MM.
                    """
    )
    public ResponseEntity<ModelReport> generateMileageReport(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @Parameter(description = "ID автомобиля", example = "1")
            @RequestParam("vehicleId") Long vehicleId,

            @Parameter(
                    description = "Дата начала периода (yyyy-MM-dd или yyyy-MM при периоде MONTH)",
                    example = "2025-01-01"
            )
            @RequestParam("startDate") String fromDate,

            @Parameter(
                    description = "Дата окончания периода (yyyy-MM-dd или yyyy-MM при периоде MONTH)",
                    example = "2025-01-31"
            )
            @RequestParam("endDate") String toDate,

            @Parameter(
                    description = "Тип периода агрегации: DAY, MONTH или YEAR",
                    example = "DAY"
            )
            @RequestParam("period") String period
    ) {

        Vehicle vehicle = vehicleService.findOne(vehicleId);
        if (vehicle == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Транспортное средство не найдено");
        }

        ModelReport report = buildVehicleMileageReport(managerId, vehicle, fromDate, toDate, period);

        return ResponseEntity.ok(report);
    }

    // ─────────────────────────────────────────────────────────────────────
    // REST API: поиск госномеров (используется в UI, но это чистый JSON)
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/api/managers/vehicles/license-plates")
    @ResponseBody
    @Operation(
            summary = "Поиск госномеров по подстроке",
            description = """
                    Используется для автодополнения в формах (Select2).
                    Возвращает список уникальных госномеров, содержащих указанную подстроку.
                    """
    )
    public List<String> getLicensePlates(
            @Parameter(
                    description = "Подстрока для поиска по госномеру",
                    example = "А12"
            )
            @RequestParam("q") String query
    ) {
        return vehicleService.findByLicensePlateContaining(query)
                .stream()
                .map(Vehicle::getLicensePlate)
                .distinct()
                .toList();
    }

    private ModelReport buildVehicleMileageReport(Long managerId,
                                                  Vehicle vehicle,
                                                  String fromDate,
                                                  String toDate,
                                                  String period) {
        Enterprise enterprise = vehicle.getEnterpriseOwnerOfVehicle();
        if (enterprise == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Транспортное средство не привязано к предприятию");
        }

        if (!enterpriseService.managerHasEnterprise(managerId, enterprise.getEnterpriseId())) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        LocalDate startDate;
        LocalDate endDate;
        if ("MONTH".equals(period)) {
            startDate = LocalDate.parse(fromDate + "-01");
            endDate = LocalDate.parse(toDate + "-01")
                    .withDayOfMonth(1)
                    .plusMonths(1)
                    .minusDays(1);
        } else {
            startDate = LocalDate.parse(fromDate);
            endDate = LocalDate.parse(toDate);
        }

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Начальная дата не может быть позже конечной");
        }

        return reportService.generateMileageReport(
                vehicle.getVehicleId(),
                startDate,
                endDate,
                PeriodType.valueOf(period.toUpperCase())
        );
    }

}
