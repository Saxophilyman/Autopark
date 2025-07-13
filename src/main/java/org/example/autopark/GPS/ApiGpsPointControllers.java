package org.example.autopark.GPS;

import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/managers")
public class ApiGpsPointControllers {
    private final GpsPointsService gpsPointsService;

    @Autowired
    public ApiGpsPointControllers(GpsPointsService gpsPointsService) {
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
