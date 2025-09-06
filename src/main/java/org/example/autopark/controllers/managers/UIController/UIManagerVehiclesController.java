package org.example.autopark.controllers.managers.UIController;

import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.dto.mapper.VehicleMapper;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Profile("!reactive")
@RequestMapping("managers/")
public class UIManagerVehiclesController {
    private final VehicleService vehicleService;
    private final EnterpriseService enterpriseService;
    private final VehicleMapper vehicleMapper;

    @Autowired
    public UIManagerVehiclesController(VehicleService vehicleService, EnterpriseService enterpriseService, VehicleMapper vehicleMapper) {
        this.vehicleService = vehicleService;
        this.enterpriseService = enterpriseService;
        this.vehicleMapper = vehicleMapper;
    }

    @GetMapping("/enterprises/{id}/vehicles")
    public String indexVehicles(
            @CurrentManagerId Long managerId,
            @PathVariable("id") Long enterpriseId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "vehicleName,vehicleId") String sortField,  // Теперь стабильная сортировка
            @RequestParam(defaultValue = "ASC") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Проверяем, принадлежит ли `enterpriseId` текущему `managerId`
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterpriseId);
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }
        // Получаем предприятие и его таймзону
        Enterprise enterprise = enterpriseService.findOne(enterpriseId);
        String enterpriseTimezone = enterprise.getTimeZone();

        // Создаём `PageRequest` для сортировки и пагинации
        String[] sortFields = sortField.split(",");
        Pageable pageRequest = PageRequest.of(
                page, size, Sort.by(
                        Arrays.stream(sortFields)
                                .map(field -> Sort.Order.by(field)
                                        .with(Sort.Direction.fromString(sortDir)))
                                .toList()
                )
        );

        // Получаем данные с пагинацией
        Page<Vehicle> vehiclesPage = vehicleService.findVehiclesForManager(
                managerId, enterpriseId, brandId, minPrice, maxPrice, year, pageRequest);

        // Преобразуем список транспортных средств с учётом таймзоны предприятия
        List<VehicleDTO> vehicleDTOList = vehiclesPage.getContent()
                .stream()
                .map(vehicle -> vehicleMapper.convertToVehicleDTO(vehicle, enterpriseTimezone))
                .collect(Collectors.toList());

        model.addAttribute("vehicles", vehicleDTOList);
        model.addAttribute("currentPage", vehiclesPage.getNumber() + 1);
        model.addAttribute("totalPages", vehiclesPage.getTotalPages());
        model.addAttribute("hasNext", vehiclesPage.hasNext());
        model.addAttribute("hasPrevious", vehiclesPage.hasPrevious());
        model.addAttribute("enterpriseId", enterpriseId);
        model.addAttribute("enterpriseTimezone", enterpriseTimezone);
        return "vehicles/index";
    }

    @GetMapping("/enterprises/{enterpriseId}/vehicles/{vehicleId}")
    public String showVehicle(
            @CurrentManagerId Long managerId,
            @PathVariable("enterpriseId") Long enterpriseId,
            @PathVariable("vehicleId") Long vehicleId,
            Model model) {

        // Проверяем, принадлежит ли предприятие текущему менеджеру
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterpriseId);
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        // Загружаем транспортное средство
        Vehicle vehicle = vehicleService.findOne(vehicleId);
        if (vehicle == null || !vehicle.getEnterpriseOwnerOfVehicle().getEnterpriseId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Транспортное средство не найдено");
        }

        // Передаём объект в модель для Thymeleaf
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("enterpriseId", enterpriseId);


        return "vehicles/showVehicle"; // Отображаем страницу просмотра
    }


}
