package org.example.autopark.reactive;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Profile("reactive")
public class ReactiveHelloController {

    @GetMapping("/reactive/hello")
    public Mono<String> hello() {
        return Mono.just("Привет из WebFlux!");
    }
}
