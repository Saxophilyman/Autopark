package org.example.autopark.scalability;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rps")
@RequiredArgsConstructor
public class RpsController {
    private static final byte[] OK = "{\"status\":\"OK\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    /** потолок узла: сеть/ядра/треды, без JSON/БД */
    @GetMapping(value = "/pure", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> pure() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(OK.length)    // фиксируем Content-Length, без chunked
                .body(OK);
    }

    /**
     * цена сериализации Jackson, без БД
     */
    @GetMapping("/json")
    public PingDto json() {
        return new PingDto("OK", System.currentTimeMillis());
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class PingDto {
        private String status;
        private long ts;
    }
}

