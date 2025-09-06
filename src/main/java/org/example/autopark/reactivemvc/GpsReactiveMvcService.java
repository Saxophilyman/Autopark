//package org.example.autopark.reactivemvc;
//
//import lombok.RequiredArgsConstructor;
//import org.example.autopark.GPS.GpsPointDto;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.time.ZoneOffset;
//
//@Service
//@RequiredArgsConstructor
//public class GpsReactiveMvcService {
//    private final GpsPointReactiveRepository repo;
//
//    public Flux<GpsPointDto> playback(Long vehicleId, Instant from, Instant to, long msDelay) {
//        Instant f = (from != null ? from : Instant.EPOCH);
//        Instant t = (to   != null ? to   : Instant.now());
//
//        return repo.streamByVehicleIdBetween(vehicleId, f, t)
//                .delayElements(Duration.ofMillis(msDelay))
//                .map(p -> {
//                    var dto = new GpsPointDto();
//                    dto.setGpsPointId(p.getId());
//                    dto.setVehicleId(String.valueOf(p.getVehicleId()));
//                    dto.setLatitude(p.getLatitude());
//                    dto.setLongitude(p.getLongitude());
//                    // Отдаём UTC — при желании можно сдвигать в зону предприятия
//                    dto.setTimestamp(p.getTimestamp().atOffset(ZoneOffset.UTC).toLocalDateTime());
//                    return dto;
//                });
//    }
//}
