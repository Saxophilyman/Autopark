package org.example.autopark.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
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
        transactionTemplate.executeWithoutResult(status -> action.run());
    }

}
