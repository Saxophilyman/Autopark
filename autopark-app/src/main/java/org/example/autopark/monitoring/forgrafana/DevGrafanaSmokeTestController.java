package org.example.autopark.monitoring.forgrafana;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.monitoring.forgrafana.SqlErrorCounter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/monitoring")
@ConditionalOnProperty(name = "monitoring.dev.smoke.enabled", havingValue = "true")
public class DevGrafanaSmokeTestController {

    private final SqlErrorCounter sqlErrorCounter;

    /**
     * Одна ссылка → накрутит метрики так, чтобы сработали:
     * - SQL_ERRORS
     * - HTTP_4XX_RATE (warning)
     * - HTTP_5XX_RATE (critical)
     * - LATENCY_P95 (critical)
     */
    @GetMapping("/smoke-test-grafana")
    public ResponseEntity<String> smokeTest() {

        // ВАЖНО: используем “чистый” RestTemplate без interceptor’ов,
        // чтобы не накручивать твои внутренние MonitoringMetrics.
        RestTemplate plain = new RestTemplate();

        int sqlIncrements = 3;

        // Чем больше, тем стабильнее пробивает процентные пороги
        int burst4xx = 200;
        int burst5xx = 80;

        // Для p95 важно много “медленных” запросов
        int slowCount = 40;
        long slowMs = 1200;

        // 1) SQL (через Counter)
        for (int i = 0; i < sqlIncrements; i++) {
            sqlErrorCounter.inc();
        }

        // 2) 4xx/5xx — реальные запросы к локальным DEV endpoint’ам (это поднимет server metrics)
        hitMany(plain, "http://localhost:8080/dev/monitoring/target-4xx", burst4xx);
        hitMany(plain, "http://localhost:8080/dev/monitoring/target-5xx", burst5xx);

        // 3) latency — пачка медленных запросов
        hitSlowParallel(plain, "http://localhost:8080/dev/monitoring/slow?ms=" + slowMs, slowCount, 10);

        String msg = """
                OK. Grafana smoke test triggered.

                Generated:
                - SQL counter +%d
                - 4xx requests: %d
                - 5xx requests: %d
                - slow requests: %d x %dms

                Далее:
                - подожди ~1–2 минуты (правила Grafana считают окно/for)
                - в Telegram должны прийти FIRING
                - затем RESOLVED (после нормализации)
                """.formatted(sqlIncrements, burst4xx, burst5xx, slowCount, slowMs);

        return ResponseEntity.ok(msg);
    }

    // Эти endpoint’ы — “мишени” (нужны для smoke-test)

    @GetMapping("/target-4xx")
    public ResponseEntity<String> target4xx() {
        return ResponseEntity.badRequest().body("Forced 400 for Grafana smoke test");
    }

    @GetMapping("/target-5xx")
    public ResponseEntity<String> target5xx() {
        return ResponseEntity.status(500).body("Forced 500 for Grafana smoke test");
    }

    @GetMapping("/slow")
    public String slow(@RequestParam(name = "ms", required = false, defaultValue = "1200") long ms)
            throws InterruptedException {
        Thread.sleep(ms);
        return "OK (slept " + ms + "ms)";
    }

    // ---- helpers ----

    private void hitMany(RestTemplate rt, String url, int count) {
        for (int i = 0; i < count; i++) {
            try {
                rt.getForEntity(url, String.class);
            } catch (RestClientException ignored) {
                // 4xx/5xx ожидаемо
            }
        }
    }

    private void hitSlowParallel(RestTemplate rt, String url, int count, int threads) {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            tasks.add(() -> {
                try {
                    rt.getForEntity(url, String.class);
                } catch (RestClientException ignored) {}
                return null;
            });
        }

        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdown();
        }
    }
}
