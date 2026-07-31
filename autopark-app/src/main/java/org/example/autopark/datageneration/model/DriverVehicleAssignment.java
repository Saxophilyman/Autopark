package org.example.autopark.datageneration.model;

public record DriverVehicleAssignment(
        int driverIndex,
        int vehicleIndex
) {
    public DriverVehicleAssignment {
        if (driverIndex < 0) {
            throw new IllegalArgumentException("Индекс водителя не может быть отрицательным");
        }
        if (vehicleIndex < 0) {
            throw new IllegalArgumentException("Индекс автомобиля не может быть отрицательным");
        }
    }
}
