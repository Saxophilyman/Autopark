package org.example.autopark.trackingRef.application.gpx;

import lombok.RequiredArgsConstructor;

import org.example.autopark.trackingRef.application.register.RegisterVehicleMovementCommand;
import org.example.autopark.trackingRef.application.register.RegisterVehicleMovementUseCase;
import org.example.autopark.trackingRef.model.TelemetrySeries;
import org.example.autopark.trackingRef.model.TripPeriod;
import org.example.autopark.trackingRef.model.VehicleRef;
import org.example.autopark.trackingRef.port.GpxTelemetrySource;
import org.example.autopark.trackingRef.port.VehicleLookupPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Оркестратор сценария загрузки GPX.
 *
 * <p>Отвечает только за особенности GPX-входа и формирование команды общего
 * сценария. JPA-сущности и репозитории здесь отсутствуют.</p>
 */
@Service
@Profile("!reactive")
@RequiredArgsConstructor
public class TripUploadService {

    private final VehicleLookupPort vehicleLookup;
    private final GpxTelemetrySource gpxTelemetrySource;
    private final RegisterVehicleMovementUseCase registerVehicleMovement;

    public void uploadTripFromGpx(
            String licensePlate,
            LocalDateTime requestedStart,
            LocalDateTime requestedEnd,
            InputStream gpxSource
    ) {
        Instant requestedStartInstant =
                requestedStart.toInstant(ZoneOffset.UTC);
        Instant requestedEndInstant =
                requestedEnd.toInstant(ZoneOffset.UTC);

        // Проверяет корректность интервала формы до разбора файла.
        new TripPeriod(requestedStartInstant, requestedEndInstant);

        VehicleRef vehicle =
                vehicleLookup.requireByLicensePlate(licensePlate);

        TelemetrySeries telemetry = gpxTelemetrySource.read(
                gpxSource,
                requestedStart,
                requestedEnd
        );

        /*
         * Сохраняем существующее правило проекта:
         * начало поездки берётся из формы,
         * окончание — по последней допустимой GPS-точке.
         *
         * При этом проверка пересечения теперь выполняется по тому же
         * периоду, который будет фактически сохранён.
         */
        TripPeriod actualPeriod = new TripPeriod(
                requestedStartInstant,
                telemetry.lastTimestamp()
        );

        registerVehicleMovement.execute(
                new RegisterVehicleMovementCommand(
                        vehicle.id(),
                        actualPeriod,
                        telemetry
                )
        );
    }
}
