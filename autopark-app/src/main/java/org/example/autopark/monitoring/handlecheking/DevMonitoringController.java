package org.example.autopark.monitoring.handlecheking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.monitoring.MonitoringCheckService;
import org.example.autopark.monitoring.MonitoringMetrics;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * DEV-контроллер для ручной проверки мониторинга и алёртов.
 *
 * В проде такой контроллер обычно отключают через профиль
 * или не включают в сборку.
 *
 * Отдельные сценарии:
 * /dev/monitoring/simulate-sql-immediate
 * /dev/monitoring/simulate-http-4xx-immediate
 * /dev/monitoring/simulate-http-5xx-immediate
 * /dev/monitoring/simulate-slow-immediate?ms=...
 *
 * Сценарии через планировщик:
 * /dev/monitoring/simulate-sql-scheduler
 * /dev/monitoring/simulate-all-scheduler
 *
 * Комбо:
 * /dev/monitoring/simulate-all-immediate
 *
 * Плюс ручной запуск:
 * /dev/monitoring/run-checks
 */
@Slf4j
@RestController
@RequestMapping("/dev/monitoring")
@RequiredArgsConstructor
public class DevMonitoringController {

    private final MonitoringMetrics metrics;
    private final MonitoringCheckService checkService;
    private final RestTemplate restTemplate;

    // ───────────────────── ВНУТРЕННИЕ ХЕЛПЕРЫ ─────────────────────

    /** Общий сценарий: накрутить ВСЕ типы сигналов. */
    private void simulateAllInternal() {
        // SQL-ошибки
        simulateSqlErrorsInternal(3);
        // HTTP 4xx/5xx
        simulateHttp4xxInternal();
        simulateHttp5xxInternal();
        // латентность
        simulateLatencyBurstInternal(20, 2000);
    }

    /** Просто накрутить N SQL-ошибок в счётчике. */
    private void simulateSqlErrorsInternal(int n) {
        for (int i = 0; i < n; i++) {
            metrics.incrementSqlError();
        }
        log.info("Simulated {} SQL errors (internal)", n);
    }

    /** Вызвать целевой эндпоинт, который отдаёт 400 (4xx) — для интерцептора. */
    private void simulateHttp4xxInternal() {
        String url = "http://localhost:8080/dev/monitoring/target-4xx";
        try {
            restTemplate.getForEntity(url, String.class);
        } catch (RestClientException ex) {
            // Для нас нормально: RestTemplate кидает исключение на 4xx/5xx.
            log.info("Expected 4xx from {}: {}", url, ex.getMessage());
        }
    }

    /** Вызвать целевой эндпоинт, который отдаёт 500 (5xx). */
    private void simulateHttp5xxInternal() {
        String url = "http://localhost:8080/dev/monitoring/target-5xx";
        try {
            restTemplate.getForEntity(url, String.class);
        } catch (RestClientException ex) {
            log.info("Expected 5xx from {}: {}", url, ex.getMessage());
        }
    }

    /**
     * Сделать несколько медленных HTTP-запросов к slow-эндпоинту,
     * чтобы поднять p95 по http.server.requests.
     */
    private void simulateLatencyBurstInternal(int count, long ms) {
        for (int i = 0; i < count; i++) {
            simulateSlowInternal(ms);
        }
        log.info("Simulated {} slow HTTP requests ({} ms each) for latency test", count, ms);
    }

    /** Один медленный запрос. */
    private void simulateSlowInternal(long ms) {
        String url = "http://localhost:8080/dev/monitoring/slow?ms=" + ms;
        try {
            restTemplate.getForEntity(url, String.class);
        } catch (RestClientException ex) {
            log.info("Slow endpoint call failed (unexpected), url={}, ex={}", url, ex.getMessage());
        }
    }

    // ───────────────────── СЦЕНАРИИ ДЛЯ ПРОВЕРКИ ПЛАНИРОВЩИКА ─────────────────────

    /**
     * Накрутить SQL-ошибки, НО НЕ вызывать checkAll().
     * Алёрт должен прийти только когда сработает @Scheduled-планировщик.
     */
    @GetMapping("/simulate-sql-scheduler")
    public String simulateSqlScheduler() {
        simulateSqlErrorsInternal(3);
        return "Simulated 3 SQL errors; waiting for scheduler to run checks";
    }

    /**
     * Накрутить ВСЕ типы сигналов, НО НЕ вызывать checkAll().
     * Планировщик сам подхватит накопленные метрики.
     */
    @GetMapping("/simulate-all-scheduler")
    public String simulateAllScheduler() {
        simulateAllInternal();
        return "Simulated SQL, HTTP 4xx/5xx and slow requests; waiting for scheduler to run checks";
    }

    // ───────────────────── КОМБО-СЦЕНАРИЙ "ВСЁ И СРАЗУ" ─────────────────────

    /**
     * Сгенерировать все типы сигналов и СРАЗУ вызвать checkAll().
     * Удобно для быстрой демонстрации всех алёртов.
     */
    @GetMapping("/simulate-all-immediate")
    public String simulateAllImmediate() {
        simulateAllInternal();
        checkService.checkAll(); // один общий вызов для всех типов
        return "Simulated SQL, HTTP 4xx/5xx and slow requests; monitoring checks triggered";
    }

    // ───────────────────── ЯВНЫЙ РУЧНОЙ ЗАПУСК checkAll ─────────────────────

    /** На всякий случай — просто руками дёрнуть checkAll(). */
    @GetMapping("/run-checks")
    public String runChecks() {
        checkService.checkAll();
        return "Monitoring checks triggered explicitly";
    }

    // ───────────────────── ОТДЕЛЬНЫЕ СЦЕНАРИИ С МГНОВЕННОЙ ПРОВЕРКОЙ ─────────────────────

    /** SQL-ошибки + сразу checkAll() → мгновенный алёрт по SQL. */
    @GetMapping("/simulate-sql-immediate")
    public String simulateSqlImmediate() {
        simulateSqlErrorsInternal(3);
        checkService.checkAll();
        return "Simulated 3 SQL errors and triggered monitoring checks";
    }

    /** HTTP 4xx + сразу checkAll() → алёрт по HTTP_CLIENT_ERRORS (WARNING). */
    @GetMapping("/simulate-http-4xx-immediate")
    public String simulateHttp4xxImmediate() {
        simulateHttp4xxInternal();
        checkService.checkAll();
        return "Triggered HTTP 4xx request and monitoring checks";
    }

    /** HTTP 5xx + сразу checkAll() → алёрт по HTTP_CLIENT_ERRORS (CRITICAL). */
    @GetMapping("/simulate-http-5xx-immediate")
    public String simulateHttp5xxImmediate() {
        simulateHttp5xxInternal();
        checkService.checkAll();
        return "Triggered HTTP 5xx request and monitoring checks";
    }

    /** Один медленный запрос + сразу checkAll() → алёрт LATENCY (если превышен порог). */
    @GetMapping("/simulate-slow-immediate")
    public String simulateSlowImmediate(@RequestParam(name = "ms", required = false) Long ms) {
        long delay = (ms != null ? ms : 2000L);
        simulateSlowInternal(delay);
        checkService.checkAll();
        return "Triggered slow HTTP request (" + delay + " ms) and monitoring checks";
    }

    /** Несколько медленных запросов + сразу checkAll() → уверенный LATENCY-алёрт. */
    @GetMapping("/simulate-latency-burst")
    public String simulateLatencyBurst(
            @RequestParam(name = "count", required = false, defaultValue = "20") int count,
            @RequestParam(name = "ms", required = false, defaultValue = "2000") long ms
    ) {
        simulateLatencyBurstInternal(count, ms);
        checkService.checkAll();
        return "Triggered " + count + " slow HTTP requests (" + ms + " ms) and monitoring checks";
    }

    // ───────────────────── ЦЕЛЕВЫЕ ЭНДПОИНТЫ ДЛЯ 4xx/5xx ─────────────────────

    @GetMapping("/target-4xx")
    public ResponseEntity<String> target4xx() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Forced 400 for monitoring test");
    }

    @GetMapping("/target-5xx")
    public ResponseEntity<String> target5xx() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Forced 500 for monitoring test");
    }

    /**
     * Медленный эндпоинт. Здесь уже БЕЗ RestTemplate – это обычный
     * контроллер, который попадает в http.server.requests и замеряется Micrometer'ом.
     */
    @GetMapping("/slow")
    public String slowEndpoint(@RequestParam(name = "ms", required = false) Long ms) throws InterruptedException {
        long delay = (ms != null ? ms : 2000L);
        log.info("Handling slow endpoint: {} ms", delay);
        Thread.sleep(delay);
        return "OK (slow endpoint, slept " + delay + " ms)";
    }
}