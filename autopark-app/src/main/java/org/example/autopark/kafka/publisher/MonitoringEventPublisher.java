package org.example.autopark.kafka.publisher;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.MonitoringEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class MonitoringEventPublisher {
    private static final String TOPIC = "monitoring.alerts";
    private final KafkaTemplate<String, MonitoringEvent> kafka;

    public void publish(MonitoringEvent evt) {
        // TODO обращу внимание: ключ можно сделать, например, по type, чтобы одинаковые типы шли в схожие партиции
        String key = evt.type().name();

        kafka.send(TOPIC, key, evt)
                .whenComplete((SendResult<String, MonitoringEvent> meta, Throwable ex) -> {
                    if (ex != null) {
                        log.error("Kafka send failed (monitoring)", ex);
                    } else {
                        log.info("sent monitoring topic={} part={} offset={} key={}",
                                meta.getRecordMetadata().topic(),
                                meta.getRecordMetadata().partition(),
                                meta.getRecordMetadata().offset(),
                                key);
                    }
                });

    }
}
