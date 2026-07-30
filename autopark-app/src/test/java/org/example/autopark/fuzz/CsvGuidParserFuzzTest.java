package org.example.autopark.fuzz;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.autopark.exportAndImport.ref.document.byGuid.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.ref.format.byGuid.CsvVehicleDtoByGuidParser;
import org.example.autopark.exportAndImport.ref.format.exception.CsvParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CsvGuidParserFuzzTest {

    private static final int MAX_INPUT_BYTES = 200_000;
    private static final int MAX_TRIPS = 10_000;

    @FuzzTest(maxDuration = "2m")
    void fuzzCsvGuidParser(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        if (data.length > MAX_INPUT_BYTES) {
            return;
        }

        try {
            VehicleExportDtoByGuid dto =
                    CsvVehicleDtoByGuidParser.parse(
                            new ByteArrayInputStream(data)
                    );

            /*
             * Если парсер успешно принял документ,
             * возвращённый DTO должен быть внутренне корректным.
             */
            assertNotNull(dto);
            assertNotNull(dto.getEnterprise());
            assertNotNull(dto.getVehicle());
            assertNotNull(dto.getTrips());

            /*
             * Защита от чрезмерного выделения памяти:
             * успешно разобранный документ не должен создавать
             * неправдоподобно большую коллекцию поездок.
             */
            assertTrue(
                    dto.getTrips().size() <= MAX_TRIPS,
                    "Parser produced too many trips: " + dto.getTrips().size()
            );

        } catch (CsvParseException expected) {
            /*
             * Произвольный набор байтов чаще всего не является
             * корректным CSV-документом.
             *
             * CsvParseException — ожидаемая и безопасная реакция парсера.
             */
        } catch (IOException e) {
            /*
             * ByteArrayInputStream не должен выбрасывать IOException.
             * Такое исключение считаем неожиданным дефектом.
             */
            throw new AssertionError(
                    "Unexpected IOException while parsing in-memory CSV",
                    e
            );
        }
    }
}