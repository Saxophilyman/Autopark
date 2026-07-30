package org.example.autopark.trackingRef.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.example.autopark.entity.Vehicle;

import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trackingRef.model.TripPeriod;
import org.example.autopark.trackingRef.model.VehicleRef;
import org.example.autopark.trackingRef.port.TripPeriodStore;
import org.example.autopark.trip.Trip;
import org.example.autopark.trip.TripRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!reactive")
@RequiredArgsConstructor
public class JpaTripPeriodStoreAdapter implements TripPeriodStore {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;

    @Override
    public boolean overlaps(VehicleRef vehicle, TripPeriod period) {
        return tripRepository.existsByVehicleAndPeriodOverlap(
                vehicle.id(),
                period.start(),
                period.end()
        );
    }

    @Override
    public Long save(VehicleRef vehicleRef, TripPeriod period) {
        Vehicle vehicle = vehicleRepository.getReferenceById(vehicleRef.id());

        Trip trip = new Trip(
                vehicle,
                period.start(),
                period.end()
        );

        return tripRepository.save(trip).getId();
    }
}
