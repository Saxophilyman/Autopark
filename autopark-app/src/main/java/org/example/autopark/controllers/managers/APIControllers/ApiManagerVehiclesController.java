package org.example.autopark.controllers.managers.APIControllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.dto.mapper.VehicleMapper;
import org.example.autopark.dto.mapper.VehiclePageDTO;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

// Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@Profile("!reactive")
@RequestMapping("/api/managers")
@RequiredArgsConstructor
@Tag(
        name = "Vehicles (Manager API)",
        description = "Управление автомобилями предприятий, доступных менеджеру"
)
public class ApiManagerVehiclesController {

    private final VehicleService vehicleService;
    private final EnterpriseService enterpriseService;
    private final VehicleMapper vehicleMapper;

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/enterprises/{enterpriseId}/vehicles
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/enterprises/{enterpriseId}/vehicles")
    @Operation(
            summary = "Список автомобилей предприятия",
            description = "Возвращает страницу автомобилей выбранного предприятия с фильтрами и сортировкой"
    )
    public ResponseEntity<VehiclePageDTO> getVehiclesForEnterprise(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @PathVariable Long enterpriseId,

            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "vehicleName,vehicleId") String sortField,
            @RequestParam(defaultValue = "ASC") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Запрос API: список автомобилей для предприятия ID={} менеджера ID={}",
                enterpriseId, managerId);

        if (!enterpriseService.managerHasEnterprise(managerId, enterpriseId)) {
            log.warn("Доступ запрещён: менеджер ID={} пытается получить доступ к предприятию ID={}",
                    managerId, enterpriseId);
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        VehiclePageDTO vehiclePageDTO = vehicleService.getVehiclesForEnterprise(
                managerId, enterpriseId, brandId, minPrice, maxPrice, year,
                sortField, sortDir, page, size
        );

        return ResponseEntity.ok(vehiclePageDTO);
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/enterprises/{enterpriseId}/vehicles/{vehicleId}
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/enterprises/{enterpriseId}/vehicles/{vehicleId}")
    @Operation(
            summary = "Получить автомобиль по ID",
            description = "Возвращает автомобиль предприятия при наличии доступа у менеджера"
    )
    public ResponseEntity<VehicleDTO> getVehicle(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @PathVariable Long vehicleId
    ) {
        Enterprise enterprise = enterpriseService.findEnterpriseForManager(managerId, enterpriseId);
        Vehicle vehicle = getVehicleForEnterpriseOrThrow(enterpriseId, vehicleId);
        VehicleDTO dto = vehicleMapper.convertToVehicleDTO(vehicle, enterprise.getTimeZone());
        return ResponseEntity.ok(dto);
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/managers/enterprises/{enterpriseId}/vehicles
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping("/enterprises/{enterpriseId}/vehicles")
    @Operation(
            summary = "Создать новый автомобиль",
            description = "Создаёт автомобиль для указанного предприятия и привязывает его к бренду"
    )
    public ResponseEntity<Void> createVehicle(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @RequestBody @Valid VehicleDTO vehicleDTO,
            BindingResult bindingResult
    ) {
        ValidationBindingUtil.Binding(bindingResult);

        Enterprise enterprise = enterpriseService.findEnterpriseForManager(managerId, enterpriseId);

        Vehicle vehicle = vehicleMapper.convertToVehicle(vehicleDTO, enterprise.getTimeZone());
        vehicle.setEnterpriseOwnerOfVehicle(enterprise);

        Long brandId = getBrandIdOrThrow(vehicleDTO);

        vehicleService.save(vehicle, brandId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT /api/managers/enterprises/{enterpriseId}/vehicles/{vehicleId}
    // ─────────────────────────────────────────────────────────────────────
    @PutMapping("/enterprises/{enterpriseId}/vehicles/{vehicleId}")
    @Operation(
            summary = "Обновить автомобиль",
            description = "Обновляет данные автомобиля предприятия при наличии доступа у менеджера"
    )
    public ResponseEntity<Void> updateVehicle(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @PathVariable Long vehicleId,
            @RequestBody @Valid VehicleDTO vehicleDTO,
            BindingResult bindingResult
    ) {
        ValidationBindingUtil.Binding(bindingResult);

        Enterprise enterprise = enterpriseService.findEnterpriseForManager(managerId, enterpriseId);
        getVehicleForEnterpriseOrThrow(enterpriseId, vehicleId);

        Vehicle updated = vehicleMapper.convertToVehicle(vehicleDTO, enterprise.getTimeZone());
        Long brandId = getBrandIdOrThrow(vehicleDTO);

        vehicleService.update(vehicleId, updated, brandId, enterpriseId);

        return ResponseEntity.ok().build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // DELETE /api/managers/enterprises/{enterpriseId}/vehicles/{vehicleId}
    // ─────────────────────────────────────────────────────────────────────
    @DeleteMapping("/enterprises/{enterpriseId}/vehicles/{vehicleId}")
    @Operation(
            summary = "Удалить автомобиль",
            description = "Удаляет автомобиль предприятия при наличии доступа у менеджера"
    )
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @PathVariable Long vehicleId
    ) {
        enterpriseService.findEnterpriseForManager(managerId, enterpriseId);
        getVehicleForEnterpriseOrThrow(enterpriseId, vehicleId);
        vehicleService.delete(vehicleId);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Вспомогательные методы
    // ─────────────────────────────────────────────────────────────────────
    private Vehicle getVehicleForEnterpriseOrThrow(Long enterpriseId, Long vehicleId) {
        Vehicle vehicle = vehicleService.findOne(vehicleId);
        if (vehicle.getEnterpriseOwnerOfVehicle() == null ||
                !Objects.equals(vehicle.getEnterpriseOwnerOfVehicle().getEnterpriseId(), enterpriseId)) {
            throw new AccessDeniedException("У вас нет доступа к этому автомобилю!");
        }
        return vehicle;
    }

    private Long getBrandIdOrThrow(VehicleDTO dto) {
        if (dto.getBrand() == null || dto.getBrand().getBrandId() == null) {
            throw new IllegalArgumentException("Не указан бренд автомобиля");
        }
        return dto.getBrand().getBrandId();
    }
}
