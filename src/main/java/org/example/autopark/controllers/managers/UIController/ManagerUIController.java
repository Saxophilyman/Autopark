package org.example.autopark.controllers.managers.UIController;

import jakarta.validation.Valid;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.BrandsService;
import org.example.autopark.service.DriverService;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.modelmapper.ModelMapper;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.stream.Collectors;


@Controller
@Profile("!reactive")
@RequestMapping("/managers")
public class ManagerUIController {
    private final EnterpriseService enterpriseService;
    private final VehicleService vehicleService;
    private final BrandsService brandService;
    private final DriverService driverService;
    private final ModelMapper modelMapper;

    @Autowired
    public ManagerUIController(EnterpriseService enterpriseService, VehicleService vehicleService, BrandsService brandService, DriverService driverService, ModelMapper modelMapper) {
        this.enterpriseService = enterpriseService;
        this.vehicleService = vehicleService;
        this.brandService = brandService;
        this.driverService = driverService;
        this.modelMapper = modelMapper;
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
