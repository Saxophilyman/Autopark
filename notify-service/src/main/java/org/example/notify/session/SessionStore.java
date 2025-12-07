// src/main/java/org/example/notify/session/SessionStore.java
package org.example.notify.session;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SessionStore {
    private final ConcurrentMap<Long, Long> managerToChat = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Long> chatToManager = new ConcurrentHashMap<>();

    public void login(Long managerId, Long chatId) {
        managerToChat.put(managerId, chatId);
        chatToManager.put(chatId, managerId);
    }

    public void logout(Long managerId) {
        Long chatId = managerToChat.remove(managerId);
        if (chatId != null) {
            chatToManager.remove(chatId);
        }
    }

    public Optional<Long> findChatId(Long managerId) {
        return Optional.ofNullable(managerToChat.get(managerId));
    }

    public Optional<Long> findManagerId(Long chatId) {
        return Optional.ofNullable(chatToManager.get(chatId));
    }
}



