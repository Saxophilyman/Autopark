package org.example.autopark.GPS;

import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/managers")
public class ApiManControllers {
    private final GpsPointsService gpsPointsService;

    @Autowired
    public ApiManControllers(GpsPointsService gpsPointsService) {
        this.gpsPointsService = gpsPointsService;
    }

    @GetMapping("/track")
    public ResponseEntity<?> getTrack(
            @CurrentManagerId
            @RequestParam Long vehicleId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "json") String format
    ) {
        return ResponseEntity.ok(gpsPointsService.getTrack(vehicleId, start, end, format));
    }
}
//    /**
//     * Получение всех точек
//     */
//    @GetMapping("/all")
//    public ResponseEntity<?> getAllPoints(
//            @RequestParam(defaultValue = "json") String format,
//            @RequestParam(defaultValue = "UTC") String timezone) {
//        return ResponseEntity.ok(gpsPointsService.getAllTrackPoints(format, timezone));
//    }

    /**
     * Получение точек конкретного автомобиля за период
     */
//    @GetMapping("/{vehicleId}")
//    public ResponseEntity<?> getTrack(
//            @CurrentManagerId
//            @PathVariable Long vehicleId,
//            @RequestParam String start,
//            @RequestParam String end,
//            @RequestParam(defaultValue = "UTC") String timezone,
//            @RequestParam(defaultValue = "json") String format) {
//
//        List<GpsPoint> points = gpsPointsService.getTrackPoints(vehicleId, LocalDateTime.parse(start), LocalDateTime.parse(end), timezone);
//
//        if ("geojson".equalsIgnoreCase(format)) {
//            return ResponseEntity.ok(gpsPointsService.convertToGeoJSON(points, ZoneId.of(timezone)));
//        }
//        return ResponseEntity.ok(points);  // Возвращаем стандартный JSON без изменений
//    }


