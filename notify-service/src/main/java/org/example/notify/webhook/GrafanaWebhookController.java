package org.example.notify.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(value = "notify.features.grafana-webhook.enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/api/notify/grafana")
public class GrafanaWebhookController {

    private final GrafanaWebhookService service;

    @Value("${internal.api.token:dev-token}")
    private String internalToken;

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody String payload
    ) {
        if (token == null || !internalToken.equals(token)) {
            log.warn("Grafana webhook rejected: invalid X-Internal-Token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Grafana webhook received ({} bytes)", payload.length());
        service.handle(payload);
        return ResponseEntity.ok().build();
    }
}
