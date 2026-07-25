package org.example.notify.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@ConditionalOnProperty(
        name = {"telegram.enabled", "telegram.bot.polling-enabled"},
        havingValue = "true"
)
@RequiredArgsConstructor
@Slf4j
public class TelegramConfig {

    @Bean
    TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    ApplicationRunner registerBot(
            TelegramBotsApi telegramBotsApi,
            TelegramUpdateHandler telegramUpdateHandler,
            @Value("${telegram.bot.polling-enabled:false}") boolean pollingEnabled
    ) {
        return args -> {
            if (!pollingEnabled) {
                log.info("Telegram long polling is disabled (telegram.bot.polling-enabled=false).");
                return;
            }

            try {
                telegramBotsApi.registerBot(telegramUpdateHandler);
                log.info("Telegram bot successfully registered via long polling");
            } catch (TelegramApiException e) {
                log.error("Failed to register Telegram bot. Service will continue without bot.", e);
            }
        };
    }
}
