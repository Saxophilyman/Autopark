package org.example.autopark.telegrambot.tg.notifydto;

import java.util.Map;

public record VehiclesSummaryDto(
        long total,
        Integer minYear,
        Integer maxYear,
        Integer avgCost,
        Map<String, Long> byBrand
) {}
