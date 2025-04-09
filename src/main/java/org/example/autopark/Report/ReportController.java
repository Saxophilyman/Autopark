package org.example.autopark.Report;

import jakarta.servlet.http.HttpSession;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
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
public class ReportController {
    private final ReportService reportService;
    private final VehicleService vehicleService;
    private final EnterpriseRepository enterpriseRepository;
    private final EnterpriseService enterpriseService;

    public ReportController(ReportService reportService, VehicleService vehicleService, EnterpriseRepository enterpriseRepository, EnterpriseService enterpriseService) {
        this.reportService = reportService;
        this.vehicleService = vehicleService;
        this.enterpriseRepository = enterpriseRepository;

        this.enterpriseService = enterpriseService;
    }


    //кнопка-ссылка на выбор типа отчёта
    @GetMapping("managers/report/showTypeOfReport")
    public String showTypeOfReport(@CurrentManagerId
                                    Model model) {
        // Добавляем доступные типы отчётов в модель
        // позже можно будет перенести в enum
        List<String> reportTypes = Arrays.asList("Пробег автомобиля", "Отчёт 2", "Отчёт 3");
        model.addAttribute("reportTypes", reportTypes);
        return "/report/typeOfReport";
    }

    @GetMapping("managers/report/typeOfGenerate")
    public String typeOfGenerate(@RequestParam("reportType") String reportType, Model model) {
        // В зависимости от типа отчёта, перенаправляем на нужный метод
        switch (reportType) {
            case "Пробег автомобиля":
                return "/report/vehiclesReport";  // Путь для отчёта по автомобилям
            case "Отчёт 2":
                return "redirect:/managers/report/enterpriseReport";  // Путь для отчёта по предприятию
            case "Отчёт 3":
                return "redirect:/managers/report/driverReport";  // Путь для отчёта по водителю
//            case "По поездкам":
//                return "redirect:/managers/report/tripReport";  // Путь для отчёта по поездкам
//            case "По трекам":
//                return "redirect:/managers/report/tracksReport";  // Путь для отчёта по трекам
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный тип отчёта");
        }
    }


//    //кнопка-ссылка на страницу
//    @GetMapping("/managers/report/vehiclesReport")
//    public String showFormForReport(@CurrentManagerId
//                                    @PathVariable("vehicleId") Long vehicleId,
//                                    Model model, @PathVariable String enterpriseId) {
//
//        // Загружаем транспортное средство
//        Vehicle vehicle = vehicleService.findOne(vehicleId);
//        model.addAttribute("vehicle", vehicle);
//
//        return "/report/showFormForReport";
//    }

    @PostMapping("managers/report/generateVehicleReport")
    public String generateReport(@CurrentManagerId Long managerId,
                                 @RequestParam("vehicleId") Long vehicleId,
                                 @RequestParam("fromDate") String fromDate,
                                 @RequestParam("toDate") String toDate,
                                 @RequestParam("period") String period,
//                                 @RequestParam("reportType") String reportType,
                                 Model model, HttpSession session) {
        // Проверка наличия транспортного средства
        Vehicle vehicle = vehicleService.findOne(vehicleId);
        if (vehicle == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Транспортное средство не найдено");
        }

        // Проверка принадлежности автомобиля предприятию
        Enterprise enterprise = vehicle.getEnterpriseOwnerOfVehicle();
        if (enterprise == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Транспортное средство не привязано к предприятию");
        }
        // Проверка, что предприятие принадлежит текущему менеджеру
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterprise.getEnterpriseId());
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }


        LocalDate startDate;
        LocalDate endDate;
        if ("MONTH".equals(period)) {
            // Для месяца просто указываем первый день месяца
            startDate = LocalDate.parse(fromDate + "-01"); // добавляем день как 01
            endDate = LocalDate.parse(toDate + "-01").withDayOfMonth(1).plusMonths(1).minusDays(1);

        } else {
            // Преобразование строковых дат в LocalDate
            startDate = LocalDate.parse(fromDate);
            endDate = LocalDate.parse(toDate);
        }
        // Валидация дат
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Начальная дата не может быть позже конечной");
        }

        // Генерация отчёта на основе выбранных параметров
        ModelReport report = reportService.generateMileageReport(vehicleId, startDate, endDate, PeriodType.valueOf(period.toUpperCase()));

        // Сохраняем отчёт в сессии
        session.setAttribute("report", report);

        // Перенаправляем на страницу с результатом
        return "redirect:/managers/report/view";
    }


    @GetMapping("managers/report/view")
    public String viewReport(@CurrentManagerId
//                             @PathVariable("enterpriseId") Long enterpriseId,
//                             @PathVariable("vehicleId") Long vehicleId,
                             Model model, HttpSession session) {

        // Получаем отчёт из сессии
        ModelReport report = (ModelReport) session.getAttribute("report");

        // Если отчёт не был передан, перенаправляем обратно на форму для отчёта
        if (report == null) {
            return "/report/vehiclesReport";
        }

        // Добавляем отчёт в модель для отображения
        model.addAttribute("report", report);

        // Удаляем отчёт из сессии после отображения
        session.removeAttribute("report");
        return "/report/viewReport";  // Страница для отображения отчёта
    }


    // REST API
    @GetMapping("api/managers/report/generateVehicleReport")
    public ResponseEntity<ModelReport> generateMileageReport(
            @CurrentManagerId Long managerId,
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("startDate") String fromDate,
            @RequestParam("endDate") String toDate,
            @RequestParam("period") String period) {

        // Проверка наличия транспортного средства
        Vehicle vehicle = vehicleService.findOne(vehicleId);
        if (vehicle == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Транспортное средство не найдено");
        }

        // Проверка принадлежности автомобиля предприятию
        Enterprise enterprise = vehicle.getEnterpriseOwnerOfVehicle();
        if (enterprise == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Транспортное средство не привязано к предприятию");
        }
        // Проверка, что предприятие принадлежит текущему менеджеру
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterprise.getEnterpriseId());
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }


        LocalDate startDate;
        LocalDate endDate;
        if ("MONTH".equals(period)) {
            // Для месяца просто указываем первый день месяца
            startDate = LocalDate.parse(fromDate + "-01"); // добавляем день как 01
            endDate = LocalDate.parse(toDate + "-01").withDayOfMonth(1).plusMonths(1).minusDays(1);

        } else {
            // Преобразование строковых дат в LocalDate
            startDate = LocalDate.parse(fromDate);
            endDate = LocalDate.parse(toDate);
        }
        // Валидация дат
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Начальная дата не может быть позже конечной");
        }

        // Генерация отчета
        ModelReport report = reportService.generateMileageReport(vehicleId, startDate, endDate, PeriodType.valueOf(period.toUpperCase()));

        return ResponseEntity.ok(report);
    }
}
