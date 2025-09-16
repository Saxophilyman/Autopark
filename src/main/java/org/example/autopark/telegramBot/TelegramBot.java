package org.example.autopark.telegramBot;

import lombok.RequiredArgsConstructor;
import org.example.autopark.Report.*;
import org.example.autopark.entity.Manager;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.VehicleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Component
@Profile("!reactive")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true")
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramAuthService authService;
    private final TelegramBotConfig config;
    private final TelegramAuthSession session;
    private final ReportService reportService;
    private final VehicleService vehicleService;

    private static final double MIN_DISTANCE_KM = 1.0; // Порог минимального пробега для отображения

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();

        // 1. Авторизация
        if (!session.isAuthorized(chatId)) {
            authService.processMessage(chatId, message).ifPresent(response -> send(chatId, response));
            return;
        }

        // 2. Команды отчётов
        if (message.equals("/report_vehicle")) {
            session.setReportStep(chatId, TelegramAuthSession.ReportStep.WAITING_REPORT_TYPE);
            send(chatId, "Выберите тип отчёта (VEHICLE_MILEAGE):");
            return;
        }
        TelegramAuthSession.ReportStep step = session.getReportStep(chatId);
        if (step != null) {
            switch (step) {
                case WAITING_REPORT_TYPE -> {
                    try {
                        ReportType type = ReportType.valueOf(message);
                        session.setReportType(chatId, type.name());
                        session.setReportStep(chatId, TelegramAuthSession.ReportStep.WAITING_VEHICLE);
                        send(chatId, "Введите номер автомобиля:");
                    } catch (IllegalArgumentException e) {
                        send(chatId, "Неверный тип. Пример: VEHICLE_MILEAGE");
                    }
                }

                case WAITING_VEHICLE -> {
                    session.setLicensePlate(chatId, message);
                    session.setReportStep(chatId, TelegramAuthSession.ReportStep.WAITING_PERIOD_TYPE);
                    send(chatId, "Выберите период (DAY, MONTH, YEAR):");
                }

                case WAITING_PERIOD_TYPE -> {
                    try {
                        PeriodType period = PeriodType.valueOf(message.toUpperCase());
                        session.setPeriodType(chatId, period);
                        session.setReportStep(chatId, TelegramAuthSession.ReportStep.WAITING_FROM_DATE);
                        send(chatId, switch (period) {
                            case DAY -> "Введите дату начала (дд.MM.гггг):";
                            case MONTH -> "Введите месяц начала (MM.yyyy):";
                            case YEAR -> "Введите год начала (yyyy):";
                        });
                    } catch (IllegalArgumentException e) {
                        send(chatId, "Неверный период. Варианты: DAY, MONTH, YEAR");
                    }
                }

                case WAITING_FROM_DATE -> {
                    try {
                        LocalDate date = parseDate(session.getPeriodType(chatId), message);
                        session.setFromDate(chatId, date);
                        session.setReportStep(chatId, TelegramAuthSession.ReportStep.WAITING_TO_DATE);
                        send(chatId, switch (session.getPeriodType(chatId)) {
                            case DAY -> "Введите дату окончания (дд.MM.гггг):";
                            case MONTH -> "Введите месяц окончания (MM.yyyy):";
                            case YEAR -> "Введите год окончания (yyyy):";
                        });
                    } catch (Exception e) {
                        send(chatId, "Неверный формат даты.");
                    }
                }

                case WAITING_TO_DATE -> {
                    try {
                        LocalDate date = parseDate(session.getPeriodType(chatId), message);
                        session.setToDate(chatId, date);
                        send(chatId, "Генерирую отчёт...");
                        Vehicle vehicle = vehicleService.findByLicensePlate(session.getLicensePlate(chatId));
                        // Вызов ReportService
                        ModelReport report = reportService.generateMileageReport(
                                vehicle.getVehicleId(),
                                session.getFromDate(chatId),
                                session.getToDate(chatId),
                                session.getPeriodType(chatId)
                        );
                        List<ReportEntry> entries = report.getResult(); // тут правильно

                        if (entries.isEmpty()) {
                            send(chatId, "Нет данных за указанный период.");
                        } else {
                            StringBuilder sb = new StringBuilder("Отчёт о пробеге:\n");
                            for (ReportEntry entry : entries) {
                                if (entry.getValue() >= MIN_DISTANCE_KM) {
                                    sb.append(entry.getPeriod())
                                            .append(": ")
                                            .append(Math.round(entry.getValue()))
                                            .append(" км\n");
                                }
                            }
                            if (sb.toString().equals("Отчёт о пробеге:\n")) {
                                send(chatId, "Нет значимого пробега по заданному фильтру.");
                            } else {
                                send(chatId, sb.toString());
                            }
                        }

                        session.clearReport(chatId);
                    } catch (Exception e) {
                        send(chatId, "Неверный формат даты окончания.");
                    }
                }
            }
            return;
        }

        // Обработка прочих сообщений
        send(chatId, "Неизвестная команда. Введите /report_vehicle для отчёта.");
    }

    private LocalDate parseDate(PeriodType type, String input) {
        return switch (type) {
            case DAY -> LocalDate.parse(input, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            case MONTH -> LocalDate.parse("01." + input, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            case YEAR -> LocalDate.parse("01.01." + input, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        };
    }


    private void send(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


}
