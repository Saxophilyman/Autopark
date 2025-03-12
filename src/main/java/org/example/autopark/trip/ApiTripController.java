package org.example.autopark.trip;

import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
}