package org.example.autopark.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.autopark.dto.EnterpriseDTO;
import org.example.autopark.dto.VehicleDTOForDriver;
import org.example.autopark.entity.Vehicle;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverId;

    @NotEmpty(message = "Обязательное поле")
    private String name;

    @NotEmpty(message = "Обязательное поле")
    private String salary;

    private EnterpriseDTO enterprise;

    private boolean isActive;
//    @JsonIgnore
    private VehicleDTOForDriver activeVehicle;
}
