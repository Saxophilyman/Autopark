package org.example.notify.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notify.session.SessionStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.telegram.enabled", havingValue = "true")
public class TelegramUpdateHandler extends TelegramLongPollingBot {

    private final SessionStore sessions;

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.username}")
    private String username;

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Long chatId = update.getMessage().getChatId();
        String text  = update.getMessage().getText().trim();

        if ("/start".equalsIgnoreCase(text)) {
            send(chatId, """
                    Привет! Я нотификатор Autopark.
                    Твой chatId: %s

                    Для DEV-связывания с менеджером:
                    POST /dev/session/login?managerId=<id>&chatId=%s
                    """.formatted(chatId, chatId));
            return;
        }

        if ("/whoami".equalsIgnoreCase(text)) {
            var managerId = sessions.findManagerId(chatId).orElse(null);
            send(chatId, managerId == null
                    ? "Не связан. Используй DEV-ссылку или прод-аутентификацию."
                    : "Связан с managerId=" + managerId);
            return;
        }

        send(chatId, "Команда не распознана. /start или /whoami");
    }

    private void send(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            log.warn("Failed to send message", e);
        }
    }

    @Override public String getBotUsername() { return username; }
    @Override public String getBotToken()    { return token; }
}
