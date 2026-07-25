package org.example.autopark.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaTopicsConfig {
    @Bean
    public NewTopic vehicleEvents() { return new NewTopic("vehicle.events", 3, (short) 1); }
    @Bean
    public NewTopic authSessions()  { return new NewTopic("auth.sessions", 3, (short) 1); }
    @Bean
    public NewTopic monitoringAlerts()  { return new NewTopic("monitoring.alerts", 3, (short) 1); }
}
