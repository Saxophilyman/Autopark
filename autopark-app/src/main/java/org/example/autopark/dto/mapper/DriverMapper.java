package org.example.autopark.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.example.autopark.dto.DriverDTO;
import org.example.autopark.dto.VehicleDTOForDriver;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DriverMapper {

    private final EnterpriseMapper enterpriseMapper;

    // ─────────────────────────────────────
    // Entity → DTO
    // ─────────────────────────────────────
    public DriverDTO toDto(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }

        DriverDTO dto = new DriverDTO();
        dto.setDriverId(driver.getDriverId());

        // имена полей отличаются
        dto.setName(driver.getDriverName());
        dto.setSalary(String.valueOf(driver.getDriverSalary()));

        dto.setActive(driver.isActive());

        // предприятие
        if (driver.getEnterpriseOwnerOfDriver() != null) {
            dto.setEnterprise(
                    enterpriseMapper.convertToDTO(driver.getEnterpriseOwnerOfDriver())
            );
        }

        // только id активной машины
        if (driver.getActiveVehicle() != null) {
            dto.setActiveVehicle(
                    new VehicleDTOForDriver(driver.getActiveVehicle().getVehicleId())
            );
        }

        return dto;
    }

    // ─────────────────────────────────────
    // DTO → Entity
    // ─────────────────────────────────────
    /**
     * Конструирует сущность Driver на основе DTO и "истинного" предприятия.
     * Enterprise сюда передаём из контроллера (из path-параметра),
     * а не доверяем полю enterprise в DTO.
     */
    public Driver toEntity(DriverDTO dto, Enterprise enterprise) {
        if (dto == null) {
            throw new IllegalArgumentException("DriverDTO cannot be null");
        }

        Driver driver = new Driver();

        // id обычно игнорируем при создании, но при апдейте он не помешает
        driver.setDriverId(dto.getDriverId());

        driver.setDriverName(dto.getName());
        driver.setDriverSalary(parseSalary(dto.getSalary()));
        driver.setActive(dto.isActive());

        // настоящая связь с предприятием — всегда из аргумента
        driver.setEnterpriseOwnerOfDriver(enterprise);

        // activeVehicle здесь не трогаем:
        // её лучше менять отдельным специализированным методом/эндпоинтом,
        // иначе логика назначения активной машины размажется.
        return driver;
    }

    // ─────────────────────────────────────
    // Вспомогательный разбор зарплаты
    // ─────────────────────────────────────
    private int parseSalary(String salaryStr) {
        if (salaryStr == null || salaryStr.isBlank()) {
            // @NotEmpty в DTO уже проверит пустоту, но на всякий случай:
            throw new IllegalArgumentException("Зарплата не может быть пустой");
        }
        try {
            return Integer.parseInt(salaryStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное значение зарплаты: " + salaryStr);
        }
    }
}
/*

Если activeVehicle лениво загружается (@OneToOne(fetch = FetchType.LAZY))
и метод findDriversForEnterprise(...) не делает JOIN FETCH, то:
В момент вызова driver.getActiveVehicle() вне транзакции можно получить LazyInitializationException.

Если сейчас всё работает — значит:
либо activeVehicle уже и так загружается (join fetch),
либо где-то ещё есть транзакция поверх.

Просто держи в уме: если внезапно увидишь LazyInitializationException на этом месте —
диагноз почти точно будет в этом getActiveVehicle().
 */