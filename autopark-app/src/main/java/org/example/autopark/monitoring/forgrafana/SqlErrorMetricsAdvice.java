package org.example.autopark.monitoring.forgrafana;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SqlErrorMetricsAdvice {

    private final SqlErrorCounter counter;

    // Подстрой пакет под свой (repositories)
    @AfterThrowing(
            pointcut = "execution(* org.example.autopark..repository..*(..)) || execution(* org.example.autopark..repositories..*(..))",
            throwing = "ex"
    )
    public void onRepositoryError(Throwable ex) {
        if (isDbError(ex)) {
            counter.inc();
            log.debug("SQL error counted: {}", ex.getClass().getSimpleName());
        }
    }

    private boolean isDbError(Throwable ex) {
        // Достаточно для старта: всё, что DataAccessException или её причина
        if (ex instanceof DataAccessException) {
            return true;
        }
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof DataAccessException) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }
}
