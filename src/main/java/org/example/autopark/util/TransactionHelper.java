package org.example.autopark.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.function.Supplier;

@Slf4j
@Component
@Profile("!reactive")
@RequiredArgsConstructor
public class TransactionHelper {

    private final TransactionTemplate transactionTemplate;

    /**
     * Выполняет логику в рамках транзакции. В случае ошибки будет rollback.
     */
    public <T> T executeInTransaction(Supplier<T> action) {
        return transactionTemplate.execute(status -> action.get());
    }

    /**
     * Выполняет void-логику в транзакции.
     */
    public void runInTransaction(Runnable action) {
        transactionTemplate.executeWithoutResult(status -> {
            log.info(">> Транзакция начата");
            try {
                action.run();
                log.info("<< Транзакция завершена успешно");
            } catch (Exception e) {
                log.warn("!! Откат транзакции из-за ошибки: {}", e.getMessage());
                throw e;
            }
        });
    }

    public void runInTransactionWithIOException(IOExceptionRunnable action) throws IOException {
        try {
            runInTransaction(() -> {
                try {
                    action.run();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }

    @FunctionalInterface
    public interface IOExceptionRunnable {
        void run() throws IOException;
    }


}
