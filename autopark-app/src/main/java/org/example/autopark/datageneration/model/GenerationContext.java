package org.example.autopark.datageneration.model;

import java.util.Objects;
import java.util.Set;

public record GenerationContext(
        Long enterpriseId,
        String enterpriseName,
        Long brandId,
        String brandName,
        Set<String> occupiedLicensePlates
) {
    public GenerationContext {
        Objects.requireNonNull(enterpriseId, "ID предприятия не задан");
        Objects.requireNonNull(brandId, "ID бренда не задан");
        Objects.requireNonNull(occupiedLicensePlates, "Список занятых номеров не задан");

        if (enterpriseName == null || enterpriseName.isBlank()) {
            throw new IllegalArgumentException("Название предприятия не задано");
        }
        if (brandName == null || brandName.isBlank()) {
            throw new IllegalArgumentException("Название бренда не задано");
        }

        occupiedLicensePlates = Set.copyOf(occupiedLicensePlates);
    }
}
