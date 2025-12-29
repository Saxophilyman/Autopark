package org.example.autopark.reactivemvc;//package org.example.autopark.reactivemvc;
//
//import lombok.RequiredArgsConstructor;
//import org.example.autopark.GPS.GpsPointDto;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;
//
//import java.time.Instant;
//
//@RestController
//@RequestMapping("/api/reactive/gps")
//@RequiredArgsConstructor
//public class GpsReactiveController {
//    private final GpsReactiveMvcService service;
//
//    // SSE
//    @GetMapping(value = "/playback/{vehicleId}",
//            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<GpsPointDto> playback(
//            @PathVariable Long vehicleId,
//            @RequestParam(required = false) Instant from,
//            @RequestParam(required = false) Instant to,
//            @RequestParam(defaultValue = "1000") long ms) {
//        return service.playback(vehicleId, from, to, ms);
//    }
//}
