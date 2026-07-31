package org.example.autopark.datageneration.application;

import java.util.List;
import java.util.Objects;

public record GenerateEnterpriseDataCommand(
        List<Long> enterpriseIds,
        int vehicleCount,
        int driverCount,
        int assignmentStep
) {
    public GenerateEnterpriseDataCommand {
        Objects.requireNonNull(enterpriseIds, "Список предприятий не задан");
        enterpriseIds = List.copyOf(enterpriseIds);

        if (enterpriseIds.isEmpty()) {
            throw new IllegalArgumentException("Список предприятий не может быть пустым");
        }
        if (enterpriseIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("ID предприятия не может быть null");
        }
        if (vehicleCount < 0) {
            throw new IllegalArgumentException("Число автомобилей не может быть отрицательным");
        }
        if (driverCount < 0) {
            throw new IllegalArgumentException("Число водителей не может быть отрицательным");
        }
        if (assignmentStep < 1) {
            throw new IllegalArgumentException("Шаг назначения должен быть не меньше 1");
        }
    }
}
