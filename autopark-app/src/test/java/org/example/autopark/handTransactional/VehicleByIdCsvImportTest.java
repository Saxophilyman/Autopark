package org.example.autopark.handTransactional;

import org.example.autopark.exportAndImport.ref.application.byID.ImportVehicleByIdCommand;
import org.example.autopark.exportAndImport.ref.application.byID.ImportVehicleByIdService;
import org.example.autopark.exportAndImport.ref.document.byID.VehicleExportDtoById;
import org.example.autopark.exportAndImport.ref.format.DataFormat;
import org.example.autopark.exportAndImport.ref.format.byID.VehicleByIdDocumentIO;
import org.example.autopark.exportAndImport.ref.format.exception.CsvParseException;
import org.example.autopark.trip.TripRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class VehicleByIdCsvImportTest {

    @Autowired
    private VehicleByIdDocumentIO documentIO;

    @Autowired
    private ImportVehicleByIdService importService;

    @Autowired
    private TripRepository tripRepository;

    @Test
    void malformedCsvDoesNotChangeDatabase() {
        long countBefore = tripRepository.count();

        String csv = """
                Enterprise ID;Enterprise Name;Address;Phone
                1;Test Enterprise;Address;1234567890
                Vehicle ID;License Plate;Model;Year;Capacity;VIN
                1;A001AA;ModelX;2021;4;VIN00001
                Trip Start;Trip End;Start Location;End Location
                2024-08-01T10:00;2024-08-01T12:00;CityA;CityB
                2024-08-02T11:00;WRONG_DATE;CityA;CityB
                """;

        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        assertThrows(CsvParseException.class, () -> {
            VehicleExportDtoById document = documentIO.read(DataFormat.CSV, input);
            importService.execute(new ImportVehicleByIdCommand(1L, document));
        });

        long countAfter = tripRepository.count();

        assertEquals(countBefore, countAfter, "Некорректный CSV не должен изменять данные");
    }
}