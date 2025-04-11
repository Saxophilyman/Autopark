package org.example.autopark.trip;

import org.example.autopark.GPS.GpsPointDto;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("api/managers")
public class ApiTripController {
    private final TripService tripService;

    @Autowired
    public ApiTripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("trips")
    public ResponseEntity<?> getTrips(
            @CurrentManagerId
            @RequestParam Long vehicleId,
            @RequestParam String startTripDate,
            @RequestParam String endTripDate
    ) {
        return ResponseEntity.ok(tripService.getTrips(vehicleId, startTripDate, endTripDate));
    }

    @GetMapping("getOnlyTrips")
    public ResponseEntity<?> getOnlyTrips(
            @CurrentManagerId
            @RequestParam Long vehicleId,
            @RequestParam String startTripDate,
            @RequestParam String endTripDate
    ){
        return ResponseEntity.ok(tripService.getOnlyTrips(vehicleId, startTripDate, endTripDate));
    }

    @GetMapping("getOnlyTripsUI")
    public List<TripDTO> getOnlyTripsUI(
            @CurrentManagerId
            @RequestParam Long vehicleId,
            @RequestParam String startTripDate,
            @RequestParam String endTripDate
    ){
        return tripService.getOnlyTrips(vehicleId, startTripDate, endTripDate);
    }

    //тоже для UI
    //проверка на соответствие предприятию
    @GetMapping("trip-track")
    public ResponseEntity<List<GpsPointDto>> getTripTrack(
            @CurrentManagerId
            @RequestParam Long tripId) {
        List<GpsPointDto> gpsPoints = tripService.getTrackByTripId(tripId);
        return ResponseEntity.ok(gpsPoints);
    }


}