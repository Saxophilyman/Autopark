package org.example.autopark.fuzz;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.autopark.exportAndImport.CsvParseException;
import org.example.autopark.exportAndImport.byGuid.CsvVehicleDtoByGuidParser;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvGuidParserFuzzTest {

    private static final int MAX_INPUT_BYTES = 200_000;

    @FuzzTest(maxDuration = "2m")
    void fuzzCsvGuidParser(byte[] data) {
        if (data == null || data.length == 0) return;
        if (data.length > MAX_INPUT_BYTES) return;

        try {
            VehicleExportDtoByGuid dto =
                    CsvVehicleDtoByGuidParser.parse(new ByteArrayInputStream(data));

            assertNotNull(dto);
            assertNotNull(dto.getEnterprise());
            assertNotNull(dto.getVehicle());
            assertNotNull(dto.getTrips());

            // дешёвый инвариант: trips не может быть null и не должен быть "безумным"
            assertTrue(dto.getTrips().size() <= 10_000);

        } catch (CsvParseException expected) {
            // ок
        } catch (IOException e) {
            // неожиданный сценарий (для ByteArrayInputStream) — считаем багом
            throw new AssertionError("Unexpected IOException", e);
        }
    }
}
