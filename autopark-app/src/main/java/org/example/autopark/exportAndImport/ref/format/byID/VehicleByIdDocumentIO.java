package org.example.autopark.exportAndImport.ref.format.byID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.example.autopark.exportAndImport.ref.document.byID.VehicleExportDtoById;
import org.example.autopark.exportAndImport.ref.format.DataFormat;
import org.example.autopark.exportAndImport.ref.format.exception.InvalidExchangeDocumentException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
@Profile("!reactive")
@RequiredArgsConstructor
public class VehicleByIdDocumentIO {
    private final ObjectMapper objectMapper;
    private final CsvVehicleByIdReader csvReader;
    private final CsvVehicleByIdWriter csvWriter;

    public VehicleExportDtoById read(DataFormat format, InputStream input) throws IOException {
        try {
            return switch (format) {
                case JSON -> objectMapper.readValue(input, VehicleExportDtoById.class);
                case CSV -> csvReader.read(input);
            };
        } catch (JsonProcessingException exception) {
            throw new InvalidExchangeDocumentException("Некорректный JSON-документ импорта по ID", exception);
        }
    }

    public void write(DataFormat format, VehicleExportDtoById document, OutputStream output) throws IOException {
        switch (format) {
            case JSON -> objectMapper.writeValue(output, document);
            case CSV -> csvWriter.write(document, output);
        }
    }
}
