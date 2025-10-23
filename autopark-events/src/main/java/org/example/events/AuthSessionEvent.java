// src/main/java/org/example/events/AuthSessionEvent.java
package org.example.events;

import java.time.Instant;

public record AuthSessionEvent(
        Long managerId,
        Long telegramChatId,
        Action action,
        Instant occurredAt
) {
    public enum Action { LOGIN, LOGOUT }
}
