package org.example.autopark.reactive;

import lombok.RequiredArgsConstructor;
import org.example.autopark.gps.GpsPointDto;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;

@RestController
@Profile("reactive")
@RequestMapping("/api/reactive/gps")
@RequiredArgsConstructor
public class GpsReactiveController {

    private final GpsReactiveService service;


    @GetMapping(value = "/playback/{vehicleId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<GpsPointDto>> playback(
            @PathVariable Long vehicleId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "1000") long ms
    ) {
        return service.playback(vehicleId, from, to, ms)
                .map(dto -> ServerSentEvent.<GpsPointDto>builder()
                        .event("gps-point")
                        .id(dto.getGpsPointId() == null ? null : dto.getGpsPointId().toString())
                        .data(dto)
                        .build());
    }
}
