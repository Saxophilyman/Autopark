package org.example.autopark.controllers.managers.APIControllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.EnterpriseDTO;
import org.example.autopark.dto.mapper.EnterpriseMapper;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Swagger / OpenAPI (лайт-набор)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@Profile("!reactive")
@RequestMapping("/api/managers")
@Tag(
        name = "Enterprises (Manager API)",
        description = "Управление предприятиями, доступными менеджеру"
)
public class ApiManagerEnterprisesController {

    private final EnterpriseService enterprisesService;
    private final EnterpriseMapper enterpriseMapper;

    @Autowired
    public ApiManagerEnterprisesController(EnterpriseService enterprisesService,
                                           EnterpriseMapper enterpriseMapper) {
        this.enterprisesService = enterprisesService;
        this.enterpriseMapper = enterpriseMapper;
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/enterprises — список предприятий менеджера
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/enterprises")
    @Operation(
            summary = "Список предприятий менеджера",
            description = "Возвращает все предприятия, к которым привязан текущий менеджер"
    )
    public List<EnterpriseDTO> indexEnterprises(
            @Parameter(hidden = true) @CurrentManagerId Long managerId
    ) {
        return enterprisesService.findEnterprisesForManager(managerId)
                .stream()
                .map(enterpriseMapper::convertToDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/enterprises/{enterpriseId}
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/enterprises/{enterpriseId}")
    @Operation(
            summary = "Получить предприятие по ID",
            description = "Возвращает предприятие менеджера по его идентификатору"
    )
    public ResponseEntity<EnterpriseDTO> getEnterprise(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId
    ) {
        Enterprise enterprise = enterprisesService.findEnterpriseForManager(managerId, enterpriseId);
        EnterpriseDTO dto = enterpriseMapper.convertToDTO(enterprise);
        return ResponseEntity.ok(dto);
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/managers/enterprises — создать предприятие
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping("/enterprises")
    @Operation(
            summary = "Создать новое предприятие",
            description = "Создаёт предприятие и привязывает его к текущему менеджеру"
    )
    public ResponseEntity<Void> create(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @RequestBody @Valid EnterpriseDTO dto,
            BindingResult bindingResult
    ) {
        ValidationBindingUtil.Binding(bindingResult);
        Enterprise enterprise = enterpriseMapper.convertToEntity(dto);
        enterprisesService.save(enterprise, managerId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT /api/managers/enterprises/{enterpriseId} — обновить
    // ─────────────────────────────────────────────────────────────────────
    @PutMapping("/enterprises/{enterpriseId}")
    @Operation(
            summary = "Обновить предприятие",
            description = "Обновляет данные предприятия, если оно принадлежит текущему менеджеру"
    )
    public ResponseEntity<Void> update(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @RequestBody @Valid EnterpriseDTO dto,
            BindingResult bindingResult
    ) {
        ValidationBindingUtil.Binding(bindingResult);
        Enterprise enterprise = enterpriseMapper.convertToEntity(dto);
        enterprisesService.update(managerId, enterpriseId, enterprise);
        return ResponseEntity.ok().build(); // 200 без тела
    }

    // ─────────────────────────────────────────────────────────────────────
    // DELETE /api/managers/enterprises/{enterpriseId} — удалить
    // ─────────────────────────────────────────────────────────────────────
    @DeleteMapping("/enterprises/{enterpriseId}")
    @Operation(
            summary = "Удалить предприятие",
            description = "Удаляет предприятие менеджера. Детали зависят от бизнес-логики сервиса."
    )
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId
    ) {
        enterprisesService.delete(managerId, enterpriseId);
        return ResponseEntity.noContent().build();
    }
}
