package org.example.autopark.GPS;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class GenerateRandomTimeForDate {
    public static Instant generateRandomTimeForDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(dateStr, formatter);

        Random random = new Random();

        // Генерируем случайные часы (0-23) и минуты (0-59)
        int hours = random.nextInt(24);
        int minutes = random.nextInt(60);
        int seconds = random.nextInt(60);

        // Создаём LocalDateTime с этими значениями
        LocalDateTime randomDateTime = date.atTime(hours, minutes, seconds);

        // Конвертируем в Instant (UTC)
        return randomDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

}
