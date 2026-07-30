package org.example.autopark.exportAndImport.ref.application.byID;



import org.example.autopark.exportAndImport.ref.document.byID.VehicleExportDtoById;

import java.util.Objects;

public record ImportVehicleByIdCommand(Long managerId, VehicleExportDtoById document) {
    public ImportVehicleByIdCommand {
        Objects.requireNonNull(managerId, "managerId must not be null");
        Objects.requireNonNull(document, "document must not be null");
    }
}
