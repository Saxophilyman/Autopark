package org.example.autopark.exportAndImport.ref.application.byGuid;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record ExportVehicleByGuidCommand(Long managerId, UUID vehicleGuid, LocalDate fromDate, LocalDate toDate, TrackMode trackMode) {
    public ExportVehicleByGuidCommand {
        Objects.requireNonNull(managerId, "managerId must not be null");
        Objects.requireNonNull(vehicleGuid, "vehicleGuid must not be null");
        Objects.requireNonNull(fromDate, "fromDate must not be null");
        Objects.requireNonNull(toDate, "toDate must not be null");
        Objects.requireNonNull(trackMode, "trackMode must not be null");

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Начальная дата не может быть позже конечной");
        }
    }
}
