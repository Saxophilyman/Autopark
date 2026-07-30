package org.example.autopark.exportAndImport.ref.format.byGuid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.example.autopark.exportAndImport.ref.document.byGuid.TripGuidExportDto;
import org.example.autopark.exportAndImport.ref.document.byGuid.VehicleExportDtoByGuid;
import org.example.autopark.exportAndImport.ref.format.DataFormat;
import org.example.autopark.exportAndImport.ref.format.exception.InvalidExchangeDocumentException;
import org.example.autopark.exportAndImport.ref.format.exception.UnsupportedExportCombinationException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Component
@Profile("!reactive")
@RequiredArgsConstructor
public class VehicleByGuidDocumentIO {
    private final ObjectMapper objectMapper;
    private final CsvVehicleByGuidReader csvReader;
    private final CsvVehicleByGuidWriter csvWriter;

    public VehicleExportDtoByGuid read(DataFormat format, InputStream input) throws IOException {
        try {
            return switch (format) {
                case JSON -> objectMapper.readValue(input, VehicleExportDtoByGuid.class);
                case CSV -> csvReader.read(input);
            };
        } catch (JsonProcessingException exception) {
            throw new InvalidExchangeDocumentException("Некорректный JSON-документ импорта по GUID", exception);
        }
    }

    public void write(DataFormat format, VehicleExportDtoByGuid document, OutputStream output) throws IOException {
        if (format == DataFormat.CSV && containsFullTrack(document)) {
            throw new UnsupportedExportCombinationException("Полный GPS-трек поддерживается только в JSON");
        }

        switch (format) {
            case JSON -> objectMapper.writeValue(output, document);
            case CSV -> csvWriter.write(document, output);
        }
    }

    private boolean containsFullTrack(VehicleExportDtoByGuid document) {
        List<TripGuidExportDto> trips = document.getTrips();
        return trips != null && trips.stream().anyMatch(trip -> trip.getGpsPoints() != null && !trip.getGpsPoints().isEmpty());
    }
}
