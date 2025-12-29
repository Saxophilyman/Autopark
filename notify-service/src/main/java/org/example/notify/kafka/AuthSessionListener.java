package org.example.notify.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.AuthSessionEvent;
import org.example.notify.session.SessionStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthSessionListener {

    private final SessionStore sessions;

    @KafkaListener(
            topics = "auth.sessions",
            groupId = "notify-service",
            properties = {
                    "spring.json.value.default.type=org.example.events.AuthSessionEvent",
                    "spring.json.trusted.packages=org.example.events"
            }
    )
    public void onAuth(AuthSessionEvent evt) {
        log.info("auth.sessions: {}", evt);
        switch (evt.action()) {
            case LOGIN  -> sessions.login(evt.managerId(), evt.telegramChatId());
            case LOGOUT -> sessions.logout(evt.managerId());
        }
    }
}
