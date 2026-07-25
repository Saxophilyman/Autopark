package org.example.autopark.monitoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringProperties {
    /**
     * Глобальный флаг включения мониторинга
     */
    private boolean enabled = true;
    /**
     * Интервал проверки в мс (по умолчанию 60 сек)
     */
    private long checkIntervalMs = 60_000;

    private Sql sql = new Sql();
    private Latency latency = new Latency();
    private HttpClient httpClient = new HttpClient();

    @Data
    public static class Sql {
        private long errorsPerIntervalThreshold = 1;
    }

    @Data
    public static class Latency {
        private double p95ThresholdSeconds = 1.5;
        /**
         * Dev-URI "медленного" эндпоинта.
         * Если таймер по этому URI найден, мы дополнительно
         * проверяем его p95 и берём максимум из общего и dev-таймера.
         *
         * В проде можно оставить как есть (эндпоинта всё равно нет),
         * либо задать пустую строку, тогда будет использоваться только общий p95.
         */
        private String devUri = "/dev/monitoring/slow";
        /**
         * Минимальный интервал между LATENCY-алертами (в секундах).
         * Если с прошлого алерта прошло меньше этого времени — новый не отправляем.
         */
        private long minAlertIntervalSeconds = 60; // дефолт — раз в минуту
    }

    @Data
    public static class HttpClient {
        private long errors4xxPerIntervalThreshold = 10;
        private long errors5xxPerIntervalThreshold = 5;
    }
}

