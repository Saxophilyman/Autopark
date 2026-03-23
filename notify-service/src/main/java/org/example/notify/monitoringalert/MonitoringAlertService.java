package org.example.notify.monitoringalert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.MonitoringEvent;
import org.example.notify.monitoringalert.email.EmailNotifier;
import org.example.notify.telegram.TelegramSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import org.example.notify.monitoringalert.email.EmailNotifier;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringAlertService {

    private final TelegramSender telegramSender;
    private final ObjectProvider<EmailNotifier> emailNotifierProvider;

    public void handle(MonitoringEvent evt) {
        String text = formatText(evt);

        // 1) Всегда отправляем в Telegram
        telegramSender.sendToAlertChat(text);

        // 2) Пробуем взять EmailNotifier, если он есть в контексте
        EmailNotifier emailNotifier = emailNotifierProvider.getIfAvailable();
        if (emailNotifier != null) {
            try {
                emailNotifier.sendAlert(text);
            } catch (Exception e) {
                log.warn("Failed to send monitoring email alert", e);
            }
        } else {
            log.debug("EmailNotifier bean not present, skip email alert");
        }
    }

    private String formatText(MonitoringEvent evt) {
        StringBuilder sb = new StringBuilder();
        sb.append("Мониторинг Autopark\n");
        sb.append("Тип: ").append(evt.type()).append("\n");
        sb.append("Серьёзность: ").append(evt.severity()).append("\n");
        sb.append("Источник: ").append(evt.source()).append("\n");
        sb.append("Сообщение: ").append(evt.message()).append("\n");

        Map<String, String> attrs = evt.attributes();
        if (attrs != null && !attrs.isEmpty()) {
            sb.append("Детали:\n");
            attrs.forEach((k, v) -> sb.append("  ").append(k).append(" = ").append(v).append("\n"));
        }
        return sb.toString();
    }
}
