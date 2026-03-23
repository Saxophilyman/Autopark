package org.example.autopark.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.monitoring.config.MonitoringProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.example.autopark.monitoring.MonitoringDomainEvent.Severity;
import static org.example.autopark.monitoring.MonitoringDomainEvent.Type;

/**
 * Сервис, который по расписанию проверяет метрики
 * и при превышении порогов публикует MonitoringDomainEvent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringCheckService {

    private final MonitoringProperties props;
    private final MonitoringMetrics metrics;
    private final MeterRegistry meterRegistry;
    private final ApplicationEventPublisher events;

    // Храним прошлые значения счётчиков, чтобы считать "за интервал"
    private double lastSqlErrorsCount = 0.0;
    private double last4xxCount = 0.0;
    private double last5xxCount = 0.0;
    private Instant lastLatencyAlertTime = null;
    // Флаг "мы уже в состоянии деградации по latency"
    // Пока true — новые алерты по LATENCY не шлём, только когда выйдем из деградации и снова зайдём.
    private boolean latencyDegraded = false;

    public void checkAll() {
        if (!props.isEnabled()) {
            log.trace("Monitoring disabled, skip checks");
            return;
        }
        checkSqlErrors();
        checkHttpClientErrors();
        checkLatencyP95();
    }

    // ─────────────────────────────────────────────
    // 1) SQL-ошибки
    // ─────────────────────────────────────────────
    private void checkSqlErrors() {
        double current = metrics.getSqlErrorsCounter().count();
        long delta = (long) (current - lastSqlErrorsCount);
        lastSqlErrorsCount = current;

        long threshold = props.getSql().getErrorsPerIntervalThreshold();

        if (delta <= 0) {
            return; // нет новых ошибок за интервал
        }

        log.debug("SQL errors in interval: {}, threshold: {}", delta, threshold);

        if (delta >= threshold) {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("errorsInInterval", Long.toString(delta));
            attrs.put("threshold", Long.toString(threshold));

            MonitoringDomainEvent event = new MonitoringDomainEvent(
                    Type.SQL_ERRORS,
                    Severity.CRITICAL,
                    "SQL-ошибок за интервал: " + delta + " (порог " + threshold + ")",
                    attrs
            );
            events.publishEvent(event);
        }
    }

    // ─────────────────────────────────────────────
    // 2) HTTP-клиент 4xx/5xx (исходящие запросы)
    // ─────────────────────────────────────────────
    private void checkHttpClientErrors() {
        // 4xx
        double current4xx = metrics.getHttpClient4xxErrorsCounter().count();
        long delta4xx = (long) (current4xx - last4xxCount);
        last4xxCount = current4xx;

        long threshold4xx = props.getHttpClient().getErrors4xxPerIntervalThreshold();

        if (delta4xx > 0) {
            log.debug("HTTP client 4xx in interval: {}, threshold: {}", delta4xx, threshold4xx);
        }

        if (delta4xx > 0 && delta4xx >= threshold4xx) {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("errors4xxInInterval", Long.toString(delta4xx));
            attrs.put("threshold4xx", Long.toString(threshold4xx));

            MonitoringDomainEvent event = new MonitoringDomainEvent(
                    Type.HTTP_CLIENT_ERRORS,
                    Severity.WARNING, // 4xx чаще всего проблема на стороне клиента/запроса
                    "HTTP 4xx (client) за интервал: " + delta4xx + " (порог " + threshold4xx + ")",
                    attrs
            );
            events.publishEvent(event);
        }

        // 5xx
        double current5xx = metrics.getHttpClient5xxErrorsCounter().count();
        long delta5xx = (long) (current5xx - last5xxCount);
        last5xxCount = current5xx;

        long threshold5xx = props.getHttpClient().getErrors5xxPerIntervalThreshold();

        if (delta5xx > 0) {
            log.debug("HTTP client 5xx in interval: {}, threshold: {}", delta5xx, threshold5xx);
        }

        if (delta5xx > 0 && delta5xx >= threshold5xx) {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("errors5xxInInterval", Long.toString(delta5xx));
            attrs.put("threshold5xx", Long.toString(threshold5xx));

            MonitoringDomainEvent event = new MonitoringDomainEvent(
                    Type.HTTP_CLIENT_ERRORS,
                    Severity.CRITICAL, // 5xx — уже проблема на стороне сервиса
                    "HTTP 5xx (client) за интервал: " + delta5xx + " (порог " + threshold5xx + ")",
                    attrs
            );
            events.publishEvent(event);
        }
    }

    // ─────────────────────────────────────────────
    // 3) Задержка (p95 по http.server.requests)
    // ─────────────────────────────────────────────
    private void checkLatencyP95() {
        double thresholdSeconds = props.getLatency().getP95ThresholdSeconds();
        String devUri = props.getLatency().getDevUri();

        // 1) Общий таймер
        Double p95All = getTimerPercentile("http.server.requests", null, null, 0.95);

        // 2) Опционально: таймер по dev-URI
        Double p95Dev = null;
        if (devUri != null && !devUri.isBlank()) {
            p95Dev = getTimerPercentile("http.server.requests", "uri", devUri, 0.95);
        }

        double p95Seconds = maxNonNaN(p95All, p95Dev);

        if (Double.isNaN(p95Seconds)) {
            log.debug("No latency percentile data available (all={}, devUri={}, uri={})",
                    p95All, p95Dev, devUri);
            return;
        }

        log.info("LATENCY check: p95All={}s, p95Dev={}s (uri={}), chosen={}s, threshold={}s",
                p95All, p95Dev, devUri, p95Seconds, thresholdSeconds);

        if (p95Seconds < thresholdSeconds) {
            // Порог не превышен — просто выходим, и заодно можно считать,
            // что "инцидент" закончился (при желании потом добавим recovery-ивенты).
            return;
        }

        // ───── АНТИ-ФЛУД ─────
        long minIntervalSec = props.getLatency().getMinAlertIntervalSeconds();
        Instant now = Instant.now();

        if (lastLatencyAlertTime != null) {
            long secondsSinceLast = Duration.between(lastLatencyAlertTime, now).getSeconds();
            if (secondsSinceLast < minIntervalSec) {
                log.info(
                        "LATENCY alert suppressed: only {}s since last (min {}s). p95={}s, threshold={}s",
                        secondsSinceLast, minIntervalSec, p95Seconds, thresholdSeconds
                );
                return;
            }
        }

        // ───── ИСТИННЫЙ АЛЕРТ ─────

        Map<String, String> attrs = new HashMap<>();
        attrs.put("p95Seconds", Double.toString(p95Seconds));
        attrs.put("thresholdSeconds", Double.toString(thresholdSeconds));
        if (p95All != null && !Double.isNaN(p95All)) {
            attrs.put("p95AllSeconds", Double.toString(p95All));
        }
        if (p95Dev != null && !Double.isNaN(p95Dev)) {
            attrs.put("p95DevSeconds", Double.toString(p95Dev));
            attrs.put("devUri", devUri);
        }

        MonitoringDomainEvent event = new MonitoringDomainEvent(
                Type.LATENCY,
                Severity.CRITICAL,
                String.format(
                        "P95 задержки HTTP-запросов %.3fs (all/dev) превышает порог %.3fs",
                        p95Seconds, thresholdSeconds
                ),
                attrs
        );

        events.publishEvent(event);
        lastLatencyAlertTime = now; // запоминаем время последней отправки
    }

    /**
     * Берём заданный перцентиль у таймера.
     * Если tagKey/tagValue == null, берём "голый" таймер без фильтра по тегам.
     */
    private Double getTimerPercentile(String metricName,
                                      String tagKey,
                                      String tagValue,
                                      double percentile) {

        MeterRegistry.Config config = meterRegistry.config(); // просто чтобы IDE не ругался, не обязательно

        Timer timer;
        if (tagKey == null || tagValue == null) {
            timer = meterRegistry.find(metricName).timer();
        } else {
            timer = meterRegistry.find(metricName)
                    .tag(tagKey, tagValue)
                    .timer();
        }

        if (timer == null) {
            return Double.NaN;
        }

        double value = timer.percentile(percentile, TimeUnit.SECONDS);
        return Double.isNaN(value) ? Double.NaN : value;
    }

    /**
     * Максимум из двух значений, корректно обрабатывающий NaN.
     */
    private double maxNonNaN(Double a, Double b) {
        boolean aValid = a != null && !Double.isNaN(a);
        boolean bValid = b != null && !Double.isNaN(b);

        if (aValid && bValid) {
            return Math.max(a, b);
        } else if (aValid) {
            return a;
        } else if (bValid) {
            return b;
        } else {
            return Double.NaN;
        }
    }
}
