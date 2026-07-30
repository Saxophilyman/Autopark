package org.example.autopark.trackingRef.application.register;

import lombok.RequiredArgsConstructor;

import org.example.autopark.trackingRef.model.TelemetryPoint;
import org.example.autopark.trackingRef.model.TripPeriod;
import org.example.autopark.trackingRef.model.VehicleRef;
import org.example.autopark.trackingRef.port.TelemetryStore;
import org.example.autopark.trackingRef.port.TripPeriodStore;
import org.example.autopark.trackingRef.port.VehicleLookupPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Единый сценарий изменения состояния автопарка при регистрации движения.
 *
 * <p>Источники координат ничего не знают о JPA и транзакции. Сервис получает
 * уже подготовленные нейтральные данные, проверяет общие инварианты и
 * атомарно сохраняет Trip и GpsPoint.</p>
 */
@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class RegisterVehicleMovementService
        implements RegisterVehicleMovementUseCase {

    private final VehicleLookupPort vehicleLookup;
    private final TripPeriodStore tripPeriodStore;
    private final TelemetryStore telemetryStore;

    @Override
    @Transactional
    public Long execute(RegisterVehicleMovementCommand command) {
        VehicleRef vehicle = vehicleLookup.requireById(command.vehicleId());
        TripPeriod period = command.tripPeriod();

        validateTelemetryInsidePeriod(command, period);

        if (tripPeriodStore.overlaps(vehicle, period)) {
            throw new IllegalArgumentException(
                    "Поездка пересекается с уже существующей поездкой"
            );
        }

        Long tripId = tripPeriodStore.save(vehicle, period);
        telemetryStore.append(vehicle, command.telemetry());

        return tripId;
    }

    private void validateTelemetryInsidePeriod(
            RegisterVehicleMovementCommand command,
            TripPeriod period
    ) {
        for (TelemetryPoint point : command.telemetry().points()) {
            if (!period.containsInclusive(point.timestamp())) {
                throw new IllegalArgumentException(
                        "GPS-точка %s не входит в период поездки %s — %s"
                                .formatted(
                                        point.timestamp(),
                                        period.start(),
                                        period.end()
                                )
                );
            }
        }
    }
}
