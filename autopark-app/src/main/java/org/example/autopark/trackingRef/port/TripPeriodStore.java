package org.example.autopark.trackingRef.port;


import org.example.autopark.trackingRef.model.TripPeriod;
import org.example.autopark.trackingRef.model.VehicleRef;

/**
 * Граница хранения временных интервалов поездок.
 */
public interface TripPeriodStore {

    boolean overlaps(VehicleRef vehicle, TripPeriod period);

    Long save(VehicleRef vehicle, TripPeriod period);
}
