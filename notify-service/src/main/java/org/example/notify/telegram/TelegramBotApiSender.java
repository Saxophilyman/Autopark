package org.example.notify.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(value = "telegram.enabled", havingValue = "true")
public class TelegramBotApiSender implements TelegramSender {

    private final RestClient telegramRest;

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.alert-chat-id:0}")
    private long alertChatId;

    public TelegramBotApiSender(@Qualifier("telegramRestClient") RestClient telegramRest) {
        this.telegramRest = telegramRest;
    }

    @Override
    public void send(long chatId, String text) {
        if (botToken == null || botToken.isBlank()) {
            log.warn("TELEGRAM_BOT_TOKEN is empty. Skip send.");
            return;
        }

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text,
                "disable_web_page_preview", true
        );

        try {
            // Важно: не через шаблон /bot{token}/..., чтобы token не URL-энкодился
            telegramRest.post()
                    .uri("/bot" + botToken + "/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Telegram message sent to chatId={}", chatId);
        } catch (Exception e) {
            log.error("Telegram sendMessage failed", e);
        }
    }

    @Override
    public void sendToAlertChat(String text) {
        if (alertChatId == 0L) {
            log.warn("TELEGRAM_ALERT_CHAT_ID is not configured. Skip alert send.");
            return;
        }
        send(alertChatId, text);
    }
}
