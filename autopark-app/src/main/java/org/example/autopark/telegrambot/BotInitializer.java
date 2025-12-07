//package org.example.autopark.telegramBot;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//
//@Component
//@RequiredArgsConstructor
//@Profile("!reactive")
//@Slf4j
//@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true")
//public class BotInitializer {
//
//    private final TelegramBot telegramBot;
//
//    @PostConstruct
//    public void init() {
//        try {
//            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//            botsApi.registerBot(telegramBot);
//            log.info("Бот успешно запущен.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
