package org.example.autopark.exportAndImport.ref.application.byGuid;



import org.example.autopark.exportAndImport.ref.document.byGuid.VehicleExportDtoByGuid;

import java.util.Objects;

public record ImportVehicleByGuidCommand(Long managerId, VehicleExportDtoByGuid document) {
    public ImportVehicleByGuidCommand {
        Objects.requireNonNull(managerId, "managerId must not be null");
        Objects.requireNonNull(document, "document must not be null");
    }
}
