package org.example.autopark.telegrambot.tg;

import lombok.RequiredArgsConstructor;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify/tg")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.telegram.bind.enabled", havingValue = "true")
@Profile("!reactive")
public class TelegramBindController {

    private final TelegramChatLinkRepository repo;

    // Привязка Telegram-чата к ТЕКУЩЕМУ менеджеру
    // POST /api/notify/tg/bind?managerId=1&chatId=123
    @PostMapping("/bind")
    public ResponseEntity<Void> bind(@CurrentManagerId Long managerId, @RequestParam Long chatId) {
        repo.save(new TelegramChatLink(managerId, chatId));
        return ResponseEntity.ok().build();
    }

    // Отвязка Telegram-чата от ТЕКУЩЕГО менеджера
    // DELETE /api/notify/tg/bind?managerId=1
    @DeleteMapping("/bind")
    public ResponseEntity<Void> unbind(@CurrentManagerId Long managerId) {
        repo.deleteById(managerId);
        return ResponseEntity.ok().build();
    }

    // Получить chatId для ТЕКУЩЕГО менеджера
    // GET /api/notify/tg/chatId?managerId=1  -> 200: Long  / 404
    @GetMapping("/chatId")
    public ResponseEntity<Long> getChatId(@CurrentManagerId Long managerId) {
        return repo.findById(managerId)
                .map(link -> ResponseEntity.ok(link.getChatId()))
                .orElse(ResponseEntity.notFound().build());
    }
}
