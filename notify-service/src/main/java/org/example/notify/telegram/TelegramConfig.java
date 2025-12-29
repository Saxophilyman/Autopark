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
        value = "telegram.bot.enabled",
        havingValue = "true",
        matchIfMissing = true
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
            @Value("${telegram.bot.enabled:true}") boolean enabled
    ) {
        return args -> {
            if (!enabled) {
                log.info("Telegram bot is disabled by property 'telegram.bot.enabled=false'.");
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