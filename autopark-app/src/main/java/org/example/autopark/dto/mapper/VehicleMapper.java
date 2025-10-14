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

    /**
     * Конвертирует `Vehicle` в `VehicleDTO` и переводит время в таймзону предприятия.
     */
    public VehicleDTO convertToVehicleDTO(Vehicle vehicle, String enterpriseTimezone) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        VehicleDTO vehicleDTO = modelMapper.map(vehicle, VehicleDTO.class);
        vehicleDTO.setPurchaseDateEnterpriseTime(
                formatUtcToEnterpriseTime(vehicle.getPurchaseDateUtc(), enterpriseTimezone)
        );
        vehicleDTO.setPurchaseDateUtc(vehicle.getPurchaseDateUtc().toString()); // Добавляем UTC время

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

        vehicleApiDto.setPurchaseDateEnterpriseTime(
                formatUtcToEnterpriseTime(vehicle.getPurchaseDateUtc(), enterpriseTimezone)
        );
        return vehicleApiDto;
    }

    /**
     * Конвертирует `VehicleDTO` в `Vehicle` и переводит время из таймзоны предприятия в UTC.
     */
    public Vehicle convertToVehicle(VehicleDTO vehicleDTO, String enterpriseTimezone) {
        if (vehicleDTO == null) {
            throw new IllegalArgumentException("VehicleDTO cannot be null");
        }

        Vehicle vehicle = modelMapper.map(vehicleDTO, Vehicle.class);
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
