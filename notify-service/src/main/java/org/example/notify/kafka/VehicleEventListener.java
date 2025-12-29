package org.example.notify.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.VehicleEvent;
import org.example.notify.telegram.TelegramNotifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleEventListener {

    private final TelegramNotifier tg;

    @KafkaListener(
            topics = "vehicle.events",
            groupId = "notify-service",
            properties = {
                    "spring.json.value.default.type=org.example.events.VehicleEvent",
                    "spring.json.trusted.packages=org.example.events"
            }
    )
    public void onVehicleEvent(VehicleEvent evt) {
        log.info("vehicle.events: {}", evt);
        tg.notifyVehicleEvent(evt);
    }
}
