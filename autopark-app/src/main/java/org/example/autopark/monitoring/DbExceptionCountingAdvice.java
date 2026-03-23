package org.example.autopark.monitoring;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * Глобальный обработчик SQL/DB-ошибок, который:
 *  1) инкрементирует мониторинговую метрику SQL-ошибок;
 *  2) логирует исключение;
 *  3) (в базовом варианте) возвращает 500 с текстом "Database error".
 *
 * Позже, если у тебя уже есть свой единый формат ошибок,
 * можно оставить только инкремент и делегировать форматирование ответов другому @ControllerAdvice.
 */

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class DbExceptionCountingAdvice {

    private final MonitoringMetrics monitoringMetrics;

    @ExceptionHandler({DataAccessException.class, SQLException.class})
    public ResponseEntity<String> handleDbError(Exception ex) {
        // 1. Инкрементируем счётчик SQL-ошибок
        monitoringMetrics.incrementSqlError();

        // 2. Пишем в лог полную ошибку
        log.error("Database error", ex);

        // 3. Возвращаем базовый ответ 500
        // TODO: если у тебя уже есть свой глобальный формат ошибок (DTO),
        // можно вместо String вернуть его, чтобы всё было единообразно.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Database error");
    }
}
