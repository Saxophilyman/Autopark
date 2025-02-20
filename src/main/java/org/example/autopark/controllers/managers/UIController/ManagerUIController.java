package org.example.autopark.controllers.managers.UIController;

import jakarta.validation.Valid;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.BrandService;
import org.example.autopark.service.DriverService;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/managers")
public class ManagerUIController {
    private final EnterpriseService enterpriseService;
    private final VehicleService vehicleService;
    private final BrandService brandService;
    private final DriverService driverService;
    private final ModelMapper modelMapper;

    @Autowired
    public ManagerUIController(EnterpriseService enterpriseService, VehicleService vehicleService, BrandService brandService, DriverService driverService, ModelMapper modelMapper) {
        this.enterpriseService = enterpriseService;
        this.vehicleService = vehicleService;
        this.brandService = brandService;
        this.driverService = driverService;
        this.modelMapper = modelMapper;
    }


    @GetMapping("/enterprises")
    public ModelAndView indexStart(@CurrentManagerId Long managerId) {

        ModelAndView enterprises = new ModelAndView("enterprises/index");
        enterprises.addObject("enterprises", enterpriseService.findEnterprisesForManager(managerId));

        return enterprises;
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

        model.addAttribute("vehicles", vehiclesPage.getContent().stream().map(this::convertToVehicleDTO).collect(Collectors.toList()));
        model.addAttribute("currentPage", vehiclesPage.getNumber() + 1);
        model.addAttribute("totalPages", vehiclesPage.getTotalPages());
        model.addAttribute("hasNext", vehiclesPage.hasNext());
        model.addAttribute("hasPrevious", vehiclesPage.hasPrevious());
        model.addAttribute("enterpriseId", enterpriseId);
        return "vehicles/index";
    }


    @GetMapping("/enterprises/{id}/vehicles/new")
    public String newVehicle(
            @CurrentManagerId Long managerId,
            @PathVariable("id") Long enterpriseId,
            Model model) {

        // Проверяем, принадлежит ли `enterpriseId` текущему `managerId`
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterpriseId);
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        // Создаём новый объект `Vehicle` и сразу устанавливаем ему `enterpriseId`
        Vehicle vehicle = new Vehicle();
        vehicle.setEnterpriseOwnerOfVehicle(enterpriseService.findOne(enterpriseId));

        // Передаём данные в модель
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("enterpriseId", enterpriseId);

        return "vehicles/newVehicle";
    }

    @PostMapping("/enterprises/{id}/vehicles/save")
    public String saveVehicle(
            @CurrentManagerId Long managerId,
            @PathVariable("id") Long enterpriseId,
            @ModelAttribute("vehicle") @Valid Vehicle vehicle,
            BindingResult bindingResult,
            Model model) {

        // Проверяем, принадлежит ли `enterpriseId` текущему `managerId`
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterpriseId);
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        // Если есть ошибки валидации, возвращаемся на страницу создания
        if (bindingResult.hasErrors()) {
            model.addAttribute("brands", brandService.findAll());
            model.addAttribute("enterpriseId", enterpriseId);
            return "vehicles/newVehicle";
        }

        // Привязываем к предприятию
        vehicle.setEnterpriseOwnerOfVehicle(enterpriseService.findOne(enterpriseId));

        // Сохраняем
        vehicleService.save(vehicle);

        // Перенаправляем на список автомобилей предприятия
        return "redirect:/managers/enterprises/" + enterpriseId + "/vehicles";
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

    @GetMapping("/enterprises/{enterpriseId}/vehicles/{id}/edit")
    public String editVehicle(
            @CurrentManagerId Long managerId,
            @PathVariable("enterpriseId") Long enterpriseId,
            @PathVariable("id") Long vehicleId,
            Model model) {

        // Проверяем, есть ли у менеджера доступ к этому предприятию
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterpriseId);
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        // Получаем объект `Vehicle` по `vehicleId`
        Vehicle vehicle = vehicleService.findOne(vehicleId);
        if (vehicle == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Транспортное средство не найдено");
        }

        // Добавляем данные в модель
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("brands", brandService.findAll()); // Доступные марки автомобилей
        model.addAttribute("enterpriseId", enterpriseId);

        return "vehicles/editVehicle"; // Путь к Thymeleaf-шаблону
    }

    @PostMapping("/enterprises/{enterpriseId}/vehicles/{id}/update")
    public String updateVehicle(
            @CurrentManagerId Long managerId,
            @PathVariable("enterpriseId") Long enterpriseId,
            @PathVariable("id") Long vehicleId,
            @ModelAttribute("vehicle") Vehicle updatedVehicle) {

        // Проверяем доступ
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterpriseId);
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        // Обновляем данные
        vehicleService.update(vehicleId, updatedVehicle, updatedVehicle.getBrandOwner().getBrandId(), enterpriseId);

        // Перенаправляем обратно на список
        return "redirect:/managers/enterprises/" + enterpriseId + "/vehicles";
    }

    @PostMapping("/enterprises/{enterpriseId}/vehicles/{id}/delete")
    public String deleteVehicle(
            @CurrentManagerId Long managerId,
            @PathVariable("enterpriseId") Long enterpriseId,
            @PathVariable("id") Long vehicleId) {

        // Проверяем доступ менеджера к предприятию
        boolean hasAccess = enterpriseService.managerHasEnterprise(managerId, enterpriseId);
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этому предприятию!");
        }

        Vehicle vehicle = vehicleService.findOne(vehicleId);
        vehicle.setActiveDriver(null);
        // Удаляем транспортное средство
        vehicleService.delete(vehicleId);

        // Перенаправляем на список автомобилей
        return "redirect:/managers/enterprises/" + enterpriseId + "/vehicles";
    }


    private VehicleDTO convertToVehicleDTO(Vehicle vehicle) {
        return modelMapper.map(vehicle, VehicleDTO.class);
    }
}
