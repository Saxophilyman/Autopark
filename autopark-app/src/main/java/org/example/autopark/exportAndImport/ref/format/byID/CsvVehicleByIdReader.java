package org.example.autopark.exportAndImport.ref.format.byID;

import org.example.autopark.exportAndImport.ref.document.byID.VehicleExportDtoById;
import org.example.autopark.exportAndImport.ref.format.exception.CsvParseException;

import org.example.autopark.trip.TripDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!reactive")
public class CsvVehicleByIdReader {

    public VehicleExportDtoById read(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            VehicleExportDtoById document = new VehicleExportDtoById();
            List<TripDTO> trips = new ArrayList<>();
            boolean readingTrips = false;
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = stripBom(line);

                if (line.isBlank()) {
                    continue;
                }

                if (line.startsWith("Enterprise ID")) {
                    String dataLine = reader.readLine();
                    lineNumber++;
                    if (dataLine == null || dataLine.isBlank()) {
                        throw new CsvParseException("CSV: отсутствуют данные предприятия и автомобиля после заголовка");
                    }
                    parseMainRow(document, stripBom(dataLine), lineNumber);
                    continue;
                }

                if (line.startsWith("Trip Start")) {
                    readingTrips = true;
                    continue;
                }

                if (readingTrips) {
                    trips.add(parseTripRow(line, lineNumber));
                }
            }

            if (document.getEnterprise() == null || document.getVehicle() == null) {
                throw new CsvParseException("CSV: не найдена секция предприятия и автомобиля");
            }

            document.setTrips(trips);
            return document;
        }
    }

    private void parseMainRow(VehicleExportDtoById document, String line, int lineNumber) {
        String[] parts = line.split(";", -1);
        if (parts.length < 10) {
            throw new CsvParseException("CSV: строка предприятия и автомобиля должна содержать 10 колонок, line " + lineNumber);
        }

        try {
            document.setEnterprise(new VehicleExportDtoById.EnterpriseShortDTO(
                    Long.parseLong(parts[0].strip()), parts[1].strip(), parts[2].strip(), parts[3].strip()));
            document.setVehicle(new VehicleExportDtoById.VehicleShortDTO(
                    Long.parseLong(parts[4].strip()), parts[5].strip(), parts[6].strip(),
                    Integer.parseInt(parts[7].strip()), Integer.parseInt(parts[8].strip()), parts[9].strip()));
        } catch (NumberFormatException exception) {
            throw new CsvParseException("CSV: некорректное числовое значение в основной секции, line " + lineNumber, exception);
        }
    }

    private TripDTO parseTripRow(String line, int lineNumber) {
        String[] parts = line.split(";", -1);
        if (parts.length < 5) {
            throw new CsvParseException("CSV: строка поездки должна содержать 5 колонок, line " + lineNumber);
        }

        try {
            TripDTO trip = new TripDTO();
            trip.setStartDate(LocalDateTime.parse(parts[0].strip()));
            trip.setEndDate(LocalDateTime.parse(parts[1].strip()));
            trip.setStartLocationInString(parts[2].strip());
            trip.setEndLocationInString(parts[3].strip());
            trip.setDuration(parts[4].strip());
            return trip;
        } catch (RuntimeException exception) {
            throw new CsvParseException("CSV: некорректная строка поездки, line " + lineNumber, exception);
        }
    }

    private String stripBom(String value) {
        return !value.isEmpty() && value.charAt(0) == '\uFEFF' ? value.substring(1) : value;
    }
}
