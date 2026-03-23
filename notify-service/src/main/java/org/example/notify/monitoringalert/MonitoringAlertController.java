package org.example.notify.monitoringalert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.MonitoringEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/monitoring")
@RequiredArgsConstructor
public class MonitoringAlertController {

    private final MonitoringAlertService alerts;

    @PostMapping({"/event", "/alert"})
    public ResponseEntity<Void> receive(@RequestBody MonitoringEvent evt) {
        // В проде тут обычно проверяют токен/подпись, чтобы внешние не могли слать алерты.
        // Для учебного проекта можно оставить так, либо позже добавить простой X-Token.
        log.info("HTTP monitoring event received: {}", evt);
        alerts.handle(evt);
        return ResponseEntity.accepted().build();
    }
}
