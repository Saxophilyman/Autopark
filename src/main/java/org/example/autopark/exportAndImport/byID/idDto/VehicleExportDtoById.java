package org.example.autopark.exportAndImport.byID.idDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.trip.TripDTO;

import java.util.List;

@Data
public class VehicleExportDtoById {
    private EnterpriseShortDTO enterprise;
    private VehicleShortDTO vehicle;
    private List<TripDTO> trips;

    public static VehicleExportDtoById fromEntities(Vehicle vehicle, Enterprise enterprise, List<TripDTO> trips) {
        VehicleExportDtoById dto = new VehicleExportDtoById();

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
                vehicle.getVehicleYearOfRelease(),
                vehicle.getBrandOwner().getBrandName()
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
        private String brand;
    }
}
