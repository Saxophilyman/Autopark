package org.example.autopark.controllers.managers.APIControllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.DriverDTO;
import org.example.autopark.dto.mapper.DriverMapper;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.service.DriverService;
import org.example.autopark.service.EnterpriseService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

// Swagger / OpenAPI (лайт-набор)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@Profile("!reactive")
@RequestMapping("/api/managers")
@RequiredArgsConstructor
@Tag(
        name = "Drivers (Manager API)",
        description = "Управление водителями предприятия"
)
public class ApiManagerDriversController {

    private final DriverService driverService;
    private final EnterpriseService enterpriseService;
    private final DriverMapper driverMapper;

    // ─────────────────────────────────────────────────────────────────────
    // GET /enterprises/{enterpriseId}/drivers — список водителей
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/enterprises/{enterpriseId}/drivers")
    @Operation(
            summary = "Список водителей предприятия",
            description = "Возвращает всех водителей, привязанных к указанному предприятию менеджера"
    )
    public ResponseEntity<List<DriverDTO>> getDrivers(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId
    ) {
        // 1. Проверяем доступ менеджера к предприятию
        enterpriseService.findEnterpriseForManager(managerId, enterpriseId);

        // 2. Берём всех водителей этого предприятия
        List<DriverDTO> dtos = driverService.findDriversForEnterprise(enterpriseId)
                .stream()
                .map(driverMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /enterprises/{enterpriseId}/drivers/{driverId} — один водитель
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/enterprises/{enterpriseId}/drivers/{driverId}")
    @Operation(
            summary = "Получить водителя по ID",
            description = "Возвращает данные водителя, если он принадлежит указанному предприятию менеджера"
    )
    public ResponseEntity<DriverDTO> getDriver(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @PathVariable Long driverId
    ) {
        // 1. Предприятие + проверка доступа
        enterpriseService.findEnterpriseForManager(managerId, enterpriseId);

        // 2. Водитель + проверка принадлежности
        Driver driver = getDriverForEnterpriseOrThrow(enterpriseId, driverId);

        // 3. DTO
        DriverDTO dto = driverMapper.toDto(driver);

        return ResponseEntity.ok(dto);
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /enterprises/{enterpriseId}/drivers — создать водителя
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping("/enterprises/{enterpriseId}/drivers")
    @Operation(
            summary = "Создать нового водителя",
            description = "Создаёт водителя и привязывает его к указанному предприятию менеджера"
    )
    public ResponseEntity<Void> createDriver(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @RequestBody @Valid DriverDTO driverDTO,
            BindingResult bindingResult
    ) {
        ValidationBindingUtil.Binding(bindingResult);

        // 1. Предприятие + проверка доступа
        Enterprise enterprise = enterpriseService.findEnterpriseForManager(managerId, enterpriseId);

        // 2. DTO → сущность
        Driver driver = driverMapper.toEntity(driverDTO, enterprise);

        // 3. Сохраняем
        driverService.save(driver);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT /enterprises/{enterpriseId}/drivers/{driverId} — обновить
    // ─────────────────────────────────────────────────────────────────────
    @PutMapping("/enterprises/{enterpriseId}/drivers/{driverId}")
    @Operation(
            summary = "Обновить водителя",
            description = "Обновляет данные водителя, если он принадлежит предприятию менеджера"
    )
    public ResponseEntity<Void> updateDriver(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @PathVariable Long driverId,
            @RequestBody @Valid DriverDTO driverDTO,
            BindingResult bindingResult
    ) {
        ValidationBindingUtil.Binding(bindingResult);

        // 1. Предприятие + проверка доступа
        Enterprise enterprise = enterpriseService.findEnterpriseForManager(managerId, enterpriseId);

        // 2. Проверяем, что этот водитель реально принадлежит этому предприятию
        getDriverForEnterpriseOrThrow(enterpriseId, driverId);

        // 3. DTO → сущность
        Driver updated = driverMapper.toEntity(driverDTO, enterprise);

        // 4. Обновляем
        driverService.update(driverId, updated);

        return ResponseEntity.ok().build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // DELETE /enterprises/{enterpriseId}/drivers/{driverId} — удалить
    // ─────────────────────────────────────────────────────────────────────
    @DeleteMapping("/enterprises/{enterpriseId}/drivers/{driverId}")
    @Operation(
            summary = "Удалить водителя",
            description = "Удаляет водителя, если он принадлежит предприятию менеджера"
    )
    public ResponseEntity<Void> deleteDriver(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @PathVariable Long driverId
    ) {
        // 1. Предприятие + проверка доступа
        enterpriseService.findEnterpriseForManager(managerId, enterpriseId);

        // 2. Водитель + принадлежность
        getDriverForEnterpriseOrThrow(enterpriseId, driverId);

        // 3. Удаляем
        driverService.delete(driverId);

        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Вспомогательный метод: водитель должен принадлежать enterpriseId
    // ─────────────────────────────────────────────────────────────────────
    private Driver getDriverForEnterpriseOrThrow(Long enterpriseId, Long driverId) {
        Driver driver = driverService.findOne(driverId);
        if (driver.getEnterpriseOwnerOfDriver() == null ||
                !Objects.equals(driver.getEnterpriseOwnerOfDriver().getEnterpriseId(), enterpriseId)) {
            throw new AccessDeniedException("У вас нет доступа к этому водителю!");
        }
        return driver;
    }
}
