package org.example.autopark.trip;

import org.example.autopark.GPS.GpsPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TripService {
    private final TripRepository tripRepository;

    @Autowired
    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }


    public List<GpsPoint> getTrips(Long vehicleId, String start, String end) {

      return new ArrayList<>(); // пока как заглушка
    }
}
