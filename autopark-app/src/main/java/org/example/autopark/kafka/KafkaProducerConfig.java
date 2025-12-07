package org.example.autopark.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.events.VehicleEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, VehicleEvent> vehicleEventProducerFactory(
            KafkaProperties kafkaProperties
    ) {
        // Базовые настройки берём из spring.kafka.* (bootstrap-servers и т.д.)
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());

        // Ключ — строка
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Значение — наш VehicleEvent, сериализуем в JSON
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Чтобы не пихать тип в заголовки (нам не нужно)
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, VehicleEvent> vehicleEventKafkaTemplate(
            ProducerFactory<String, VehicleEvent> vehicleEventProducerFactory
    ) {
        return new KafkaTemplate<>(vehicleEventProducerFactory);
    }
}
