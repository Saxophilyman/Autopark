package org.example.autopark.exportAndImport.ref.application.byID;

import java.time.LocalDate;
import java.util.Objects;

public record ExportVehicleByIdCommand(Long managerId, Long vehicleId, LocalDate fromDate, LocalDate toDate) {
    public ExportVehicleByIdCommand {
        Objects.requireNonNull(managerId, "managerId must not be null");
        Objects.requireNonNull(vehicleId, "vehicleId must not be null");
        Objects.requireNonNull(fromDate, "fromDate must not be null");
        Objects.requireNonNull(toDate, "toDate must not be null");

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Начальная дата не может быть позже конечной");
        }
    }
}
