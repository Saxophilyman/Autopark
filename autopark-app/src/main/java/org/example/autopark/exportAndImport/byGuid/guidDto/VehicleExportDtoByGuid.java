package org.example.autopark.exportAndImport.byGuid.guidDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;

import java.util.List;
import java.util.UUID;

@Data
public class VehicleExportDtoByGuid {
    private EnterpriseShortDTOByGuid enterprise;
    private VehicleShortDTOByGuid vehicle;
    private List<TripGuidExportDto> trips;

    public static VehicleExportDtoByGuid fromEntities(Vehicle vehicle, Enterprise enterprise, List<TripGuidExportDto> trips) {
        VehicleExportDtoByGuid dto = new VehicleExportDtoByGuid();

        dto.enterprise = new EnterpriseShortDTOByGuid(
                enterprise.getGuid(),
                enterprise.getName(),
                enterprise.getCityOfEnterprise(),
                enterprise.getTimeZone()
        );

        dto.vehicle = new VehicleShortDTOByGuid(
                vehicle.getGuid(),
                vehicle.getVehicleName(),
                vehicle.getLicensePlate(),
                vehicle.getVehicleCost(),
                vehicle.getVehicleYearOfRelease(),
                vehicle.getBrandOwner().getBrandName()
        );

        dto.trips = trips;
        return dto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnterpriseShortDTOByGuid {
        private UUID guid;
        private  String name;
        private  String city;
        private  String timeZone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleShortDTOByGuid {
        private UUID guid;
        private  String name;
        private String licensePlate;
        private  int cost;
        private  int yearOfRelease;
        private String brand;

    }

    //пока просто для наличия
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GpsPointGuidDto {
        private UUID guid;
        private double latitude;
        private double longitude;
//        private String address; // опционально, если сохраняешь
        private String timestamp;
    }
}
