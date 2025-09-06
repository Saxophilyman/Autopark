package org.example.autopark.handTransactional;

import org.example.autopark.exportAndImport.ExportAndImportService;
import org.example.autopark.trip.TripRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class ExportAndImportServiceTest {

    @Autowired
    private ExportAndImportService exportAndImportService;

    @Autowired
    private TripRepository tripRepository;

    @Test
    void testImportFromCsvRollback() throws Exception {
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
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            exportAndImportService.importFromCsv(inputStream);
        });

        long countAfter = tripRepository.count();

        assertEquals(countBefore, countAfter, "Данные не должны были сохраниться после rollback");
    }
}
