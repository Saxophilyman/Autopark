package org.example.autopark.telegramBot.tg.controller;

import lombok.RequiredArgsConstructor;

import org.example.autopark.repository.VehicleRepository; // свой репозиторий
import org.example.autopark.telegramBot.tg.notifydto.VehiclesSummaryDto;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotifySummaryController {

    private final VehicleRepository vehicles; // при необходимости поменяйте

    // GET /api/notify/vehicles/summary?managerId=1
    @GetMapping("/vehicles/summary")
    public VehiclesSummaryDto summary(@RequestParam Long managerId) {
        // Минимально рабочая версия: считаем общее число. Остальное — null/empty.
        long total = vehicles.count(); // при желании замените на countByManager(managerId)

        return new VehiclesSummaryDto(
                total,
                null, null, null,
                Map.of()
        );
    }
}
