// src/main/java/org/example/autopark/reactive/DemoSseController.java
package org.example.autopark.reactive;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;

@RestController
@Profile("reactive")
public class DemoSseController {

    // Простая демка: каждую секунду шлём число
    @GetMapping(value = "/api/reactive/demo/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> demo() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> ServerSentEvent.<String>builder()
                        .id(Long.toString(i))
                        .event("tick")
                        .data("tick #" + i + " @ " + Instant.now())
                        .build());
    }
}
