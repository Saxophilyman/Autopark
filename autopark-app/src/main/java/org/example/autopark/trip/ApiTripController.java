package org.example.autopark.trip;

import org.example.autopark.gps.GpsPointDto;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Profile("!reactive")
@RequestMapping("/api/managers")
@Tag(
        name = "Trips (Manager API)",
        description = "Поездки автомобилей и GPS-треки по поездкам"
)
public class ApiTripController {
    private final TripService tripService;

    @Autowired
    public ApiTripController(TripService tripService) {
        this.tripService = tripService;
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/trips
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/trips")
    @Operation(
            summary = "Получить GPS-точки поездок за период",
            description = """
                    Возвращает все GPS-точки, относящиеся к поездкам автомобиля за указанный период.
                    Даты передаются в часовом поясе предприятия.
                    Формат:
                    • yyyy-MM-dd — берётся целый день,
                    • или yyyy-MM-dd'T'HH:mm:ss — конкретное время.
                    """
    )
    public ResponseEntity<List<GpsPointDto>> getTrips(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestParam Long vehicleId,
            @RequestParam String startTripDate,
            @RequestParam String endTripDate
    ) {
        List<GpsPointDto> points = tripService.getTrips(vehicleId, startTripDate, endTripDate);
        return ResponseEntity.ok(points);
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/getOnlyTrips
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/getOnlyTrips")
    @Operation(
            summary = "Получить список поездок за период",
            description = """
                    Возвращает список поездок автомобиля (без GPS-точек),
                    с временем и адресами начала/конца в часовом поясе предприятия.
                    Используется для API/интеграций.
                    """
    )
    public ResponseEntity<List<TripDTO>> getOnlyTrips(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestParam Long vehicleId,
            @RequestParam String startTripDate,
            @RequestParam String endTripDate
    ){
        List<TripDTO> trips = tripService.getOnlyTrips(vehicleId, startTripDate, endTripDate);
        return ResponseEntity.ok(trips);
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/getOnlyTripsUI
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/getOnlyTripsUI")
    @Operation(
            summary = "Список поездок для UI",
            description = """
                    Возвращает список поездок автомобиля за период.
                    Используется клиентским JS для отображения таблицы поездок и треков на карте.
                    """
    )
    public List<TripDTO> getOnlyTripsUI(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestParam Long vehicleId,
            @RequestParam String startTripDate,
            @RequestParam String endTripDate
    ){
        return tripService.getOnlyTrips(vehicleId, startTripDate, endTripDate);
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/trip-track
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/trip-track")
    @Operation(
            summary = "Получить трек конкретной поездки",
            description = "Возвращает GPS-точки для одной поездки по её ID, в локальном времени предприятия."
    )
    public ResponseEntity<List<GpsPointDto>> getTripTrack(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestParam Long tripId
    ) {
        List<GpsPointDto> gpsPoints = tripService.getTrackByTripId(tripId);
        return ResponseEntity.ok(gpsPoints);
    }
}
