package org.example.autopark.exportAndImport.byGuid;

import lombok.experimental.UtilityClass;
import org.example.autopark.exportAndImport.CsvParseException;
import org.example.autopark.exportAndImport.byGuid.guidDto.TripGuidExportDto;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@UtilityClass
public class CsvVehicleDtoByGuidParser {

    private static final int MAX_TRIPS = 10_000;

    private static final List<String> HEADER_MAIN = List.of(
            "Enterprise GUID", "Enterprise Name", "City", "TimeZone",
            "Vehicle GUID", "Vehicle Name", "LicensePlate", "Cost", "Year", "Brand"
    );

    private static final List<String> HEADER_TRIP = List.of(
            "Trip GUID", "Trip Start", "Trip End", "Start Location", "End Location", "Duration"
    );

    public VehicleExportDtoByGuid parse(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            List<Line> lines = readUsefulLines(reader);
            if (lines.isEmpty()) {
                throw new CsvParseException("CSV: файл пуст или не содержит полезных строк");
            }

            ParsedHeader header = parseMainSection(lines);
            List<TripGuidExportDto> trips = parseTripSection(lines, header.nextIndex());

            VehicleExportDtoByGuid dto = new VehicleExportDtoByGuid();
            dto.setEnterprise(header.enterprise());
            dto.setVehicle(header.vehicle());
            dto.setTrips(trips);
            return dto;
        }
    }

    // ---------- ВСПОМОГАТЕЛЬНЫЕ ТИПЫ ----------

    private record Line(int no, String text) {}

    private record ParsedHeader(
            VehicleExportDtoByGuid.EnterpriseShortDTOByGuid enterprise,
            VehicleExportDtoByGuid.VehicleShortDTOByGuid vehicle,
            int nextIndex
    ) {}

    private enum Section { ENTERPRISE, TRIP, UNKNOWN }

    // ---------- ЧТЕНИЕ ----------

    private List<Line> readUsefulLines(BufferedReader reader) throws IOException {
        List<Line> result = new ArrayList<>();
        String raw;
        int lineNo = 0;

        while ((raw = reader.readLine()) != null) {
            lineNo++;

            String line = stripBom(raw);

            // ВАЖНО: isBlank() учитывает Unicode whitespace
            if (line == null || line.isBlank()) continue;

            result.add(new Line(lineNo, line));
        }
        return result;
    }

    // ---------- MAIN: Enterprise + Vehicle ----------

    private ParsedHeader parseMainSection(List<Line> lines) {
        int headerIndex = findSectionHeader(lines, 0, "Enterprise GUID");
        if (headerIndex < 0) {
            throw new CsvParseException("CSV: не найдена секция Enterprise/Vehicle (ожидался заголовок с колонками: " + HEADER_MAIN + ")");
        }
        if (headerIndex + 1 >= lines.size()) {
            throw new CsvParseException("CSV: после заголовка Enterprise GUID нет строки данных (line "
                    + lines.get(headerIndex).no() + ")");
        }

        Line headerLine = lines.get(headerIndex);
        Map<String, Integer> idx = parseHeaderIndexes(headerLine, HEADER_MAIN);

        Line dataLine = lines.get(headerIndex + 1);
        String[] parts = split(dataLine);

        UUID enterpriseGuid = uuid(cell(parts, idx, "Enterprise GUID", dataLine.no()), dataLine.no(), "Enterprise GUID");
        String enterpriseName = nonBlank(cell(parts, idx, "Enterprise Name", dataLine.no()), dataLine.no(), "Enterprise Name");
        String city = nonBlank(cell(parts, idx, "City", dataLine.no()), dataLine.no(), "City");
        String timeZone = timeZone(cell(parts, idx, "TimeZone", dataLine.no()), dataLine.no(), "TimeZone");

        UUID vehicleGuid = uuid(cell(parts, idx, "Vehicle GUID", dataLine.no()), dataLine.no(), "Vehicle GUID");
        String vehicleName = nonBlank(cell(parts, idx, "Vehicle Name", dataLine.no()), dataLine.no(), "Vehicle Name");
        String licensePlate = nonBlank(cell(parts, idx, "LicensePlate", dataLine.no()), dataLine.no(), "LicensePlate");
        int cost = nonNegativeInt(cell(parts, idx, "Cost", dataLine.no()), dataLine.no(), "Cost");
        int year = intInRange(cell(parts, idx, "Year", dataLine.no()), dataLine.no(), "Year", 1900, 2100);
        String brand = nonBlank(cell(parts, idx, "Brand", dataLine.no()), dataLine.no(), "Brand");

        VehicleExportDtoByGuid.EnterpriseShortDTOByGuid enterprise =
                new VehicleExportDtoByGuid.EnterpriseShortDTOByGuid(enterpriseGuid, enterpriseName, city, timeZone);

        VehicleExportDtoByGuid.VehicleShortDTOByGuid vehicle =
                new VehicleExportDtoByGuid.VehicleShortDTOByGuid(vehicleGuid, vehicleName, licensePlate, cost, year, brand);

        return new ParsedHeader(enterprise, vehicle, headerIndex + 2);
    }

    // ---------- Trips ----------

    private List<TripGuidExportDto> parseTripSection(List<Line> lines, int fromIndex) {
        int tripHeaderIndex = findSectionHeader(lines, fromIndex, "Trip GUID");
        if (tripHeaderIndex < 0) return List.of();

        Line headerLine = lines.get(tripHeaderIndex);
        Map<String, Integer> idx = parseHeaderIndexes(headerLine, HEADER_TRIP);

        List<TripGuidExportDto> trips = new ArrayList<>();

        for (int i = tripHeaderIndex + 1; i < lines.size(); i++) {
            Line line = lines.get(i);

            Section sec = detectSection(line);
            if (sec == Section.ENTERPRISE) break;
            if (sec == Section.TRIP) continue;

            if (trips.size() >= MAX_TRIPS) {
                throw new CsvParseException("CSV: слишком много поездок (>" + MAX_TRIPS + "), остановка на line " + line.no());
            }

            String[] parts = split(line);

            UUID tripGuid = uuid(cell(parts, idx, "Trip GUID", line.no()), line.no(), "Trip GUID");
            LocalDateTime start = dateTime(cell(parts, idx, "Trip Start", line.no()), line.no(), "Trip Start");
            LocalDateTime end = dateTime(cell(parts, idx, "Trip End", line.no()), line.no(), "Trip End");

            if (start.isAfter(end)) {
                throw new CsvParseException("CSV: Trip Start позже Trip End (line " + line.no() + "): " + start + " > " + end);
            }

            String startLoc = nonBlank(cell(parts, idx, "Start Location", line.no()), line.no(), "Start Location");
            String endLoc = nonBlank(cell(parts, idx, "End Location", line.no()), line.no(), "End Location");

            Duration duration = durationIso(cell(parts, idx, "Duration", line.no()), line.no(), "Duration");

            // Инвариант: Duration >= 0 и совпадает с (end-start)
            Duration expected = Duration.between(start, end);
            if (duration.isNegative()) {
                throw new CsvParseException("CSV: Duration отрицательная (line " + line.no() + "): " + duration);
            }
            if (!duration.equals(expected)) {
                throw new CsvParseException("CSV: Duration не совпадает с (Trip End - Trip Start) (line " + line.no()
                        + "): duration=" + duration + ", expected=" + expected);
            }

            TripGuidExportDto trip = new TripGuidExportDto();
            trip.setGuid(tripGuid);
            trip.setStartTime(start);
            trip.setEndTime(end);
            trip.setStartLocationInString(startLoc);
            trip.setEndLocationInString(endLoc);
            trip.setDuration(duration.toString());

            trips.add(trip);
        }

        return trips;
    }

    // ---------- Заголовки / секции ----------

    private int findSectionHeader(List<Line> lines, int from, String markerCell) {
        for (int i = from; i < lines.size(); i++) {
            Line l = lines.get(i);
            if (containsCell(l, markerCell)) return i;
        }
        return -1;
    }

    private boolean containsCell(Line line, String expected) {
        String[] cols = split(line);
        for (String c : cols) {
            String normalized = normalizeHeaderCell(c);
            if (normalized.equals(expected)) return true;
        }
        return false;
    }

    private Section detectSection(Line line) {
        if (containsCell(line, "Enterprise GUID")) return Section.ENTERPRISE;
        if (containsCell(line, "Trip GUID")) return Section.TRIP;
        return Section.UNKNOWN;
    }

    private Map<String, Integer> parseHeaderIndexes(Line headerLine, List<String> expectedColumns) {
        String[] cols = split(headerLine);

        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < cols.length; i++) {
            String col = normalizeHeaderCell(cols[i]);

            Integer prev = idx.put(col, i);
            if (prev != null) {
                throw new CsvParseException("CSV: дублирующаяся колонка '" + col
                        + "' в заголовке (line " + headerLine.no() + "). Заголовок: " + headerLine.text());
            }
        }

        for (String col : expectedColumns) {
            if (!idx.containsKey(col)) {
                throw new CsvParseException("CSV: в заголовке (line " + headerLine.no()
                        + ") отсутствует колонка '" + col + "'. Заголовок: " + headerLine.text());
            }
        }
        return idx;
    }

    private String normalizeHeaderCell(String raw) {
        if (raw == null) return "";
        // strip() вместо trim(): Unicode whitespace
        return stripBom(raw).strip();
    }

    private String cell(String[] parts, Map<String, Integer> idx, String col, int lineNo) {
        Integer i = idx.get(col);
        if (i == null) {
            throw new CsvParseException("CSV: внутренняя ошибка — не найдена колонка '" + col + "' в карте индексов (line " + lineNo + ")");
        }
        if (i >= parts.length) {
            throw new CsvParseException("CSV: строка короче заголовка, нет колонки '" + col + "' (line " + lineNo + ")");
        }
        return parts[i];
    }

    private String[] split(Line line) {
        return line.text().split(";", -1);
    }

    // ---------- Примитивные парсеры + валидация ----------

    private String stripBom(String s) {
        if (s == null || s.isEmpty()) return s;
        return (s.charAt(0) == '\uFEFF') ? s.substring(1) : s;
    }

    private String nonBlank(String raw, int lineNo, String field) {
        // strip() + isBlank(): корректно для Unicode whitespace
        String v = (raw == null) ? "" : raw.strip();
        if (v.isBlank()) {
            throw new CsvParseException("CSV: пустое значение поля " + field + " (line " + lineNo + ")");
        }
        return v;
    }

    private UUID uuid(String raw, int lineNo, String field) {
        try {
            return UUID.fromString(nonBlank(raw, lineNo, field));
        } catch (Exception e) {
            throw new CsvParseException("CSV: невалидный UUID в поле " + field + " (line " + lineNo + "): " + raw, e);
        }
    }

    private int nonNegativeInt(String raw, int lineNo, String field) {
        int v = intValue(raw, lineNo, field);
        if (v < 0) {
            throw new CsvParseException("CSV: поле " + field + " < 0 (line " + lineNo + "): " + v);
        }
        return v;
    }

    private int intInRange(String raw, int lineNo, String field, int min, int max) {
        int v = intValue(raw, lineNo, field);
        if (v < min || v > max) {
            throw new CsvParseException("CSV: поле " + field + " вне диапазона [" + min + "," + max + "] (line "
                    + lineNo + "): " + v);
        }
        return v;
    }

    private int intValue(String raw, int lineNo, String field) {
        try {
            return Integer.parseInt(nonBlank(raw, lineNo, field));
        } catch (Exception e) {
            throw new CsvParseException("CSV: невалидное число в поле " + field + " (line " + lineNo + "): " + raw, e);
        }
    }

    private LocalDateTime dateTime(String raw, int lineNo, String field) {
        try {
            return LocalDateTime.parse(nonBlank(raw, lineNo, field));
        } catch (Exception e) {
            throw new CsvParseException("CSV: невалидная дата-время в поле " + field + " (line " + lineNo + "): " + raw, e);
        }
    }

    private String timeZone(String raw, int lineNo, String field) {
        String v = nonBlank(raw, lineNo, field);
        try {
            ZoneId.of(v);
            return v;
        } catch (Exception e) {
            throw new CsvParseException("CSV: невалидная TimeZone в поле " + field + " (line " + lineNo + "): " + raw, e);
        }
    }

    private Duration durationIso(String raw, int lineNo, String field) {
        String v = nonBlank(raw, lineNo, field);
        try {
            return Duration.parse(v);
        } catch (Exception e) {
            throw new CsvParseException("CSV: невалидная Duration (ISO-8601) в поле " + field + " (line " + lineNo + "): " + raw, e);
        }
    }
}
