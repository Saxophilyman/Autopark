package org.example.autopark.exportAndImport.byGuid.guidDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripGuidExportDto {
    private UUID guid;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String startLocationInString;
    private String endLocationInString;
    private String duration;
    private List<VehicleExportDtoByGuid.GpsPointGuidDto> gpsPoints;

}
