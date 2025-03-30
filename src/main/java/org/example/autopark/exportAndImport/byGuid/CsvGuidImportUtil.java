package org.example.autopark.exportAndImport.byGuid;

import lombok.RequiredArgsConstructor;
import org.example.autopark.exportAndImport.byGuid.ImportServiceByGuid;
import org.example.autopark.exportAndImport.byGuid.guidDto.TripGuidExportDto;
import org.example.autopark.exportAndImport.byGuid.guidDto.VehicleExportDtoByGuid;
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
                line = reader.readLine();
                String[] parts = line.split(";");
                dto.setEnterprise(new VehicleExportDtoByGuid.EnterpriseShortDTOByGuid(
                        UUID.fromString(parts[0]), parts[1], parts[2], parts[3]
                ));
                dto.setVehicle(new VehicleExportDtoByGuid.VehicleShortDTOByGuid(
                        UUID.fromString(parts[3]), parts[4], Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6]), parts[7]
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
                trips.add(trip);
            }
        }

        dto.setTrips(trips);
        importServiceByGuid.importFromDtoByGuid(dto);
    }
}
