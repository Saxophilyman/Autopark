package org.example.notify.telegram.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notify.client.AutoparkClient;
import org.example.notify.session.SessionStore;
import org.example.notify.telegram.TelegramSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"telegram.enabled", "telegram.bot.polling-enabled", "notify.features.reports.enabled"},
        havingValue = "true"
)
public class TelegramReportConversation {

    private final ReportConversationStore store;
    private final SessionStore sessions;
    private final AutoparkClient autopark;
    private final TelegramSender tg;

    private static final DateTimeFormatter DAY_RU = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter MONTH_RU = DateTimeFormatter.ofPattern("MM.yyyy");

    public boolean tryStart(long chatId, String text) {
        if (!"/report_vehicle".equalsIgnoreCase(text)) {
            return false;
        }

        var managerOpt = sessions.findManagerId(chatId);
        if (managerOpt.isEmpty()) {
            tg.send(chatId, "Чат не связан с менеджером. Сначала выполни привязку (DEV или через события).");
            return true;
        }

        store.start(chatId);
        tg.send(chatId, "Введите номер автомобиля (license plate):");
        return true;
    }

    public boolean isInConversation(long chatId) {
        return store.isActive(chatId);
    }

    public void handleStep(long chatId, String text) {
        if ("/cancel".equalsIgnoreCase(text)) {
            store.clear(chatId);
            tg.send(chatId, "Ок, отменил генерацию отчёта.");
            return;
        }

        var stateOpt = store.get(chatId);
        if (stateOpt.isEmpty()) {
            return;
        }

        var state = stateOpt.get();

        switch (state.getStep()) {
            case WAITING_VEHICLE -> {
                state.setPlate(text.trim());
                state.setStep(ReportConversationStore.Step.WAITING_PERIOD);
                tg.send(chatId, "Выберите период: DAY, MONTH, YEAR");
            }

            case WAITING_PERIOD -> {
                try {
                    ReportPeriod period = ReportPeriod.valueOf(text.trim().toUpperCase());
                    state.setPeriod(period);
                    state.setStep(ReportConversationStore.Step.WAITING_FROM);

                    tg.send(chatId, switch (period) {
                        case DAY -> "Введите дату начала (дд.MM.гггг или yyyy-MM-dd):";
                        case MONTH -> "Введите месяц начала (MM.yyyy или yyyy-MM):";
                        case YEAR -> "Введите год начала (yyyy):";
                    });
                } catch (Exception e) {
                    tg.send(chatId, "Неверный период. Варианты: DAY, MONTH, YEAR");
                }
            }

            case WAITING_FROM -> {
                try {
                    LocalDate from = parseToLocalDateStart(state.getPeriod(), text.trim());
                    state.setFrom(from);
                    state.setStep(ReportConversationStore.Step.WAITING_TO);

                    tg.send(chatId, switch (state.getPeriod()) {
                        case DAY -> "Введите дату окончания (дд.MM.гггг или yyyy-MM-dd):";
                        case MONTH -> "Введите месяц окончания (MM.yyyy или yyyy-MM):";
                        case YEAR -> "Введите год окончания (yyyy):";
                    });
                } catch (Exception e) {
                    tg.send(chatId, "Неверный формат. Попробуй ещё раз.");
                }
            }

            case WAITING_TO -> {
                try {
                    LocalDate to = parseToLocalDateEnd(state.getPeriod(), text.trim());
                    if (state.getFrom().isAfter(to)) {
                        tg.send(chatId, "Начало периода позже конца. Введите корректное окончание:");
                        return;
                    }
                    state.setTo(to);

                    Long managerId = sessions.findManagerId(chatId).orElseThrow();
                    tg.send(chatId, "Генерирую отчёт...");

                    String reportText = autopark.mileageReport(
                            managerId,
                            state.getPlate(),
                            state.getFrom(),
                            state.getTo(),
                            state.getPeriod()
                    );

                    tg.send(chatId, reportText);
                    store.clear(chatId);

                } catch (Exception e) {
                    log.warn("Report generation failed", e);
                    tg.send(chatId, "Не удалось построить отчёт. Проверь номер авто и даты. /cancel чтобы выйти.");
                }
            }
        }
    }

    private LocalDate parseToLocalDateStart(ReportPeriod period, String input) {
        return switch (period) {
            case DAY -> parseDay(input);
            case MONTH -> parseMonth(input).atDay(1);
            case YEAR -> Year.parse(input).atDay(1);
        };
    }

    private LocalDate parseToLocalDateEnd(ReportPeriod period, String input) {
        return switch (period) {
            case DAY -> parseDay(input);
            case MONTH -> parseMonth(input).atEndOfMonth();
            case YEAR -> Year.parse(input).atMonth(12).atEndOfMonth();
        };
    }

    private LocalDate parseDay(String input) {
        // dd.MM.yyyy или yyyy-MM-dd
        if (input.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
            return LocalDate.parse(input, DAY_RU);
        }
        return LocalDate.parse(input); // ISO yyyy-MM-dd
    }

    private YearMonth parseMonth(String input) {
        // MM.yyyy или yyyy-MM
        if (input.matches("\\d{2}\\.\\d{4}")) {
            return YearMonth.parse(input, MONTH_RU);
        }
        return YearMonth.parse(input); // yyyy-MM
    }
}
