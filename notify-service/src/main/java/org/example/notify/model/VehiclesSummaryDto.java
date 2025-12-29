package org.example.notify.model;

import java.util.Map;

/** Должен совпасть по полям с DTO, который отдаёт autopark-app */
public record VehiclesSummaryDto(
        long total,
        Integer minYear,
        Integer maxYear,
        Integer avgCost,
        Map<String, Long> byBrand
) {}
