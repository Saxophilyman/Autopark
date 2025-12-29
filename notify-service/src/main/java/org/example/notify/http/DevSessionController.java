// src/main/java/org/example/notify/http/DevSessionController.java
package org.example.notify.http;

import lombok.RequiredArgsConstructor;
import org.example.notify.session.SessionStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev/session")
@RequiredArgsConstructor
public class DevSessionController {
    private final SessionStore store;

    // Пример: POST http://localhost:8082/dev/session/login?managerId=1&chatId=123456789
    @PostMapping("/login")
    public String login(@RequestParam Long managerId, @RequestParam Long chatId) {
        store.login(managerId, chatId);
        return "ok";
    }

    @PostMapping("/logout")
    public String logout(@RequestParam Long managerId) {
        store.logout(managerId);
        return "ok";
    }
}
