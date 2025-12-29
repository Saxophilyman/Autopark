package org.example.autopark.telegrambot.tg.controller;

import lombok.RequiredArgsConstructor;

import org.example.autopark.repository.VehicleRepository; // свой репозиторий
import org.example.autopark.telegrambot.tg.notifydto.VehiclesSummaryDto;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@Profile("!reactive")
@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotifySummaryController {

    private final VehicleRepository vehicles; // при необходимости поменяйте

    // GET /api/notify/vehicles/summary?managerId=1
    @GetMapping("/vehicles/summary")
    public VehiclesSummaryDto summary(@RequestParam Long managerId) {
        // TODO: сейчас считаем только общее количество ТС, managerId не используется.
        // В будущем сюда можно добавить фильтр по менеджеру и агрегаты.
        long total = vehicles.count(); // при желании замените на countByManager(managerId)

        return new VehiclesSummaryDto(
                total,
                null, null, null,
                Map.of()
        );
    }
}
