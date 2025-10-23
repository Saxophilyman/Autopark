package org.example.autopark.telegramBot.tg;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify/tg")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.telegram.bind.enabled", havingValue = "true")
public class TelegramBindController {

    private final TelegramChatLinkRepository repo;

    // POST /api/notify/tg/bind?managerId=1&chatId=123
    @PostMapping("/bind")
    public ResponseEntity<Void> bind(@RequestParam Long managerId, @RequestParam Long chatId) {
        repo.save(new TelegramChatLink(managerId, chatId));
        return ResponseEntity.ok().build();
    }


    // DELETE /api/notify/tg/bind?managerId=1
    @DeleteMapping("/bind")
    public ResponseEntity<Void> unbind(@RequestParam Long managerId) {
        repo.deleteById(managerId);
        return ResponseEntity.ok().build();
    }

    // GET /api/notify/tg/chatId?managerId=1  -> 200: Long  / 404
    @GetMapping("/chatId")
    public ResponseEntity<Long> getChatId(@RequestParam Long managerId) {
        return repo.findById(managerId)
                .map(link -> ResponseEntity.ok(link.getChatId()))
                .orElse(ResponseEntity.notFound().build());
    }
}
