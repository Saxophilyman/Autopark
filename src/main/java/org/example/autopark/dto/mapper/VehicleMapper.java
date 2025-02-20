package org.example.autopark.dto.mapper;

import org.example.autopark.dto.BrandDTO;
import org.example.autopark.dto.EnterpriseDTO;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class VehicleMapper {

    private final ModelMapper modelMapper;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public VehicleMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    public VehicleDTO convertToVehicleDTO(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        // Используем ModelMapper для автоматического маппинга полей
        VehicleDTO vehicleDTO = modelMapper.map(vehicle, VehicleDTO.class);
        // Конвертация UTC -> Таймзона предприятия
        vehicleDTO.setPurchaseDateEnterpriseTime(formatUtcToEnterpriseTime(
                vehicle.getPurchaseDateUtc(), vehicle.getEnterpriseOwnerOfVehicle()));
        return vehicleDTO;
    }

    /**
     * Конвертация DTO в сущность Vehicle перед сохранением.
     */
    public Vehicle convertToVehicle(VehicleDTO vehicleDTO, Enterprise enterprise) {
        if (vehicleDTO == null) {
            throw new IllegalArgumentException("VehicleDTO cannot be null");
        }
        // Используем ModelMapper для автоматического маппинга полей
        Vehicle vehicle = modelMapper.map(vehicleDTO, Vehicle.class);
        vehicle.setEnterpriseOwnerOfVehicle(enterprise);
        // Конвертация времени из локальной таймзоны предприятия в UTC перед сохранением
        vehicle.setPurchaseDateUtc(parseEnterpriseTimeToUtc(
                vehicleDTO.getPurchaseDateEnterpriseTime(), enterprise.getTimeZone()));
        return vehicle;
    }

    /**
     * Преобразует время покупки из UTC в локальное время предприятия.
     */
    private String formatUtcToEnterpriseTime(Instant utcTime, Enterprise enterprise) {
        if (utcTime == null) return null;

        ZoneId zoneId = (enterprise != null && enterprise.getTimeZone() != null) ?
                ZoneId.of(enterprise.getTimeZone()) : ZoneOffset.UTC;

        return utcTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(zoneId)
                .format(DATE_TIME_FORMATTER);
    }

    /**
     * Преобразует локальное время предприятия в UTC перед сохранением.
     */
    private Instant parseEnterpriseTimeToUtc(String localDateTimeStr, String timezone) {
        if (localDateTimeStr == null || timezone == null) return null;

        ZoneId zoneId = ZoneId.of(timezone);
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeStr, DATE_TIME_FORMATTER);

        return localDateTime.atZone(zoneId).withZoneSameInstant(ZoneOffset.UTC).toInstant();
    }
}
