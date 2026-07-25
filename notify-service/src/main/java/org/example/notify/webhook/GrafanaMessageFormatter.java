package org.example.notify.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GrafanaMessageFormatter {

    public List<String> formatMessages(JsonNode payload) {
        List<String> result = new ArrayList<>();

        JsonNode alerts = payload.path("alerts");
        if (!alerts.isArray() || alerts.isEmpty()) {
            // fallback: бывает, если формат другой
            result.add(formatSingle(payload, payload));
            return result;
        }

        for (JsonNode alert : alerts) {
            result.add(formatSingle(payload, alert));
        }
        return result;
    }

    private String formatSingle(JsonNode root, JsonNode alert) {
        String status = text(alert, "status", text(root, "status", "unknown")).toUpperCase();

        JsonNode labels = alert.path("labels");
        JsonNode ann = alert.path("annotations");

        String system = text(labels, "system", "autopark");
        String type = text(labels, "type", text(labels, "alertname", "unknown"));
        String severity = normalizeSeverity(text(labels, "severity", "unknown"));

        String job = text(labels, "job", "");
        String instance = text(labels, "instance", "");

        String window = text(ann, "window", "");
        String threshold = text(ann, "threshold", "");
        String value = text(ann, "value", "");
        String description = text(ann, "description", text(ann, "summary", ""));

        // собираем компактно: только если поле есть
        List<String> lines = new ArrayList<>();
        lines.add("Мониторинг " + capitalize(system));
        lines.add("Статус: " + status);
        lines.add("Тип: " + type);
        lines.add("Серьёзность: " + severity);

        String source = buildSource(job, instance);
        if (!source.isBlank()) {
            lines.add("Источник: " + source);
        }
        if (!window.isBlank()) {
            lines.add("Окно: " + window);
        }
        if (!threshold.isBlank()) {
            lines.add("Порог: " + threshold);
        }
        if (!value.isBlank()) {
            lines.add("Значение: " + value);
        }
        if (!description.isBlank()) {
            lines.add("Причина: " + description);
        }

        return String.join("\n", lines);
    }

    private String buildSource(String job, String instance) {
        StringBuilder sb = new StringBuilder();
        if (!job.isBlank()) sb.append("job=").append(job);
        if (!instance.isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("instance=").append(instance);
        }
        return sb.toString();
    }

    private String normalizeSeverity(String raw) {
        String s = raw == null ? "" : raw.trim().toLowerCase();
        return switch (s) {
            case "critical" -> "CRITICAL";
            case "warning" -> "WARNING";
            default -> raw == null ? "UNKNOWN" : raw.toUpperCase();
        };
    }

    private String text(JsonNode node, String field, String def) {
        if (node == null || node.isMissingNode() || node.isNull()) return def;
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || v.isMissingNode()) return def;
        return v.asText(def);
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}