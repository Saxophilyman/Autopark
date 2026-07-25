package org.example.autopark.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;
import org.example.autopark.gps.GpsPoint;
import org.example.autopark.gps.GpsPointsRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@Profile("!reactive")
@RequiredArgsConstructor
public class ReportService {

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final Locale RU_LOCALE = new Locale("ru", "RU");

    private final GpsPointsRepository gpsPointsRepository;

    @Cacheable(
            value = "mileageReports",
            key = "#vehicleId + '_' + #startDate.toString() + '_' + #endDate.toString() + '_' + #period.name()"
    )
    public ModelReport generateMileageReport(Long vehicleId, LocalDate startDate,
                                             LocalDate endDate, PeriodType period) {
        log.info("Генерация отчёта: vehicleId={}, period={}, from={}, to={}",
                vehicleId, period, startDate, endDate);

        List<GpsPoint> gpsPoints = loadGpsPoints(vehicleId, startDate, endDate);

        List<ReportEntry> reportEntries = switch (period) {
            case DAY -> calculateMileageByDay(gpsPoints, startDate, endDate);
            case MONTH -> calculateMileageByMonth(gpsPoints, startDate, endDate);
            case YEAR -> calculateMileageByYear(gpsPoints, startDate, endDate);
        };

        return ModelReport.builder()
                .reportName("Пробег автомобиля")
                .periodType(period)
                .startDate(startDate)
                .endDate(endDate)
                .mileage(sumMileage(reportEntries))
                .result(reportEntries)
                .build();
    }

    private List<GpsPoint> loadGpsPoints(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        List<GpsPoint> gpsPoints = gpsPointsRepository.findByVehicleAndTimeRange(
                vehicleId,
                startDate.atStartOfDay(UTC).toInstant(),
                endDate.atTime(23, 59, 59).atZone(UTC).toInstant()
        );

        log.info("Найдено {} GPS-точек для расчёта пробега", gpsPoints.size());
        return gpsPoints;
    }

    private List<ReportEntry> calculateMileageByDay(List<GpsPoint> gpsPoints, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Double> dailyMileageMap = new HashMap<>();

        for (int i = 1; i < gpsPoints.size(); i++) {
            GpsPoint point1 = gpsPoints.get(i - 1);
            GpsPoint point2 = gpsPoints.get(i);

            LocalDate day = point1.getTimestamp().atZone(UTC).toLocalDate();
            double distance = calculateDistanceBetweenPoints(point1, point2);

            dailyMileageMap.put(day, dailyMileageMap.getOrDefault(day, 0.0) + distance);
        }

        List<ReportEntry> dailyReport = new ArrayList<>();
        for (LocalDate currentDay = startDate; !currentDay.isAfter(endDate); currentDay = currentDay.plusDays(1)) {
            double mileage = roundMileage(dailyMileageMap.getOrDefault(currentDay, 0.0));
            dailyReport.add(new ReportEntry(currentDay.toString(), mileage));
        }

        return dailyReport;
    }

    private List<ReportEntry> calculateMileageByMonth(List<GpsPoint> gpsPoints, LocalDate startDate, LocalDate endDate) {
        Map<YearMonth, Double> monthlyMileageMap = new HashMap<>();

        for (int i = 1; i < gpsPoints.size(); i++) {
            GpsPoint point1 = gpsPoints.get(i - 1);
            GpsPoint point2 = gpsPoints.get(i);

            YearMonth month = YearMonth.from(point1.getTimestamp().atZone(UTC).toLocalDate());
            double distance = calculateDistanceBetweenPoints(point1, point2);

            monthlyMileageMap.put(month, monthlyMileageMap.getOrDefault(month, 0.0) + distance);
        }

        List<ReportEntry> monthlyReport = new ArrayList<>();
        for (YearMonth currentMonth = YearMonth.from(startDate);
             !currentMonth.isAfter(YearMonth.from(endDate));
             currentMonth = currentMonth.plusMonths(1)) {

            double mileage = roundMileage(monthlyMileageMap.getOrDefault(currentMonth, 0.0));
            monthlyReport.add(new ReportEntry(formatMonth(currentMonth), mileage));
        }

        return monthlyReport;
    }

    private List<ReportEntry> calculateMileageByYear(List<GpsPoint> gpsPoints, LocalDate startDate, LocalDate endDate) {
        Map<Integer, Double> yearlyMileageMap = new HashMap<>();

        for (int i = 1; i < gpsPoints.size(); i++) {
            GpsPoint point1 = gpsPoints.get(i - 1);
            GpsPoint point2 = gpsPoints.get(i);

            int year = point1.getTimestamp().atZone(UTC).getYear();
            double distance = calculateDistanceBetweenPoints(point1, point2);

            yearlyMileageMap.put(year, yearlyMileageMap.getOrDefault(year, 0.0) + distance);
        }

        List<ReportEntry> yearlyReport = new ArrayList<>();
        for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
            double mileage = roundMileage(yearlyMileageMap.getOrDefault(year, 0.0));
            yearlyReport.add(new ReportEntry(String.valueOf(year), mileage));
        }

        return yearlyReport;
    }

    private double calculateDistanceBetweenPoints(GpsPoint point1, GpsPoint point2) {
        return Haversine.calculateDistance(
                new GpsPointCoord(point1.getLocation().getY(), point1.getLocation().getX()),
                new GpsPointCoord(point2.getLocation().getY(), point2.getLocation().getX())
        );
    }

    private double sumMileage(List<ReportEntry> reportEntries) {
        return reportEntries.stream()
                .mapToDouble(ReportEntry::getValue)
                .sum();
    }

    private double roundMileage(double mileage) {
        return Math.round(mileage * 1000.0) / 1000.0;
    }

    private String formatMonth(YearMonth yearMonth) {
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, RU_LOCALE);
        return capitalizeFirstLetter(monthName) + " " + yearMonth.getYear();
    }

    private String capitalizeFirstLetter(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(RU_LOCALE) + value.substring(1);
    }
}