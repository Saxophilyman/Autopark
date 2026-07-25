package org.example.autopark.report;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.EnterpriseService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class VehicleMileageReportService {

    private final ReportService reportService;
    private final EnterpriseService enterpriseService;

    public ModelReport buildMileageReportForManager(Long managerId, Vehicle vehicle,
                                                    String fromDate, String toDate, String periodRaw) {
        validateManagerAccessToVehicle(managerId, vehicle);
        PeriodType periodType = parsePeriodType(periodRaw);
        ReportDateRange dateRange = buildDateRange(fromDate, toDate, periodType);

        return reportService.generateMileageReport(
                vehicle.getVehicleId(),
                dateRange.startDate(),
                dateRange.endDate(),
                periodType
        );
    }

    private void validateManagerAccessToVehicle(Long managerId, Vehicle vehicle) {
        Enterprise enterprise = vehicle.getEnterpriseOwnerOfVehicle();

        if (enterprise == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Транспортное средство не привязано к предприятию"
            );
        }

        if (!enterpriseService.managerHasEnterprise(managerId, enterprise.getEnterpriseId())) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }
    }

    private PeriodType parsePeriodType(String periodRaw) {
        try {
            return PeriodType.valueOf(periodRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный тип периода");
        }
    }

    private ReportDateRange buildDateRange(String fromDate, String toDate, PeriodType periodType) {
        try {
            LocalDate startDate;
            LocalDate endDate;

            if (periodType == PeriodType.MONTH) {
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
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Начальная дата не может быть позже конечной"
                );
            }

            return new ReportDateRange(startDate, endDate);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный формат даты");
        }
    }

    private record ReportDateRange(LocalDate startDate, LocalDate endDate) {
    }
}