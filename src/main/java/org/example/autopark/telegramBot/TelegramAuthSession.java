package org.example.autopark.telegramBot;

import lombok.Getter;
import org.example.autopark.Report.PeriodType;
import org.example.autopark.entity.Manager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("!reactive")
public class TelegramAuthSession {
    public enum AuthStep {
        WAITING_USERNAME,
        WAITING_PASSWORD
    }

    public enum ReportStep {
        WAITING_REPORT_TYPE,
        WAITING_VEHICLE,
        WAITING_PERIOD_TYPE,
        WAITING_FROM_DATE,
        WAITING_TO_DATE
    }

    // Временное хранение шагов логина
    private final Map<Long, AuthStep> stepMap = new HashMap<>();
    private final Map<Long, String> usernameMap = new HashMap<>();
    // Храним авторизованных менеджеров
    @Getter
    private final Map<Long, Manager> authorizedManagers = new ConcurrentHashMap<>();


    private final Map<Long, ReportStep> reportSteps = new HashMap<>();
    private final Map<Long, String> reportType = new HashMap<>();
    private final Map<Long, String> licensePlate = new HashMap<>();
    private final Map<Long, PeriodType> periodType = new HashMap<>();
    private final Map<Long, LocalDate> fromDate = new HashMap<>();
    private final Map<Long, LocalDate> toDate = new HashMap<>();

    // ----------- AUTH -----------
    public void startAuth(Long chatId) {
        stepMap.put(chatId, AuthStep.WAITING_USERNAME);
    }

    public void setUsername(Long chatId, String username) {
        usernameMap.put(chatId, username);
        stepMap.put(chatId, AuthStep.WAITING_PASSWORD);
    }

    public AuthStep getStep(Long chatId) {
        return stepMap.get(chatId);
    }

    public String getUsername(Long chatId) {
        return usernameMap.get(chatId);
    }

    public void clear(Long chatId) {
        stepMap.remove(chatId);
        usernameMap.remove(chatId);
    }

    public void authorize(Long chatId, Manager manager) {
        authorizedManagers.put(chatId, manager);
    }

    public Optional<Manager> getAuthorizedManager(Long chatId) {
        return Optional.ofNullable(authorizedManagers.get(chatId));
    }

    public boolean isAuthorized(Long chatId) {
        return authorizedManagers.containsKey(chatId);
    }

    public void logout(Long chatId) {
        authorizedManagers.remove(chatId);
    }

    // ----------- REPORT -----------
    public void setReportStep(Long chatId, ReportStep step) {
        reportSteps.put(chatId, step);
    }

    public ReportStep getReportStep(Long chatId) {
        return reportSteps.get(chatId);
    }

    public void setReportType(Long chatId, String type) {
        reportType.put(chatId, type);
    }

    public String getReportType(Long chatId) {
        return reportType.get(chatId);
    }

    public void setLicensePlate(Long chatId, String plate) {
        licensePlate.put(chatId, plate);
    }

    public String getLicensePlate(Long chatId) {
        return licensePlate.get(chatId);
    }

    public void setFromDate(Long chatId, LocalDate date) {
        fromDate.put(chatId, date);
    }

    public LocalDate getFromDate(Long chatId) {
        return fromDate.get(chatId);
    }

    public void setToDate(Long chatId, LocalDate date) {
        toDate.put(chatId, date);
    }

    public LocalDate getToDate(Long chatId) {
        return toDate.get(chatId);
    }

    public void setPeriodType(Long chatId, PeriodType type) {
        periodType.put(chatId, type);
    }

    public PeriodType getPeriodType(Long chatId) {
        return periodType.get(chatId);
    }

    public void clearReport(Long chatId) {
        reportSteps.remove(chatId);
        reportType.remove(chatId);
        licensePlate.remove(chatId);
        fromDate.remove(chatId);
        toDate.remove(chatId);
        periodType.remove(chatId);
    }
}
