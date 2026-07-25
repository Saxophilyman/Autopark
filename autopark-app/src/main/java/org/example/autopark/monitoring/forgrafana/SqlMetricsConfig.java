package org.example.autopark.monitoring.forgrafana;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SqlMetricsConfig {

    @Bean
    public Counter autoparkSqlErrorsTotal(MeterRegistry registry) {
        return Counter.builder("autopark_sql_errors_total")
                .description("Total count of SQL/DB errors in autopark")
                .register(registry);
    }
}
