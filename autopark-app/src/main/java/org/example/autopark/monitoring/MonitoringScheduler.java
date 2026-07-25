package org.example.autopark.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.monitoring.config.MonitoringProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик, который раз в monitoring.check-interval-ms
 * запускает проверки мониторинга.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringScheduler {
    private final MonitoringCheckService checkService;
    private final MonitoringProperties props;

    /**
     * fixedDelayString — задержка между ОКОНЧАНИЕМ предыдущей проверки
     * и НАЧАЛОМ следующей.
     * <p>
     * Значение берём из monitoring.check-interval-ms (с дефолтом 60000 мс).
     */
    @Scheduled(fixedDelayString = "${monitoring.check-interval-ms:60000}")
    public void runChecks() {
        if (!props.isEnabled()) {
            log.trace("Monitoring disabled, skip scheduled checks");
            return;
        }
        try {
            checkService.checkAll();
        } catch (Exception e) {
            // Очень важно: мониторинг не должен валить приложение.
            log.warn("Monitoring check failed", e);
        }
    }
}
