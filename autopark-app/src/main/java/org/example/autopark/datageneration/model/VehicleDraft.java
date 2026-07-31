package org.example.autopark.datageneration.model;

import java.util.Objects;

public record VehicleDraft(
        String name,
        String licensePlate,
        int cost,
        int yearOfRelease,
        Long brandId
) {
    public VehicleDraft {
        Objects.requireNonNull(brandId, "ID бренда не задан");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название автомобиля не задано");
        }
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("Государственный номер не задан");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Стоимость автомобиля не может быть отрицательной");
        }
        if (yearOfRelease < 1900) {
            throw new IllegalArgumentException("Некорректный год выпуска автомобиля");
        }
    }
}
