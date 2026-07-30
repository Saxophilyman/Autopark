package org.example.autopark.tracking.application.register;


import org.example.autopark.trackingRef.application.register.RegisterVehicleMovementCommand;
import org.example.autopark.trackingRef.application.register.RegisterVehicleMovementService;
import org.example.autopark.trackingRef.application.register.RegisterVehicleMovementUseCase;
import org.example.autopark.trackingRef.model.TelemetryPoint;
import org.example.autopark.trackingRef.model.TelemetrySeries;
import org.example.autopark.trackingRef.model.TripPeriod;
import org.example.autopark.trackingRef.model.VehicleRef;
import org.example.autopark.trackingRef.port.TelemetryStore;
import org.example.autopark.trackingRef.port.TripPeriodStore;
import org.example.autopark.trackingRef.port.VehicleLookupPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Use-case тест: проверяет назначение сценария для проекта, а не внутренние
 * вызовы методов и не устройство Spring/JPA-адаптеров.
 */
class RegisterVehicleMovementUseCaseTest {

    @Test
    void registersTripPeriodAndTelemetryForExistingVehicle() {
        InMemoryVehicleLookup vehicles =
                new InMemoryVehicleLookup().addVehicle(10L, "A001AA");
        InMemoryTripPeriodStore trips = new InMemoryTripPeriodStore();
        InMemoryTelemetryStore telemetryStore =
                new InMemoryTelemetryStore();

        RegisterVehicleMovementUseCase useCase =
                new RegisterVehicleMovementService(
                        vehicles,
                        trips,
                        telemetryStore
                );

        TripPeriod period = new TripPeriod(
                Instant.parse("2026-04-10T10:00:00Z"),
                Instant.parse("2026-04-10T10:00:20Z")
        );

        TelemetrySeries telemetry = new TelemetrySeries(List.of(
                point("2026-04-10T10:00:00Z", 55.75, 37.61),
                point("2026-04-10T10:00:10Z", 55.76, 37.62),
                point("2026-04-10T10:00:20Z", 55.77, 37.63)
        ));

        Long tripId = useCase.execute(
                new RegisterVehicleMovementCommand(
                        10L,
                        period,
                        telemetry
                )
        );

        assertEquals(1L, tripId);
        assertEquals(1, trips.savedTrips.size());
        assertEquals(period, trips.savedTrips.get(0).period());
        assertEquals(10L, trips.savedTrips.get(0).vehicleId());

        assertEquals(3, telemetryStore.savedPoints.size());
        assertEquals(
                Instant.parse("2026-04-10T10:00:00Z"),
                telemetryStore.savedPoints.get(0).timestamp()
        );
        assertEquals(
                Instant.parse("2026-04-10T10:00:20Z"),
                telemetryStore.savedPoints.get(2).timestamp()
        );
    }

    @Test
    void overlappingTripDoesNotChangeProjectState() {
        InMemoryVehicleLookup vehicles =
                new InMemoryVehicleLookup().addVehicle(10L, "A001AA");
        InMemoryTripPeriodStore trips = new InMemoryTripPeriodStore();
        trips.overlap = true;
        InMemoryTelemetryStore telemetryStore =
                new InMemoryTelemetryStore();

        RegisterVehicleMovementUseCase useCase =
                new RegisterVehicleMovementService(
                        vehicles,
                        trips,
                        telemetryStore
                );

        TripPeriod period = new TripPeriod(
                Instant.parse("2026-04-10T10:00:00Z"),
                Instant.parse("2026-04-10T10:00:20Z")
        );

        TelemetrySeries telemetry = new TelemetrySeries(List.of(
                point("2026-04-10T10:00:00Z", 55.75, 37.61),
                point("2026-04-10T10:00:20Z", 55.77, 37.63)
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(
                        new RegisterVehicleMovementCommand(
                                10L,
                                period,
                                telemetry
                        )
                )
        );

        assertTrue(trips.savedTrips.isEmpty());
        assertTrue(telemetryStore.savedPoints.isEmpty());
    }

    @Test
    void telemetryOutsideTripPeriodDoesNotChangeProjectState() {
        InMemoryVehicleLookup vehicles =
                new InMemoryVehicleLookup().addVehicle(10L, "A001AA");
        InMemoryTripPeriodStore trips = new InMemoryTripPeriodStore();
        InMemoryTelemetryStore telemetryStore =
                new InMemoryTelemetryStore();

        RegisterVehicleMovementUseCase useCase =
                new RegisterVehicleMovementService(
                        vehicles,
                        trips,
                        telemetryStore
                );

        TripPeriod period = new TripPeriod(
                Instant.parse("2026-04-10T10:00:00Z"),
                Instant.parse("2026-04-10T10:00:20Z")
        );

        TelemetrySeries telemetry = new TelemetrySeries(List.of(
                point("2026-04-10T09:59:59Z", 55.75, 37.61),
                point("2026-04-10T10:00:20Z", 55.77, 37.63)
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(
                        new RegisterVehicleMovementCommand(
                                10L,
                                period,
                                telemetry
                        )
                )
        );

        assertTrue(trips.savedTrips.isEmpty());
        assertTrue(telemetryStore.savedPoints.isEmpty());
    }

    private static TelemetryPoint point(
            String timestamp,
            double latitude,
            double longitude
    ) {
        return new TelemetryPoint(
                Instant.parse(timestamp),
                latitude,
                longitude
        );
    }

    private static final class InMemoryVehicleLookup
            implements VehicleLookupPort {

        private final Set<Long> ids = new HashSet<>();
        private final Map<String, Long> idsByLicensePlate = new HashMap<>();

        InMemoryVehicleLookup addVehicle(Long id, String licensePlate) {
            ids.add(id);
            idsByLicensePlate.put(licensePlate, id);
            return this;
        }

        @Override
        public VehicleRef requireById(Long vehicleId) {
            if (!ids.contains(vehicleId)) {
                throw new IllegalArgumentException("Автомобиль не найден");
            }
            return new VehicleRef(vehicleId);
        }

        @Override
        public VehicleRef requireByLicensePlate(String licensePlate) {
            Long id = idsByLicensePlate.get(licensePlate);
            if (id == null) {
                throw new IllegalArgumentException("Автомобиль не найден");
            }
            return new VehicleRef(id);
        }
    }

    private static final class InMemoryTripPeriodStore
            implements TripPeriodStore {

        private boolean overlap;
        private final List<SavedTrip> savedTrips = new ArrayList<>();

        @Override
        public boolean overlaps(VehicleRef vehicle, TripPeriod period) {
            return overlap;
        }

        @Override
        public Long save(VehicleRef vehicle, TripPeriod period) {
            long id = savedTrips.size() + 1L;
            savedTrips.add(new SavedTrip(id, vehicle.id(), period));
            return id;
        }
    }

    private static final class InMemoryTelemetryStore
            implements TelemetryStore {

        private final List<TelemetryPoint> savedPoints =
                new ArrayList<>();

        @Override
        public void append(
                VehicleRef vehicle,
                TelemetrySeries telemetry
        ) {
            savedPoints.addAll(telemetry.points());
        }
    }

    private record SavedTrip(
            Long id,
            Long vehicleId,
            TripPeriod period
    ) {
    }
}
