package org.example.autopark.controllers.managers.APIControllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.dto.mapper.VehicleMapper;
import org.example.autopark.dto.mapper.VehiclePageDTO;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@RestController
@Profile("!reactive")
@RequestMapping("/api/managers")
@RequiredArgsConstructor
public class ApiManagerVehiclesController {
    private final VehicleService vehicleService;
    private final EnterpriseService enterpriseService;
    private final VehicleMapper vehicleMapper;

    @GetMapping("/enterprises/{enterpriseId}/vehicles")
    public ResponseEntity<VehiclePageDTO> getVehiclesForEnterprise(
            @CurrentManagerId Long managerId,
            @PathVariable Long enterpriseId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "vehicleName,vehicleId") String sortField,
            @RequestParam(defaultValue = "ASC") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Запрос API: список автомобилей для предприятия ID={} менеджера ID={}", enterpriseId, managerId);

        // Проверяем доступ менеджера к предприятию
        if (!enterpriseService.managerHasEnterprise(managerId, enterpriseId)) {
            log.warn("Доступ запрещён: менеджер ID={} пытается получить доступ к предприятию ID={}", managerId, enterpriseId);
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        // Получаем `VehiclePageDTO` из сервиса
        VehiclePageDTO vehiclePageDTO = vehicleService.getVehiclesForEnterprise(
                managerId, enterpriseId, brandId, minPrice, maxPrice, year, sortField, sortDir, page, size);

        return ResponseEntity.ok(vehiclePageDTO);
    }
}
