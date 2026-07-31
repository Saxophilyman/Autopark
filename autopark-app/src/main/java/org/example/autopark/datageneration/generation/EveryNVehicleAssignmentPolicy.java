package org.example.autopark.datageneration.generation;

import org.example.autopark.datageneration.model.DriverVehicleAssignment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!reactive")
public class EveryNVehicleAssignmentPolicy {

    /**
     * Сохраняет прежнюю семантику DataGenService: при шаге 2 назначения получают
     * автомобили с индексами 0, 2, 4 и далее, а число назначений равно
     * floor(vehicleCount / assignmentStep), но не больше числа водителей.
     */
    public List<DriverVehicleAssignment> assign(
            int vehicleCount,
            int driverCount,
            int assignmentStep
    ) {
        if (vehicleCount < 0 || driverCount < 0) {
            throw new IllegalArgumentException("Количество автомобилей и водителей не может быть отрицательным");
        }
        if (assignmentStep < 1) {
            throw new IllegalArgumentException("Шаг назначения должен быть не меньше 1");
        }

        int assignmentCount = Math.min(driverCount, vehicleCount / assignmentStep);
        List<DriverVehicleAssignment> result = new ArrayList<>(assignmentCount);

        for (int driverIndex = 0; driverIndex < assignmentCount; driverIndex++) {
            int vehicleIndex = driverIndex * assignmentStep;
            result.add(new DriverVehicleAssignment(driverIndex, vehicleIndex));
        }

        return List.copyOf(result);
    }
}
