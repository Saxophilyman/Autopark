// notify-service/src/main/java/org/example/notify/telegram/TelegramNotifier.java
package org.example.notify.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.VehicleEvent;
import org.example.notify.client.AutoparkClient;
import org.example.notify.model.VehicleBriefDto;
import org.example.notify.session.SessionStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

@Component
@ConditionalOnProperty(
        value = "telegram.bot.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class TelegramNotifier {

    private final TelegramUpdateHandler bot;
    private final SessionStore sessions;
    private final AutoparkClient http;

    public void notifyVehicleEvent(VehicleEvent evt) {
        Optional<Long> chatOpt = sessions.findChatId(evt.managerId());
        if (chatOpt.isEmpty()) {
            log.info("Manager {} has no chat bound; skip TG notify", evt.managerId());
            return;
        }
        long chatId = chatOpt.get();

        String plate = evt.licensePlate();
        String name  = evt.vehicleName();
        String ent   = evt.enterpriseName();

        if (plate == null || name == null || ent == null) {
            var brief = http.lookupVehicleBrief(evt.vehicleGuid())
                    .orElse(new VehicleBriefDto("—", "—", "—"));
            if (plate == null) plate = brief.plate();
            if (name  == null) name  = brief.name();
            if (ent   == null) ent   = brief.enterpriseName();
        }

        String text = """
                Событие по автомобилю
                предприятие: %s
                номер: %s
                описание: %s
                действие: %s
                """.formatted(ent, plate, name, evt.action());

        send(chatId, text);
    }

    public void send(long chatId, String text) {
        try {
            bot.execute(new SendMessage(Long.toString(chatId), text));
        } catch (Exception e) {
            log.warn("Failed to send TG message", e);
        }
    }
}
