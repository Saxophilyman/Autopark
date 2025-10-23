package org.example.autopark.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicsConfig {
    @Bean
    public NewTopic vehicleEvents() { return new NewTopic("vehicle.events", 3, (short) 1); }
    @Bean
    public NewTopic authSessions()  { return new NewTopic("auth.sessions", 3, (short) 1); }
}
