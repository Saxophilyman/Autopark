package org.example.autopark.monitoring.forgrafana;

import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqlErrorCounter {

    private final Counter autoparkSqlErrorsTotal;

    public void inc() {
        autoparkSqlErrorsTotal.increment();
    }
}
