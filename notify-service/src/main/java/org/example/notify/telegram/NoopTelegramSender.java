package org.example.notify.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(value = "telegram.enabled", havingValue = "false", matchIfMissing = true)
public class NoopTelegramSender implements TelegramSender {
    @Override public void send(long chatId, String text) {
        log.debug("Telegram disabled. Skip send to chatId={}", chatId);
    }
    @Override public void sendToAlertChat(String text) {
        log.debug("Telegram disabled. Skip sendToAlertChat");
    }
}
