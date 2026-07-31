package org.example.autopark.datageneration.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record GeneratedEnterpriseData(
        Long enterpriseId,
        List<VehicleDraft> vehicles,
        List<DriverDraft> drivers,
        List<DriverVehicleAssignment> assignments
) {
    public GeneratedEnterpriseData {
        Objects.requireNonNull(enterpriseId, "ID предприятия не задан");
        Objects.requireNonNull(vehicles, "Список автомобилей не задан");
        Objects.requireNonNull(drivers, "Список водителей не задан");
        Objects.requireNonNull(assignments, "Список назначений не задан");

        vehicles = List.copyOf(vehicles);
        drivers = List.copyOf(drivers);
        assignments = List.copyOf(assignments);

        validateAssignments(vehicles, drivers, assignments);
    }

    private static void validateAssignments(
            List<VehicleDraft> vehicles,
            List<DriverDraft> drivers,
            List<DriverVehicleAssignment> assignments
    ) {
        Set<Integer> assignedDrivers = new HashSet<>();
        Set<Integer> assignedVehicles = new HashSet<>();

        for (DriverVehicleAssignment assignment : assignments) {
            if (assignment.driverIndex() >= drivers.size()) {
                throw new IllegalArgumentException("Назначение ссылается на отсутствующего водителя");
            }
            if (assignment.vehicleIndex() >= vehicles.size()) {
                throw new IllegalArgumentException("Назначение ссылается на отсутствующий автомобиль");
            }
            if (!assignedDrivers.add(assignment.driverIndex())) {
                throw new IllegalArgumentException("Водитель назначен более чем одному активному автомобилю");
            }
            if (!assignedVehicles.add(assignment.vehicleIndex())) {
                throw new IllegalArgumentException("Автомобилю назначено более одного активного водителя");
            }
        }
    }
}
