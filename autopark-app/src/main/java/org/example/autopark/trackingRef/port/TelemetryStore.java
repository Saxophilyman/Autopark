package org.example.autopark.trackingRef.port;


import org.example.autopark.trackingRef.model.TelemetrySeries;
import org.example.autopark.trackingRef.model.VehicleRef;

/**
 * Граница добавления телеметрических наблюдений автомобиля.
 */
public interface TelemetryStore {

    void append(VehicleRef vehicle, TelemetrySeries telemetry);
}
