//package org.example.autopark.telegramBot;
//
//import lombok.RequiredArgsConstructor;
//import org.example.autopark.repository.ManagerRepository;
//import org.example.autopark.telegramBot.TelegramAuthSession;
//import org.springframework.context.annotation.Profile;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//@Profile("!reactive")
//@RequiredArgsConstructor
//public class TelegramAuthService {
//
//    private final TelegramAuthSession session;
//    private final ManagerRepository managerRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public Optional<String> processMessage(Long chatId, String message) {
//        switch (message) {
//            case "/start" -> {
//                return Optional.of("Привет! Напиши /login для входа.");
//            }
//            case "/login" -> {
//                session.startAuth(chatId);
//                return Optional.of("Введите логин:");
//            }
//            default -> {
//                var step = session.getStep(chatId);
//                if (step == null) {
//                    return Optional.of("Неизвестная команда. Напиши /login для входа.");
//                } else if (step == TelegramAuthSession.AuthStep.WAITING_USERNAME) {
//                    session.setUsername(chatId, message);
//                    return Optional.of("Введите пароль:");
//                } else if (step == TelegramAuthSession.AuthStep.WAITING_PASSWORD) {
//                    String username = session.getUsername(chatId);
//                    session.clear(chatId); // очищаем промежуточное состояние
//
//                    // Пытаемся найти менеджера и проверить пароль
//                    return managerRepository.findByUsername(username)
//                            .filter(manager -> passwordEncoder.matches(message, manager.getPassword()))
//                            .map(manager -> {
//                                session.authorize(chatId, manager); // ✅ сохраняем авторизацию
//                                return "Успешный вход! Добро пожаловать, " + username +
//                                ". Введите /report_vehicle для отчёта.";
//                            })
//                            .or(() -> Optional.of("Неверный логин или пароль."));
//                } else {
//                    return Optional.empty();
//                }
//            }
//        }
//    }
//
//
//}
