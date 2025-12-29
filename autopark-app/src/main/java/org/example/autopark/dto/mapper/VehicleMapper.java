package org.example.autopark.dto.mapper;

import org.example.autopark.dto.VehicleApiDto;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Vehicle;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Component
public class VehicleMapper {
    private final ModelMapper modelMapper;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public VehicleMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public VehicleDTO convertToVehicleDTO(Vehicle vehicle, String enterpriseTimezone) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        VehicleDTO vehicleDTO = modelMapper.map(vehicle, VehicleDTO.class);

        // Берём значение один раз, чтобы не дёргать геттер 2 раза
        Instant purchaseDateUtc = vehicle.getPurchaseDateUtc();

        // Локальное время предприятия (метод и так умеет работать с null)
        vehicleDTO.setPurchaseDateEnterpriseTime(
                formatUtcToEnterpriseTime(purchaseDateUtc, enterpriseTimezone)
        );

        // Сюда кладём строку, но только если не null
        vehicleDTO.setPurchaseDateUtc(
                purchaseDateUtc != null ? purchaseDateUtc.toString() : null
        );

        return vehicleDTO;
    }

    public VehicleApiDto convertToVehicleApiDto(Vehicle vehicle, String enterpriseTimezone) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        VehicleApiDto vehicleApiDto = modelMapper.map(vehicle, VehicleApiDto.class);

        if (vehicle.getBrandOwner() != null) {
            vehicleApiDto.setBrandId(vehicle.getBrandOwner().getBrandId());
        }
        if (vehicle.getEnterpriseOwnerOfVehicle() != null) {
            vehicleApiDto.setEnterpriseId(vehicle.getEnterpriseOwnerOfVehicle().getEnterpriseId());
        }

        Instant purchaseDateUtc = vehicle.getPurchaseDateUtc();
        vehicleApiDto.setPurchaseDateEnterpriseTime(
                formatUtcToEnterpriseTime(purchaseDateUtc, enterpriseTimezone)
        );

        return vehicleApiDto;
    }

    public Vehicle convertToVehicle(VehicleDTO vehicleDTO, String enterpriseTimezone) {
        if (vehicleDTO == null) {
            throw new IllegalArgumentException("VehicleDTO cannot be null");
        }

        Vehicle vehicle = modelMapper.map(vehicleDTO, Vehicle.class);
        // Может вернуть null — это ок, ниже в сервисе ты это аккуратно обрабатываешь
        vehicle.setPurchaseDateUtc(
                parseEnterpriseTimeToUtc(vehicleDTO.getPurchaseDateEnterpriseTime(), enterpriseTimezone)
        );

        return vehicle;
    }

    /**
     * Преобразует время покупки из UTC в локальное время предприятия.
     */
    private String formatUtcToEnterpriseTime(Instant utcTime, String enterpriseTimezone) {
        if (utcTime == null || enterpriseTimezone == null) return null;

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(enterpriseTimezone);
        } catch (Exception e) {
            zoneId = ZoneOffset.UTC; // Если таймзона указана неправильно, используем UTC
        }

        return utcTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(zoneId)
                .format(DATE_TIME_FORMATTER);
    }

    /**
     * Преобразует локальное время предприятия в UTC перед сохранением.
     */
    private Instant parseEnterpriseTimeToUtc(String localDateTimeStr, String enterpriseTimezone) {
        if (localDateTimeStr == null || enterpriseTimezone == null) return null;

        try {
            ZoneId zoneId = ZoneId.of(enterpriseTimezone);
            LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeStr, DATE_TIME_FORMATTER);
            return localDateTime.atZone(zoneId).withZoneSameInstant(ZoneOffset.UTC).toInstant();
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при обработке даты: " + localDateTimeStr);
        }
    }
}
