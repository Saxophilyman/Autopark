package org.example.autopark.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Обёртка над Micrometer для мониторинговых метрик.
 * Здесь мы регистрируем счётчики и даём удобные методы инкремента.
 */
@Component
public class MonitoringMetrics  {
    @Getter
    private final Counter sqlErrorsCounter;

    @Getter
    private final Counter httpClient4xxErrorsCounter;

    @Getter
    private final Counter httpClient5xxErrorsCounter;

    public MonitoringMetrics(MeterRegistry meterRegistry) {
        // SQL-ошибки
        this.sqlErrorsCounter = Counter.builder("autopark.sql.errors")
                .description("Количество SQL-ошибок в приложении")
                .register(meterRegistry);

        // HTTP 4xx ошибки клиента
        this.httpClient4xxErrorsCounter = Counter.builder("autopark.http.client.errors.4xx")
                .description("Количество 4xx ошибок HTTP-клиента")
                .register(meterRegistry);

        // HTTP 5xx ошибки сервера
        this.httpClient5xxErrorsCounter = Counter.builder("autopark.http.client.errors.5xx")
                .description("Количество 5xx ошибок HTTP-клиента")
                .register(meterRegistry);
    }

    // Удобные методы, чтобы снаружи не работать с MeterRegistry и Counter напрямую

    public void incrementSqlError() {
        sqlErrorsCounter.increment();
    }

    public void incrementHttpClient4xx() {
        httpClient4xxErrorsCounter.increment();
    }

    public void incrementHttpClient5xx() {
        httpClient5xxErrorsCounter.increment();
    }

    // Если хочешь аналог getTotalCount() для SQL:
    public double getSqlErrorsTotal() {
        return sqlErrorsCounter.count();
    }
}
