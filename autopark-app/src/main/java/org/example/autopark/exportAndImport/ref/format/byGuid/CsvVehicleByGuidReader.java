package org.example.autopark.exportAndImport.ref.format.byGuid;


import org.example.autopark.exportAndImport.ref.document.byGuid.VehicleExportDtoByGuid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@Profile("!reactive")
public class CsvVehicleByGuidReader {
    public VehicleExportDtoByGuid read(InputStream input) throws IOException {
        return CsvVehicleDtoByGuidParser.parse(input);
    }
}
