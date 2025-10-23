// src/main/java/org/example/notify/telegram/TelegramConfig.java
package org.example.notify.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@ConditionalOnProperty(value = "app.telegram.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TelegramConfig {

    // Регистрируем API и сам бот (лонг-поллинг)
    @Bean
    TelegramBotsApi telegramBotsApi() throws Exception {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    ApplicationRunner registerBot(TelegramBotsApi api, TelegramUpdateHandler handler) {
        return args -> api.registerBot(handler);
    }
}




//package org.example.notify.telegram;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//
//@Configuration
//public class TelegramConfig {
//
//    @Bean
//    public TelegramLongPollingBot telegramBot(
//            @Value("${telegram.bot.token}") String token,
//            @Value("${telegram.bot.username}") String username,
//            TelegramUpdateHandler handler
//    ) {
//        return new TelegramLongPollingBot() {
//            @Override public String getBotUsername() { return username; }
//            @Override public String getBotToken() { return token; }
//            @Override public void onUpdateReceived(Update update) { handler.onUpdate(this, update); }
//        };
//    }
//
//    @Bean
//    public TelegramBotsApi telegramBotsApi(TelegramLongPollingBot bot) throws Exception {
//        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
//        api.registerBot(bot);
//        return api;
//    }
//}
