package org.example.autopark.exportAndImport.byGuid;

import lombok.RequiredArgsConstructor;
import org.example.autopark.exportAndImport.byGuid.ImportServiceByGuid;
import org.example.autopark.exportAndImport.byGuid.guidDto.TripGuidExportDto;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class CsvGuidImportUtil {

    private final ImportServiceByGuid importServiceByGuid;

    public void importFromCsvGuid(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        VehicleExportDtoByGuid dto = new VehicleExportDtoByGuid();
        List<TripGuidExportDto> trips = new ArrayList<>();

        boolean readingTrips = false;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            if (line.startsWith("Enterprise GUID")) {
                // читаем строку данных по предприятию + машине
                line = reader.readLine();
                String[] parts = line.split(";");
                dto.setEnterprise(new VehicleExportDtoByGuid.EnterpriseShortDTOByGuid(
                        UUID.fromString(parts[0]), // Enterprise GUID
                        parts[1],                  // Name
                        parts[2],                  // City
                        parts[3]                   // TimeZone
                ));
                dto.setVehicle(new VehicleExportDtoByGuid.VehicleShortDTOByGuid(
                        UUID.fromString(parts[4]), // Vehicle GUID
                        parts[5],                  // Vehicle Name
                        parts[6],                  // LicensePlate
                        Integer.parseInt(parts[7]),// Cost
                        Integer.parseInt(parts[8]),// Year
                        parts[9]                   // Brand
                ));
            } else if (line.startsWith("Trip GUID")) {
                readingTrips = true;
            } else if (readingTrips) {
                String[] parts = line.split(";");
                TripGuidExportDto trip = new TripGuidExportDto();
                trip.setGuid(UUID.fromString(parts[0]));
                trip.setStartTime(LocalDateTime.parse(parts[1]));
                trip.setEndTime(LocalDateTime.parse(parts[2]));
                trip.setStartLocationInString(parts[3]);
                trip.setEndLocationInString(parts[4]);
                trip.setDuration(parts[5]);
                trips.add(trip);
            }
        }

        dto.setTrips(trips);
        importServiceByGuid.importFromDtoByGuid(dto);
    }
}