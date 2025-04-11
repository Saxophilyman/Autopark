package org.example.autopark.dto;

import lombok.Data;

@Data
public class VehicleApiDto {
    private Long vehicleId;
    private String vehicleName;
    private String licensePlate;
    private int vehicleCost;
    private int vehicleYearOfRelease;

    private Long brandId; // 👈 Передаём только ID бренда
    private Long enterpriseId; // 👈 Передаём только ID предприятия

    private String purchaseDateEnterpriseTime;
}
