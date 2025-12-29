package org.example.autopark.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.VehicleEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class VehicleEventPublisher {

    private static final String TOPIC = "vehicle.events";

    private final KafkaTemplate<String, VehicleEvent> kafka;

    public void publish(VehicleEvent evt) {
        kafka.send(TOPIC, evt.vehicleGuid().toString(), evt)
                .whenComplete((SendResult<String, VehicleEvent> meta, Throwable ex) -> {
                    if (ex != null) {
                        log.error("Kafka send failed", ex);
                    } else {
                        log.info("sent topic={} part={} offset={} key={}",
                                meta.getRecordMetadata().topic(),
                                meta.getRecordMetadata().partition(),
                                meta.getRecordMetadata().offset(),
                                evt.vehicleGuid());
                    }
                });
    }
}
