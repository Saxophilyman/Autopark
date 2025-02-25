package org.example.autopark.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @NotEmpty(message = "Обязательное поле")
    private String vehicleName;

    @NotNull
    @Min(value = 0)
    private int vehicleCost;

    @NotNull
    @Min(value = 1900, message = "Транспорт может быть только старше 1900 г.")
    @Max(value = 2024, message = "Транспорт не может быть сейчас старше 2024 г.")
    private int vehicleYearOfRelease;


    private BrandDTO brand;

    private EnterpriseDTO enterprise;

    //через DTO производить конвертацию
    private String purchaseDateEnterpriseTime;
    private String purchaseDateUtc; // Оригинальное UTC время для корректного перевода в браузере

}
