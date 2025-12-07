package org.example.autopark.controllers.managers.APIControllers;

import lombok.RequiredArgsConstructor;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.BrandDTO;
import org.example.autopark.entity.Brand;
import org.example.autopark.service.BrandsService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Swagger / OpenAPI (лайт-набор)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Profile("!reactive")
@RequestMapping("/api/managers")
@RequiredArgsConstructor
@Tag(
        name = "Brands (Manager API)",
        description = "Справочник брендов/моделей транспортных средств"
)
public class ApiManagerBrandsController {

    private final BrandsService brandsService;
    private final ModelMapper modelMapper;

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/brands — список всех брендов
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/brands")
    @Operation(
            summary = "Список брендов",
            description = "Возвращает все бренды (используется для селектов, фильтров и т.п.)"
    )
    public List<BrandDTO> getBrands(
            @Parameter(hidden = true) @CurrentManagerId Long managerId
    ) {
        // managerId здесь нужен только чтобы сработал аргумент-резолвер
        return brandsService.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/managers/brands/{brandId} — один бренд по ID
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/brands/{brandId}")
    @Operation(
            summary = "Получить бренд по ID",
            description = "Возвращает один бренд по идентификатору"
    )
    public ResponseEntity<BrandDTO> getBrand(
            @Parameter(hidden = true) @CurrentManagerId Long managerId,
            @PathVariable Long brandId
    ) {
        Brand brand = brandsService.findOne(brandId);
        return ResponseEntity.ok(toDto(brand));
    }

    private BrandDTO toDto(Brand brand) {
        return modelMapper.map(brand, BrandDTO.class);
    }
}
