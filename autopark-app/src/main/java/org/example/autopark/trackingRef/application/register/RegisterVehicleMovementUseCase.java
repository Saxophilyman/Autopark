package org.example.autopark.trackingRef.application.register;

/**
 * Прикладная граница регистрации движения автомобиля.
 */
public interface RegisterVehicleMovementUseCase {

    Long execute(RegisterVehicleMovementCommand command);
}
