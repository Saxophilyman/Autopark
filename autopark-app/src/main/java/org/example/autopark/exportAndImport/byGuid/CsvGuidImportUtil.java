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
        VehicleExportDtoByGuid dto = CsvVehicleDtoByGuidParser.parse(inputStream);
        importServiceByGuid.importFromDtoByGuid(dto);
    }

}