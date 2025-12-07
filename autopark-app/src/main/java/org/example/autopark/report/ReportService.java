package org.example.autopark.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.gps.GpsPoint;
import org.example.autopark.gps.GpsPointsRepository;
import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;
import org.example.autopark.repository.EnterpriseRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@Profile("!reactive")
@RequiredArgsConstructor
public class ReportService {
    private final GpsPointsRepository gpsPointsRepository;
    private final EnterpriseRepository enterpriseRepository;


    // Основной метод для генерации отчёта
    @Cacheable(
            value = "mileageReports",
            key = "#vehicleId + '_' + #startDate.toString() + '_' + #endDate.toString() + '_' + #period.name()"
    )
    public ModelReport generateMileageReport(Long vehicleId, LocalDate startDate, LocalDate endDate, PeriodType period) {
        // Получаем GPS-точки для автомобиля за указанный период
        log.info("Генерация отчёта: vehicleId={}, period={}, from={}, to={}", vehicleId, period, startDate, endDate);
        List<GpsPoint> gpsPoints = gpsPointsRepository.findByVehicleAndTimeRange(vehicleId,
                startDate.atStartOfDay(ZoneId.of("UTC")).toInstant(),
                endDate.atTime(23, 59, 59).atZone(ZoneId.of("UTC")).toInstant());
        log.info("Найдено {} GPS-точек для расчёта пробега", gpsPoints.size());
        List<ReportEntry> result = new ArrayList<>();
        double totalMileage = 0.0;

        switch (period) {
            case DAY:
                result = calculateMileageByDay(gpsPoints, startDate, endDate);
                totalMileage = result.stream().mapToDouble(ReportEntry::getValue).sum();
                break;
            case MONTH:
                result = calculateMileageByMonth(gpsPoints, startDate, endDate);
                totalMileage = result.stream().mapToDouble(ReportEntry::getValue).sum();
                break;
            case YEAR:
                result = calculateMileageByYear(gpsPoints, startDate, endDate);
                totalMileage = result.stream().mapToDouble(ReportEntry::getValue).sum();
                break;
        }

        // Формируем отчёт
        return ModelReport.builder()
                .reportName("Пробег автомобиля")
                .periodType(period)
//                .reportType(ReportType.VEHICLE_MILEAGE)
                .startDate(startDate)
                .endDate(endDate)
                .mileage(totalMileage)
                .result(result)
                .build();
    }

    private List<ReportEntry> calculateMileageByDay(List<GpsPoint> gpsPoints, LocalDate startDate, LocalDate endDate) {
        List<ReportEntry> dailyReport = new ArrayList<>();
        Map<LocalDate, Double> dailyMileageMap = new HashMap<>();

        // Проходим по GPS-точкам и вычисляем расстояния
        for (int i = 1; i < gpsPoints.size(); i++) {
            GpsPoint point1 = gpsPoints.get(i - 1);
            GpsPoint point2 = gpsPoints.get(i);

            double distance = Haversine.calculateDistance(
                    new GpsPointCoord(point1.getLocation().getY(), point1.getLocation().getX()),
                    new GpsPointCoord(point2.getLocation().getY(), point2.getLocation().getX())
            );

            LocalDate day = point1.getTimestamp().atZone(ZoneId.of("UTC")).toLocalDate();
            dailyMileageMap.put(day, dailyMileageMap.getOrDefault(day, 0.0) + distance);
        }

        // Проходим по всем дням от startDate до endDate и добавляем нулевой пробег для дней без поездок
        for (LocalDate currentDay = startDate; !currentDay.isAfter(endDate); currentDay = currentDay.plusDays(1)) {
            double mileage = dailyMileageMap.getOrDefault(currentDay, 0.0);

            // Округление до тысячных
            mileage = Math.round(mileage * 1000.0) / 1000.0;

            // Добавляем запись в отчёт
            dailyReport.add(new ReportEntry(currentDay.toString(), mileage));
        }

        return dailyReport;
    }


    private List<ReportEntry> calculateMileageByMonth(List<GpsPoint> gpsPoints, LocalDate startDate, LocalDate endDate) {
        List<ReportEntry> monthlyReport = new ArrayList<>();
        Map<String, Double> monthlyMileageMap = new HashMap<>();

        // Проходим по GPS-точкам и вычисляем расстояния
        for (int i = 1; i < gpsPoints.size(); i++) {
            GpsPoint point1 = gpsPoints.get(i - 1);
            GpsPoint point2 = gpsPoints.get(i);

            double distance = Haversine.calculateDistance(
                    new GpsPointCoord(point1.getLocation().getY(), point1.getLocation().getX()),
                    new GpsPointCoord(point2.getLocation().getY(), point2.getLocation().getX())
            );

            // Получаем месяц и год для каждой точки
            String month = point1.getTimestamp().atZone(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("ru", "RU")));

            // Заменяем родительный падеж на именительный
            month = fixMonthInNominative(month);

            // Суммируем пробег по месяцам
            monthlyMileageMap.put(month, monthlyMileageMap.getOrDefault(month, 0.0) + distance);
        }

        // Проходим по всем месяцам от startDate до endDate и добавляем нулевой пробег для месяцев без поездок
        for (LocalDate currentMonth = startDate.withDayOfMonth(1); !currentMonth.isAfter(endDate.withDayOfMonth(1)); currentMonth = currentMonth.plusMonths(1)) {
            // Формируем строку для месяца в формате "Март 2025" с заглавной буквы
            String month = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("ru", "RU")));

            // Заменяем родительный падеж на именительный
            month = fixMonthInNominative(month);

            // Получаем пробег для месяца или 0, если данных нет
            double mileage = monthlyMileageMap.getOrDefault(month, 0.0);

            // Округление до тысячных
            mileage = Math.round(mileage * 1000.0) / 1000.0;

            // Добавляем запись в отчёт
            monthlyReport.add(new ReportEntry(month, mileage));
        }

        return monthlyReport;
    }


    // Метод для исправления падежа месяца на именительный, сохраняя год
    private String fixMonthInNominative(String monthWithYear) {
        String[] parts = monthWithYear.split(" ");
        if (parts.length < 2) {
            // На всякий случай — если вдруг формат другой, возвращаем как есть
            return monthWithYear;
        }

        String rawMonth = parts[0]; // "марта"
        String year = parts[1];     // "2025"

        Map<String, String> monthCorrections = new HashMap<>();
        monthCorrections.put("января", "Январь");
        monthCorrections.put("февраля", "Февраль");
        monthCorrections.put("марта", "Март");
        monthCorrections.put("апреля", "Апрель");
        monthCorrections.put("мая", "Май");
        monthCorrections.put("июня", "Июнь");
        monthCorrections.put("июля", "Июль");
        monthCorrections.put("августа", "Август");
        monthCorrections.put("сентября", "Сентябрь");
        monthCorrections.put("октября", "Октябрь");
        monthCorrections.put("ноября", "Ноябрь");
        monthCorrections.put("декабря", "Декабрь");

        String correctedMonth = monthCorrections.getOrDefault(rawMonth, rawMonth);

        return correctedMonth + " " + year; // "Март 2025"
    }





    // Метод для расчёта пробега по годам
    private List<ReportEntry> calculateMileageByYear(List<GpsPoint> gpsPoints, LocalDate startDate, LocalDate endDate) {
        List<ReportEntry> yearlyReport = new ArrayList<>();
        Map<Integer, Double> yearlyMileageMap = new HashMap<>();

        // Проходим по GPS-точкам и вычисляем расстояния
        for (int i = 1; i < gpsPoints.size(); i++) {
            GpsPoint point1 = gpsPoints.get(i - 1);
            GpsPoint point2 = gpsPoints.get(i);

            double distance = Haversine.calculateDistance(
                    new GpsPointCoord(point1.getLocation().getY(), point1.getLocation().getX()),
                    new GpsPointCoord(point2.getLocation().getY(), point2.getLocation().getX())
            );

            int year = point1.getTimestamp().atZone(ZoneId.of("UTC")).getYear();

            yearlyMileageMap.put(year, yearlyMileageMap.getOrDefault(year, 0.0) + distance);
        }

        // Преобразуем данные в ReportEntry
        yearlyMileageMap.forEach((year, mileage) -> {
            yearlyReport.add(new ReportEntry(String.valueOf(year), mileage));
        });

        return yearlyReport;

    }
}
