package org.example.autopark.datageneration.model;

public record DriverDraft(
        String name,
        int salary
) {
    public DriverDraft {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя водителя не задано");
        }
        if (salary < 1) {
            throw new IllegalArgumentException("Зарплата водителя должна быть больше нуля");
        }
    }
}
