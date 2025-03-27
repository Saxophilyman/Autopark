package org.example.autopark.exportAndImport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.trip.TripDTO;

import java.time.Instant;
import java.util.List;

@Data
public class VehicleExportDto {
    private EnterpriseShortDTO enterprise;
    private VehicleShortDTO vehicle;
    private List<TripDTO> trips;

    public static VehicleExportDto fromEntities(Vehicle vehicle, Enterprise enterprise, List<TripDTO> trips) {
        VehicleExportDto dto = new VehicleExportDto();

        dto.enterprise = new EnterpriseShortDTO(
                enterprise.getEnterpriseId(),
                enterprise.getName(),
                enterprise.getCityOfEnterprise(),
                enterprise.getTimeZone()
        );

        dto.vehicle = new VehicleShortDTO(
                vehicle.getVehicleId(),
                vehicle.getVehicleName(),
                vehicle.getVehicleCost(),
                vehicle.getVehicleYearOfRelease()
        );

        dto.trips = trips;
        return dto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnterpriseShortDTO {
        private  Long id;
        private  String name;
        private  String city;
        private  String timeZone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleShortDTO {
        private  Long id;
        private  String name;
        private  int cost;
        private  int yearOfRelease;
    }
}
