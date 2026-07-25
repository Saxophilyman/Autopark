package org.example.autopark.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.events.MonitoringEvent;
import org.example.events.VehicleEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaProducerConfig {
    private final KafkaProperties kafkaProperties;
    private final SslBundles sslBundles; // если не сконфигурированы — просто не будут использоваться

    //Object будет вместо vehicleEventProducerFactory и monitoringEventProducerFactory и д.р.
    private Map<String, Object> baseProducerProps() {
        // Базовые настройки берём из spring.kafka.* (bootstrap-servers и т.д.)
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(sslBundles));
        // Ключ — строка
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Значение — наш какой-то event, сериализуем в JSON
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Чтобы не пихать тип в заголовки (нам не нужно)
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    private <T> ProducerFactory<String, T> producerFactory() {
        return new DefaultKafkaProducerFactory<>(baseProducerProps());
    }

    @Bean
    public KafkaTemplate<String, VehicleEvent> vehicleEventKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    //Продюсер для MonitoringEvent
    @Bean
    public KafkaTemplate<String, MonitoringEvent> monitoringEventKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
