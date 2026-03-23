package org.example.autopark.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.autopark.exportAndImport.CsvParseException;
import org.example.autopark.exportAndImport.byGuid.CsvVehicleDtoByGuidParser;
import org.example.autopark.exportAndImport.byGuid.guidDto.TripGuidExportDto;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CsvGuidParserStructuredFuzzTest {

    private static final List<String> HEADER_MAIN = List.of(
            "Enterprise GUID", "Enterprise Name", "City", "TimeZone",
            "Vehicle GUID", "Vehicle Name", "LicensePlate", "Cost", "Year", "Brand"
    );

    private static final List<String> HEADER_TRIP = List.of(
            "Trip GUID", "Trip Start", "Trip End", "Start Location", "End Location", "Duration"
    );

    private static final List<String> SOME_TIMEZONES = List.of(
            "UTC", "Europe/Tallinn", "Europe/Oslo", "Europe/London", "America/New_York", "Asia/Tokyo"
    );

    private static final int MAX_TRIPS_IN_GENERATOR = 50;
    private static final int MAX_STR_LEN = 30;

    /**
     * Важно: мы сознательно выбираем "мягкий" контракт:
     * - секция trips опциональна
     * - если заголовок trips битый (missing/duplicate), парсер имеет право:
     *   а) проигнорировать trips (dto.trips = empty), или
     *   б) упасть CsvParseException
     *
     * В structured fuzz мы проверяем, что:
     * - VALID всегда парсится
     * - "строго невалидные main/значения" всегда должны приводить к CsvParseException
     * - "битый trips header" допускает два исхода (см. выше)
     */
    private enum Mode {
        VALID,                  // валидный CSV, заголовки перемешаны
        MAIN_MISSING_COLUMN,    // main заголовок: не хватает колонки -> CsvParseException
        MAIN_DUPLICATE_COLUMN,  // main заголовок: дубликат -> CsvParseException

        TRIP_MISSING_COLUMN,    // trips заголовок: не хватает колонки -> допускаем ignore trips ИЛИ CsvParseException
        TRIP_DUPLICATE_COLUMN,  // trips заголовок: дубликат -> допускаем ignore trips ИЛИ CsvParseException

        INVALID_TIMEZONE,       // плохая TimeZone -> CsvParseException
        NEGATIVE_COST,          // cost < 0 -> CsvParseException
        YEAR_OUT_OF_RANGE,      // year вне диапазона -> CsvParseException
        START_AFTER_END,        // start > end -> CsvParseException (если trips секция распознана)
        DURATION_INVALID,       // duration не парсится -> CsvParseException (если trips секция распознана)
        DURATION_MISMATCH       // duration != end-start -> CsvParseException (если это инвариант парсера и trips распознана)
    }

    private record CsvCase(String csv, Mode mode) {}

    @FuzzTest(maxDuration = "2m")
    void fuzzCsvGuidParserStructured(FuzzedDataProvider data) {
        CsvCase csvCase = buildCsv(data);

        byte[] bytes = csvCase.csv.getBytes(StandardCharsets.UTF_8);

        try {
            VehicleExportDtoByGuid dto = CsvVehicleDtoByGuidParser.parse(new ByteArrayInputStream(bytes));

            // 1) Если mode строго "должен падать" — а мы успешно распарсили => finding
            if (mustFail(csvCase.mode)) {
                fail("Expected CsvParseException, but parsing succeeded. mode=" + csvCase.mode + "\nCSV:\n" + csvCase.csv);
            }

            // 2) Для успешных кейсов проверяем инварианты DTO
            assertDtoInvariants(dto);

            // 3) Специальная проверка для битого trips header:
            // если парсер НЕ упал, то trips должны быть пустыми (секция trips не распознана как валидная)
            if (csvCase.mode == Mode.TRIP_MISSING_COLUMN || csvCase.mode == Mode.TRIP_DUPLICATE_COLUMN) {
                assertTrue(dto.getTrips().isEmpty(),
                        "Trips must be empty when trip header is malformed (if parsing succeeds). mode=" + csvCase.mode
                                + "\nCSV:\n" + csvCase.csv);
            }

        } catch (CsvParseException e) {
            // Если mode валидный — падать нельзя
            if (csvCase.mode == Mode.VALID) {
                throw e;
            }

            // Если mode относится к "битому trips header" — падение допустимо
            if (csvCase.mode == Mode.TRIP_MISSING_COLUMN || csvCase.mode == Mode.TRIP_DUPLICATE_COLUMN) {
                return;
            }

            // Для остальных режимов падение и ожидалось (ок)
        } catch (IOException e) {
            throw new AssertionError("Unexpected IOException for ByteArrayInputStream", e);
        }
    }

    private static boolean mustFail(Mode mode) {
        return switch (mode) {
            case VALID,
                 TRIP_MISSING_COLUMN,
                 TRIP_DUPLICATE_COLUMN -> false;
            default -> true;
        };
    }

    private static CsvCase buildCsv(FuzzedDataProvider data) {
        Mode mode = pickMode(data);

        boolean withBom = data.consumeBoolean();

        List<String> mainHeader = shuffled(HEADER_MAIN, data);
        List<String> tripHeader = shuffled(HEADER_TRIP, data);

        Map<String, String> mainValues = new HashMap<>();
        mainValues.put("Enterprise GUID", uuid(data));
        mainValues.put("Enterprise Name", nonBlankStr(data));
        mainValues.put("City", nonBlankStr(data));
        mainValues.put("TimeZone", pickTimezone(data));

        mainValues.put("Vehicle GUID", uuid(data));
        mainValues.put("Vehicle Name", nonBlankStr(data));
        mainValues.put("LicensePlate", nonBlankStr(data));
        mainValues.put("Cost", String.valueOf(data.consumeInt(0, 1_000_000)));
        mainValues.put("Year", String.valueOf(data.consumeInt(1900, 2100)));
        mainValues.put("Brand", nonBlankStr(data));

        boolean modeNeedsTrips = switch (mode) {
            case TRIP_MISSING_COLUMN, TRIP_DUPLICATE_COLUMN, START_AFTER_END, DURATION_INVALID, DURATION_MISMATCH -> true;
            default -> false;
        };

        int tripCount = data.consumeInt(0, MAX_TRIPS_IN_GENERATOR);
        if (modeNeedsTrips && tripCount == 0) tripCount = 1;

        List<Map<String, String>> trips = new ArrayList<>();
        for (int i = 0; i < tripCount; i++) {
            trips.add(tripValues(data));
        }

        // Ломаем заголовки, но не удаляем маркеры "Enterprise GUID"/"Trip GUID", чтобы парсер мог "увидеть" секцию.
        switch (mode) {
            case MAIN_MISSING_COLUMN -> removeRandomButKeepMarker(data, mainHeader, "Enterprise GUID");
            case MAIN_DUPLICATE_COLUMN -> duplicateRandom(data, mainHeader);

            case TRIP_MISSING_COLUMN -> removeRandomButKeepMarker(data, tripHeader, "Trip GUID");
            case TRIP_DUPLICATE_COLUMN -> duplicateRandom(data, tripHeader);

            default -> { }
        }

        // Ломаем значения main
        switch (mode) {
            case INVALID_TIMEZONE -> mainValues.put("TimeZone", "Bad/Zone");
            case NEGATIVE_COST -> mainValues.put("Cost", "-1");
            case YEAR_OUT_OF_RANGE -> mainValues.put("Year", "2500");
            default -> { }
        }

        // Ломаем значения trips (в первой поездке)
        if (!trips.isEmpty()) {
            Map<String, String> t0 = trips.get(0);

            if (mode == Mode.START_AFTER_END) {
                LocalDateTime start = LocalDateTime.parse(t0.get("Trip Start"));
                t0.put("Trip End", start.minusSeconds(1).toString());
                // duration пусть останется прежней — это усилит шанс, что парсер заметит несогласованность
            }

            if (mode == Mode.DURATION_INVALID) {
                t0.put("Duration", "NOT_A_DURATION");
            }

            if (mode == Mode.DURATION_MISMATCH) {
                LocalDateTime start = LocalDateTime.parse(t0.get("Trip Start"));
                LocalDateTime end = LocalDateTime.parse(t0.get("Trip End"));
                long sec = Duration.between(start, end).getSeconds();
                long wrong = (sec == 0) ? 1 : sec + 1;
                t0.put("Duration", Duration.ofSeconds(wrong).toString());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(joinHeader(mainHeader, withBom)).append('\n');
        sb.append(joinRow(mainHeader, mainValues)).append('\n');
        sb.append('\n');
        sb.append(joinHeader(tripHeader, false)).append('\n');
        for (Map<String, String> tv : trips) {
            sb.append(joinRow(tripHeader, tv)).append('\n');
        }

        return new CsvCase(sb.toString(), mode);
    }

    private static Mode pickMode(FuzzedDataProvider data) {
        int x = data.consumeInt(0, 99);
        if (x < 50) return Mode.VALID;

        if (x < 58) return Mode.MAIN_MISSING_COLUMN;
        if (x < 66) return Mode.MAIN_DUPLICATE_COLUMN;
        if (x < 74) return Mode.TRIP_MISSING_COLUMN;
        if (x < 82) return Mode.TRIP_DUPLICATE_COLUMN;

        if (x < 85) return Mode.INVALID_TIMEZONE;
        if (x < 88) return Mode.NEGATIVE_COST;
        if (x < 91) return Mode.YEAR_OUT_OF_RANGE;
        if (x < 94) return Mode.START_AFTER_END;
        if (x < 97) return Mode.DURATION_INVALID;
        return Mode.DURATION_MISMATCH;
    }

    // ---------- Оракул для успешных кейсов ----------

    private static void assertDtoInvariants(VehicleExportDtoByGuid dto) {
        assertNotNull(dto);
        assertNotNull(dto.getEnterprise());
        assertNotNull(dto.getVehicle());
        assertNotNull(dto.getTrips());

        assertNotNull(dto.getEnterprise().getGuid());
        assertFalse(dto.getEnterprise().getName().isBlank());
        assertFalse(dto.getEnterprise().getCity().isBlank());
        assertFalse(dto.getEnterprise().getTimeZone().isBlank());

        assertNotNull(dto.getVehicle().getGuid());
        assertFalse(dto.getVehicle().getName().isBlank());
        assertFalse(dto.getVehicle().getLicensePlate().isBlank());
        assertFalse(dto.getVehicle().getBrand().isBlank());
        assertTrue(dto.getVehicle().getCost() >= 0);
        assertTrue(dto.getVehicle().getYearOfRelease() >= 1900 && dto.getVehicle().getYearOfRelease() <= 2100);

        for (TripGuidExportDto t : dto.getTrips()) {
            assertNotNull(t.getGuid());
            assertNotNull(t.getStartTime());
            assertNotNull(t.getEndTime());
            assertFalse(t.getStartTime().isAfter(t.getEndTime()));
            assertNotNull(t.getDuration());
            assertDoesNotThrow(() -> Duration.parse(t.getDuration()));
        }
    }

    // ---------- Генераторы trips ----------

    private static Map<String, String> tripValues(FuzzedDataProvider data) {
        Map<String, String> v = new HashMap<>();
        v.put("Trip GUID", uuid(data));

        LocalDateTime start = randomDateTime(data);
        int plusSec = data.consumeInt(0, 10_000);
        LocalDateTime end = start.plusSeconds(plusSec);

        v.put("Trip Start", start.toString());
        v.put("Trip End", end.toString());
        v.put("Start Location", nonBlankStr(data));
        v.put("End Location", nonBlankStr(data));
        v.put("Duration", Duration.ofSeconds(plusSec).toString());
        return v;
    }

    // ---------- helpers ----------

    private static List<String> shuffled(List<String> base, FuzzedDataProvider data) {
        List<String> h = new ArrayList<>(base);
        Collections.shuffle(h, new Random(data.consumeLong()));
        return h;
    }

    private static void removeRandomButKeepMarker(FuzzedDataProvider data, List<String> header, String marker) {
        if (header.size() <= 1) return;

        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < header.size(); i++) {
            if (!header.get(i).equals(marker)) candidates.add(i);
        }
        if (candidates.isEmpty()) return;

        int idx = candidates.get(data.consumeInt(0, candidates.size() - 1));
        header.remove(idx);
    }

    private static void duplicateRandom(FuzzedDataProvider data, List<String> header) {
        if (header.isEmpty()) return;
        int idx = data.consumeInt(0, header.size() - 1);
        header.add(header.get(idx));
    }

    private static String joinHeader(List<String> header, boolean withBom) {
        if (!withBom || header.isEmpty()) return String.join(";", header);

        List<String> copy = new ArrayList<>(header);
        copy.set(0, "\uFEFF" + copy.get(0));
        return String.join(";", copy);
    }

    private static String joinRow(List<String> header, Map<String, String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < header.size(); i++) {
            if (i > 0) sb.append(';');
            String col = header.get(i);
            sb.append(safeCell(values.getOrDefault(col, "")));
        }
        return sb.toString();
    }

    private static String uuid(FuzzedDataProvider data) {
        UUID u = new UUID(data.consumeLong(), data.consumeLong());
        return u.toString();
    }

    private static String pickTimezone(FuzzedDataProvider data) {
        return SOME_TIMEZONES.get(data.consumeInt(0, SOME_TIMEZONES.size() - 1));
    }

    private static LocalDateTime randomDateTime(FuzzedDataProvider data) {
        int year = data.consumeInt(2020, 2026);
        int month = data.consumeInt(1, 12);
        int day = data.consumeInt(1, 28);
        int hour = data.consumeInt(0, 23);
        int min = data.consumeInt(0, 59);
        int sec = data.consumeInt(0, 59);
        return LocalDateTime.of(year, month, day, hour, min, sec);
    }

    private static String nonBlankStr(FuzzedDataProvider data) {
        String s = data.consumeString(MAX_STR_LEN);
        s = safeCell(s).trim();
        return s.isEmpty() ? "X" : s;
    }

    private static String safeCell(String s) {
        if (s == null) return "";
        return s.replace(';', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ');
    }
}
