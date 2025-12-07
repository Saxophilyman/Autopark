package org.example.autopark.integrationTest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.autopark.integrationTest.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleEventKafkaIT extends IntegrationTestBase {

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
    );

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry r) {
        // Адрес брокера для spring-kafka
        r.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        // ВКЛЮЧАЕМ Kafka только для этого теста
        r.add("app.kafka.enabled", () -> "true");
    }

    @Test
    @Sql({"/sql/enterprise_seed.sql", "/sql/manager_seed.sql"})
    void publishes_event_on_create() {
        // 1. Готовим запрос на создание автомобиля
        var req = new VehicleCreate(
                null,
                "Kalina",
                "Ф001АИ",
                500_000,
                2015,
                new EnterpriseRef(1L)   // enterprise_id = 1 из sql-файла
        );

        // 2. Делаем HTTP-запрос к реальному контроллеру
        var response = http.postForEntity(
                url("/api/managers/1/vehicles"),
                req,
                VehicleDto.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        var body = Objects.requireNonNull(response.getBody());
        assertThat(body.getVehicleId()).isNotNull();

        // 3. Kafka-consumer: читаем запись из топика
        try (var consumer = new KafkaConsumer<String, String>(consumerProps())) {
            consumer.subscribe(List.of("vehicle.events"));

            var records = consumer.poll(Duration.ofSeconds(10));
            assertThat(records.count())
                    .as("В топике vehicle.events должно быть хотя бы одно событие")
                    .isGreaterThan(0);

            var payload = records.iterator().next().value();

            // 4. Проверяем полезную нагрузку
            // В VehicleEvent теперь нет числового id, но там есть:
            // action, licensePlate, vehicleName, guid и пр.
            assertThat(payload)
                    .contains("CREATED")   // enum action
                    .contains("Ф001АИ")    // номер
                    .contains("Kalina");   // имя
        }
    }

    private Properties consumerProps() {
        var p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        p.put(ConsumerConfig.GROUP_ID_CONFIG, "it-consumer");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return p;
    }

    // ===== ЛОКАЛЬНЫЕ DTO ТОЛЬКО ДЛЯ IT-ТЕСТА =====

    // Тело запроса к контроллеру (формат как у VehicleDTO)
    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class VehicleCreate {
        Long vehicleId;
        String vehicleName;
        String licensePlate;
        Integer vehicleCost;
        Integer vehicleYearOfRelease;
        EnterpriseRef enterprise;
    }

    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EnterpriseRef {
        Long enterpriseId;
    }

    // Ответ от контроллера — нам важен только id и пара полей
    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class VehicleDto {
        Long vehicleId;
        String vehicleName;
        String licensePlate;
        Integer vehicleCost;
        Integer vehicleYearOfRelease;
    }
}
