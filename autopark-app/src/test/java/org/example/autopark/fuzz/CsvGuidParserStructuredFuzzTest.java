package org.example.autopark.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.autopark.exportAndImport.ref.document.byGuid.TripGuidExportDto;
import org.example.autopark.exportAndImport.ref.document.byGuid.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.ref.format.byGuid.CsvVehicleDtoByGuidParser;
import org.example.autopark.exportAndImport.ref.format.exception.CsvParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CsvGuidParserStructuredFuzzTest {

    private static final List<String> HEADER_MAIN = List.of(
            "Enterprise GUID",
            "Enterprise Name",
            "City",
            "TimeZone",
            "Vehicle GUID",
            "Vehicle Name",
            "LicensePlate",
            "Cost",
            "Year",
            "Brand"
    );

    private static final List<String> HEADER_TRIP = List.of(
            "Trip GUID",
            "Trip Start",
            "Trip End",
            "Start Location",
            "End Location",
            "Duration"
    );

    private static final List<String> SOME_TIMEZONES = List.of(
            "UTC",
            "Europe/Tallinn",
            "Europe/Oslo",
            "Europe/London",
            "America/New_York",
            "Asia/Tokyo"
    );

    private static final int MAX_TRIPS_IN_GENERATOR = 50;
    private static final int MAX_STR_LEN = 30;

    /*
     * Контракт structured fuzz-теста:
     *
     * VALID:
     *     документ обязан успешно разбираться.
     *
     * MAIN_MISSING_COLUMN / MAIN_DUPLICATE_COLUMN:
     *     парсер обязан выбросить CsvParseException.
     *
     * TRIP_MISSING_COLUMN / TRIP_DUPLICATE_COLUMN:
     *     разрешены два варианта:
     *     1. CsvParseException;
     *     2. успешный разбор с пустым списком trips.
     *
     * Остальные режимы:
     *     содержат заведомо некорректные значения и должны
     *     приводить к CsvParseException.
     */
    private enum Mode {
        VALID,

        MAIN_MISSING_COLUMN,
        MAIN_DUPLICATE_COLUMN,

        TRIP_MISSING_COLUMN,
        TRIP_DUPLICATE_COLUMN,

        INVALID_TIMEZONE,
        NEGATIVE_COST,
        YEAR_OUT_OF_RANGE,

        START_AFTER_END,
        DURATION_INVALID,
        DURATION_MISMATCH
    }

    private record CsvCase(String csv, Mode mode) {
    }

    @FuzzTest(maxDuration = "2m")
    void fuzzCsvGuidParserStructured(FuzzedDataProvider data) {
        CsvCase csvCase = buildCsv(data);

        byte[] bytes = csvCase.csv()
                .getBytes(StandardCharsets.UTF_8);

        try {
            VehicleExportDtoByGuid dto =
                    CsvVehicleDtoByGuidParser.parse(
                            new ByteArrayInputStream(bytes)
                    );

            /*
             * Для строго невалидных режимов успешный разбор
             * означает ошибку в парсере или неверный тестовый контракт.
             */
            if (mustFail(csvCase.mode())) {
                fail(
                        "Expected CsvParseException, but parsing succeeded."
                                + "\nMode: " + csvCase.mode()
                                + "\nCSV:\n" + csvCase.csv()
                );
            }

            assertDtoInvariants(dto);

            /*
             * Если повреждённый заголовок секции trips не вызвал
             * исключение, парсер не должен принимать строки этой
             * секции как корректные поездки.
             */
            if (hasMalformedTripHeader(csvCase.mode())) {
                assertTrue(
                        dto.getTrips().isEmpty(),
                        "Trips must be empty when trip header is malformed."
                                + "\nMode: " + csvCase.mode()
                                + "\nCSV:\n" + csvCase.csv()
                );
            }

        } catch (CsvParseException e) {
            /*
             * Валидный документ не имеет права завершаться
             * CsvParseException.
             */
            if (csvCase.mode() == Mode.VALID) {
                throw new AssertionError(
                        "Parser rejected a generated VALID CSV."
                                + "\nCSV:\n" + csvCase.csv(),
                        e
                );
            }

            /*
             * Для повреждённого trips-заголовка исключение допустимо.
             */
            if (hasMalformedTripHeader(csvCase.mode())) {
                return;
            }

            /*
             * Для остальных невалидных режимов CsvParseException
             * является ожидаемым результатом.
             */
            if (mustFail(csvCase.mode())) {
                return;
            }

            throw new AssertionError(
                    "Unexpected CsvParseException."
                            + "\nMode: " + csvCase.mode()
                            + "\nCSV:\n" + csvCase.csv(),
                    e
            );

        } catch (IOException e) {
            throw new AssertionError(
                    "Unexpected IOException for ByteArrayInputStream",
                    e
            );
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

    private static boolean hasMalformedTripHeader(Mode mode) {
        return mode == Mode.TRIP_MISSING_COLUMN
                || mode == Mode.TRIP_DUPLICATE_COLUMN;
    }

    private static CsvCase buildCsv(FuzzedDataProvider data) {
        Mode mode = pickMode(data);

        boolean withBom = data.consumeBoolean();

        List<String> mainHeader = shuffled(HEADER_MAIN, data);
        List<String> tripHeader = shuffled(HEADER_TRIP, data);

        Map<String, String> mainValues = new HashMap<>();

        mainValues.put("Enterprise GUID", uuid(data));
        mainValues.put("Enterprise Name", nonBlankCsvText(data));
        mainValues.put("City", nonBlankCsvText(data));
        mainValues.put("TimeZone", pickTimezone(data));

        mainValues.put("Vehicle GUID", uuid(data));
        mainValues.put("Vehicle Name", nonBlankCsvText(data));
        mainValues.put("LicensePlate", nonBlankCsvText(data));
        mainValues.put(
                "Cost",
                String.valueOf(data.consumeInt(0, 1_000_000))
        );
        mainValues.put(
                "Year",
                String.valueOf(data.consumeInt(1900, 2100))
        );
        mainValues.put("Brand", nonBlankCsvText(data));

        boolean modeNeedsTrips = switch (mode) {
            case TRIP_MISSING_COLUMN,
                 TRIP_DUPLICATE_COLUMN,
                 START_AFTER_END,
                 DURATION_INVALID,
                 DURATION_MISMATCH -> true;

            default -> false;
        };

        int tripCount = data.consumeInt(
                0,
                MAX_TRIPS_IN_GENERATOR
        );

        if (modeNeedsTrips && tripCount == 0) {
            tripCount = 1;
        }

        List<Map<String, String>> trips = new ArrayList<>();

        for (int i = 0; i < tripCount; i++) {
            trips.add(tripValues(data));
        }

        /*
         * Повреждение заголовков.
         *
         * Маркеры секций Enterprise GUID и Trip GUID сохраняются,
         * чтобы парсер мог определить тип секции.
         */
        switch (mode) {
            case MAIN_MISSING_COLUMN ->
                    removeRandomButKeepMarker(
                            data,
                            mainHeader,
                            "Enterprise GUID"
                    );

            case MAIN_DUPLICATE_COLUMN ->
                    duplicateRandom(data, mainHeader);

            case TRIP_MISSING_COLUMN ->
                    removeRandomButKeepMarker(
                            data,
                            tripHeader,
                            "Trip GUID"
                    );

            case TRIP_DUPLICATE_COLUMN ->
                    duplicateRandom(data, tripHeader);

            default -> {
                // Заголовок остаётся валидным.
            }
        }

        /*
         * Повреждение значений основной секции.
         */
        switch (mode) {
            case INVALID_TIMEZONE ->
                    mainValues.put("TimeZone", "Bad/Zone");

            case NEGATIVE_COST ->
                    mainValues.put("Cost", "-1");

            case YEAR_OUT_OF_RANGE ->
                    mainValues.put("Year", "2500");

            default -> {
                // Значения основной секции остаются валидными.
            }
        }

        /*
         * Повреждение первой поездки.
         */
        if (!trips.isEmpty()) {
            Map<String, String> firstTrip = trips.get(0);

            if (mode == Mode.START_AFTER_END) {
                LocalDateTime start = LocalDateTime.parse(
                        firstTrip.get("Trip Start")
                );

                firstTrip.put(
                        "Trip End",
                        start.minusSeconds(1).toString()
                );
            }

            if (mode == Mode.DURATION_INVALID) {
                firstTrip.put(
                        "Duration",
                        "NOT_A_DURATION"
                );
            }

            if (mode == Mode.DURATION_MISMATCH) {
                LocalDateTime start = LocalDateTime.parse(
                        firstTrip.get("Trip Start")
                );

                LocalDateTime end = LocalDateTime.parse(
                        firstTrip.get("Trip End")
                );

                long actualSeconds =
                        Duration.between(start, end).getSeconds();

                long wrongSeconds =
                        actualSeconds == 0
                                ? 1
                                : actualSeconds + 1;

                firstTrip.put(
                        "Duration",
                        Duration.ofSeconds(wrongSeconds).toString()
                );
            }
        }

        StringBuilder csv = new StringBuilder();

        csv.append(joinHeader(mainHeader, withBom))
                .append('\n');

        csv.append(joinRow(mainHeader, mainValues))
                .append('\n');

        csv.append('\n');

        csv.append(joinHeader(tripHeader, false))
                .append('\n');

        for (Map<String, String> trip : trips) {
            csv.append(joinRow(tripHeader, trip))
                    .append('\n');
        }

        return new CsvCase(csv.toString(), mode);
    }

    private static Mode pickMode(FuzzedDataProvider data) {
        int value = data.consumeInt(0, 99);

        if (value < 50) {
            return Mode.VALID;
        }

        if (value < 58) {
            return Mode.MAIN_MISSING_COLUMN;
        }

        if (value < 66) {
            return Mode.MAIN_DUPLICATE_COLUMN;
        }

        if (value < 74) {
            return Mode.TRIP_MISSING_COLUMN;
        }

        if (value < 82) {
            return Mode.TRIP_DUPLICATE_COLUMN;
        }

        if (value < 85) {
            return Mode.INVALID_TIMEZONE;
        }

        if (value < 88) {
            return Mode.NEGATIVE_COST;
        }

        if (value < 91) {
            return Mode.YEAR_OUT_OF_RANGE;
        }

        if (value < 94) {
            return Mode.START_AFTER_END;
        }

        if (value < 97) {
            return Mode.DURATION_INVALID;
        }

        return Mode.DURATION_MISMATCH;
    }

    private static void assertDtoInvariants(
            VehicleExportDtoByGuid dto
    ) {
        assertNotNull(dto);

        assertNotNull(dto.getEnterprise());
        assertNotNull(dto.getVehicle());
        assertNotNull(dto.getTrips());

        assertNotNull(dto.getEnterprise().getGuid());

        assertNotNull(dto.getEnterprise().getName());
        assertFalse(dto.getEnterprise().getName().isBlank());

        assertNotNull(dto.getEnterprise().getCity());
        assertFalse(dto.getEnterprise().getCity().isBlank());

        assertNotNull(dto.getEnterprise().getTimeZone());
        assertFalse(dto.getEnterprise().getTimeZone().isBlank());

        assertNotNull(dto.getVehicle().getGuid());

        assertNotNull(dto.getVehicle().getName());
        assertFalse(dto.getVehicle().getName().isBlank());

        assertNotNull(dto.getVehicle().getLicensePlate());
        assertFalse(dto.getVehicle().getLicensePlate().isBlank());

        assertNotNull(dto.getVehicle().getBrand());
        assertFalse(dto.getVehicle().getBrand().isBlank());

        assertTrue(
                dto.getVehicle().getCost() >= 0,
                "Vehicle cost must not be negative"
        );

        assertTrue(
                dto.getVehicle().getYearOfRelease() >= 1900
                        && dto.getVehicle().getYearOfRelease() <= 2100,
                "Vehicle year is outside the allowed range"
        );

        for (TripGuidExportDto trip : dto.getTrips()) {
            assertNotNull(trip);
            assertNotNull(trip.getGuid());
            assertNotNull(trip.getStartTime());
            assertNotNull(trip.getEndTime());

            assertFalse(
                    trip.getStartTime().isAfter(trip.getEndTime()),
                    "Trip start must not be after trip end"
            );

            assertNotNull(trip.getDuration());

            assertDoesNotThrow(
                    () -> Duration.parse(trip.getDuration()),
                    "Trip duration must use ISO-8601 format"
            );
        }
    }

    private static Map<String, String> tripValues(
            FuzzedDataProvider data
    ) {
        Map<String, String> values = new HashMap<>();

        values.put("Trip GUID", uuid(data));

        LocalDateTime start = randomDateTime(data);
        int plusSeconds = data.consumeInt(0, 10_000);
        LocalDateTime end = start.plusSeconds(plusSeconds);

        values.put("Trip Start", start.toString());
        values.put("Trip End", end.toString());

        values.put(
                "Start Location",
                nonBlankCsvText(data)
        );

        values.put(
                "End Location",
                nonBlankCsvText(data)
        );

        values.put(
                "Duration",
                Duration.ofSeconds(plusSeconds).toString()
        );

        return values;
    }

    private static List<String> shuffled(
            List<String> base,
            FuzzedDataProvider data
    ) {
        List<String> result = new ArrayList<>(base);

        Collections.shuffle(
                result,
                new Random(data.consumeLong())
        );

        return result;
    }

    private static void removeRandomButKeepMarker(
            FuzzedDataProvider data,
            List<String> header,
            String marker
    ) {
        if (header.size() <= 1) {
            return;
        }

        List<Integer> candidates = new ArrayList<>();

        for (int i = 0; i < header.size(); i++) {
            if (!header.get(i).equals(marker)) {
                candidates.add(i);
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        int candidateIndex = data.consumeInt(
                0,
                candidates.size() - 1
        );

        header.remove(candidates.get(candidateIndex));
    }

    private static void duplicateRandom(
            FuzzedDataProvider data,
            List<String> header
    ) {
        if (header.isEmpty()) {
            return;
        }

        int index = data.consumeInt(
                0,
                header.size() - 1
        );

        header.add(header.get(index));
    }

    private static String joinHeader(
            List<String> header,
            boolean withBom
    ) {
        if (!withBom || header.isEmpty()) {
            return String.join(";", header);
        }

        List<String> copy = new ArrayList<>(header);

        copy.set(
                0,
                "\uFEFF" + copy.get(0)
        );

        return String.join(";", copy);
    }

    private static String joinRow(
            List<String> header,
            Map<String, String> values
    ) {
        StringBuilder row = new StringBuilder();

        for (int i = 0; i < header.size(); i++) {
            if (i > 0) {
                row.append(';');
            }

            String column = header.get(i);
            String value = values.getOrDefault(column, "");

            row.append(safeCell(value));
        }

        return row.toString();
    }

    private static String uuid(FuzzedDataProvider data) {
        UUID uuid = new UUID(
                data.consumeLong(),
                data.consumeLong()
        );

        return uuid.toString();
    }

    private static String pickTimezone(
            FuzzedDataProvider data
    ) {
        int index = data.consumeInt(
                0,
                SOME_TIMEZONES.size() - 1
        );

        return SOME_TIMEZONES.get(index);
    }

    private static LocalDateTime randomDateTime(
            FuzzedDataProvider data
    ) {
        int year = data.consumeInt(2020, 2026);
        int month = data.consumeInt(1, 12);
        int day = data.consumeInt(1, 28);
        int hour = data.consumeInt(0, 23);
        int minute = data.consumeInt(0, 59);
        int second = data.consumeInt(0, 59);

        return LocalDateTime.of(
                year,
                month,
                day,
                hour,
                minute,
                second
        );
    }

    /**
     * Генерирует непустой текст, который гарантированно
     * не меняет структуру CSV.
     *
     * Разрешены:
     * - латинские буквы;
     * - цифры;
     * - пробел;
     * - дефис;
     * - подчёркивание.
     *
     * Кавычки, разделители, переводы строк и управляющие
     * символы сюда не попадают.
     */
    private static String nonBlankCsvText(
            FuzzedDataProvider data
    ) {
        String source = data.consumeString(MAX_STR_LEN);

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);

            if (Character.isLetterOrDigit(ch)) {
                result.append(ch);
                continue;
            }

            if (ch == ' ' || ch == '-' || ch == '_') {
                result.append(ch);
            }
        }

        String value = result.toString().trim();

        if (value.isEmpty()) {
            return "X";
        }

        return value;
    }

    private static String safeCell(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace(';', ' ')
                .replace('"', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ')
                .replace('\0', ' ');
    }
}