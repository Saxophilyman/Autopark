package org.example.autopark.nplusone;

import lombok.RequiredArgsConstructor;
import org.example.autopark.repository.VehicleRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Profile("!reactive")
@RequestMapping("/demo/nplusone")
public class NPlusOneDemoController {

    private final NPlusOneDemoService demo;

    @GetMapping
    public ResponseEntity<Map<String, Object>> run() {
        // Вернём краткую сводку; подробности смотри в логах по меткам [A-N+1], [B-FETCH], ...
        return ResponseEntity.ok(demo.runAll());
    }
}
