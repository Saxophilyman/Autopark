package org.example.autopark.gps;

import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Profile("!reactive")
@RequestMapping("/api/managers")
@Tag(
        name = "GPS tracks (Manager API)",
        description = "Получение GPS-треков автомобилей в JSON или GeoJSON формате"
)
public class ApiGpsPointControllers {
    private final GpsPointsService gpsPointsService;

    @Autowired
    public ApiGpsPointControllers(GpsPointsService gpsPointsService) {
        this.gpsPointsService = gpsPointsService;
    }

    @GetMapping("/track")
    @Operation(
            summary = "Получить GPS-трек автомобиля",
            description = """
                    Возвращает GPS-трек автомобиля за указанный интервал времени.
                    Время передаётся в часовом поясе предприятия в формате ISO_LOCAL_DATE_TIME
                    (например: 2025-12-05T10:00:00).
                    Параметр format определяет формат ответа: "json" (список точек) или "geojson".
                    """
    )
    public ResponseEntity<TrackResponse> getTrack(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestParam Long vehicleId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "json") String format
    ) {
        TrackResponse response = gpsPointsService.getTrack(vehicleId, start, end, format);
        return ResponseEntity.ok(response);
    }
}
