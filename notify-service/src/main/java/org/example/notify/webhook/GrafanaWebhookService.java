package org.example.notify.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notify.telegram.TelegramSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "notify.features.grafana-webhook.enabled", havingValue = "true", matchIfMissing = true)
public class GrafanaWebhookService {

    private final ObjectMapper objectMapper;
    private final TelegramSender telegramSender;
    private final GrafanaMessageFormatter formatter;

    public void handle(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            List<String> messages = formatter.formatMessages(root);
            for (String msg : messages) {
                telegramSender.sendToAlertChat(msg);
            }

        } catch (Exception e) {
            log.error("Failed to parse/handle Grafana webhook payload", e);
        }
    }
}
