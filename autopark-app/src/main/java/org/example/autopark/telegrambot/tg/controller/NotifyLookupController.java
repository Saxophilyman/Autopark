package org.example.autopark.telegrambot.tg.controller;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;

import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.telegrambot.tg.notifydto.VehicleBriefDto;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@Profile("!reactive")
@RestController
@RequestMapping("/api/notify/lookup")
@RequiredArgsConstructor
public class NotifyLookupController {

    private final VehicleRepository vehicles;

    @GetMapping("/vehicle-brief")
    public ResponseEntity<VehicleBriefDto> vehicleBrief(@RequestParam UUID vehicleGuid) {
        return vehicles.findByGuid(vehicleGuid)
                .map(this::toBrief)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private VehicleBriefDto toBrief(Vehicle v) {
        String plate = v.getLicensePlate();
        String name  = v.getVehicleName();

        String enterpriseName = null;
        Enterprise e = v.getEnterpriseOwnerOfVehicle();
        if (e != null) {
            // ⚠️ Если в твоём классе Enterprise метод называется иначе (например getName()),
            // просто замени e.getEnterpriseName() на актуальный геттер.
            enterpriseName = e.getName();
        }
        return new VehicleBriefDto(plate, name, enterpriseName);
    }
}
