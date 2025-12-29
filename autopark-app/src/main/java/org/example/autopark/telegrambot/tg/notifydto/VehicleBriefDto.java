package org.example.autopark.telegrambot.tg.notifydto;

public record VehicleBriefDto(
        String plate,           // номер (license plate)
        String name,            // отображаемое имя/модель (если есть)
        String enterpriseName   // название предприятия (если есть)
) {}
