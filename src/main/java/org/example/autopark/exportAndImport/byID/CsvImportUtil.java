package org.example.autopark.exportAndImport.byID;

import lombok.RequiredArgsConstructor;
import org.example.autopark.exportAndImport.byID.idDto.VehicleExportDtoById;
import org.example.autopark.trip.TripDTO;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvImportUtil {
    private final ImportServiceById importServiceById;

    public void importFromCsv(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        VehicleExportDtoById dto = new VehicleExportDtoById();
        List<TripDTO> trips = new ArrayList<>();

        boolean readingTrips = false;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            if (line.startsWith("Enterprise ID")) {
                // Пропускаем заголовок
                line = reader.readLine();
                String[] parts = line.split(";");
                dto.setEnterprise(new VehicleExportDtoById.EnterpriseShortDTO(
                        Long.parseLong(parts[0]), parts[1], parts[2], parts[3]
                ));
                dto.setVehicle(new VehicleExportDtoById.VehicleShortDTO(
                        Long.parseLong(parts[4]), parts[5], parts[6], Integer.parseInt(parts[7]),
                        Integer.parseInt(parts[8]), parts[9]
                ));
            } else if (line.startsWith("Trip Start")) {
                readingTrips = true;
            } else if (readingTrips) {
                String[] parts = line.split(";");
                TripDTO trip = new TripDTO();
                trip.setStartDate(LocalDateTime.parse(parts[0]));
                trip.setEndDate(LocalDateTime.parse(parts[1]));
                trip.setStartLocationInString(parts[2]);
                trip.setEndLocationInString(parts[3]);
                trips.add(trip);
            }
        }

        dto.setTrips(trips);
        importServiceById.importFromDtoById(dto);
    }
}
