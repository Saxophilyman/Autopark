package org.example.autopark.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.autopark.dto.DriverDTO;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exception.NotCreatedException;
import org.example.autopark.exception.VehicleErrorResponse;
import org.example.autopark.exception.VehicleNotCreatedException;
import org.example.autopark.exception.VehicleNotFoundException;
import org.example.autopark.security.ManagerDetails;
import org.example.autopark.service.DriverService;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/managers")
public class ApiManagerController {
    private final EnterpriseService enterprisesService;
    private final VehicleService vehiclesService;
    private final DriverService driversService;
    private final ModelMapper modelMapper;

    public ApiManagerController(EnterpriseService enterprisesService, VehicleService vehiclesService,
                                DriverService driversService, ModelMapper modelMapper) {
        this.enterprisesService = enterprisesService;
        this.vehiclesService = vehiclesService;
        this.driversService = driversService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ModelAndView start(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ManagerDetails managerDetails = (ManagerDetails) authentication.getPrincipal();

        //System.out.println(managerDetails.getManager().getUsername());

        model.addAttribute("manager", managerDetails.getManager());

        return new ModelAndView("startPage");
    }

    @GetMapping("/{id}/enterprises")
    public List<Enterprise> indexEnterprises(@PathVariable("id") Long id) {
        return enterprisesService.findEnterprisesForManager(id);
    }

    @GetMapping("/{id}/vehicles")
    public List<VehicleDTO> indexVehicles(@PathVariable("id") Long id) {
        return vehiclesService.findVehiclesForManager(id).stream().map(this::convertToVehicleDTO)
                .collect(Collectors.toList());
    }

    private VehicleDTO convertToVehicleDTO(Vehicle vehicle) {
        return modelMapper.map(vehicle, VehicleDTO.class);
    }

    @GetMapping("/{id}/drivers")
    public List<DriverDTO> indexDrivers(@PathVariable("id") Long id) {
        return driversService.findDriversForManager(id).stream().map(this::convertToDriverDTO)
                .collect(Collectors.toList());
    }

    private DriverDTO convertToDriverDTO(Driver driver) {
        return modelMapper.map(driver, DriverDTO.class);
    }

    @PostMapping("/hello")
    public ResponseEntity<HttpStatus> hello(){
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/debug-csrf")
    public void debugCsrf(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("Cookie Name: " + cookie.getName());
                System.out.println("Cookie Value: " + cookie.getValue());
            }
        }
    }
    @PostMapping("/{id}/vehicles")
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid VehicleDTO vehicle,
                                             BindingResult bindingResult, @PathVariable Long id) {

        Binding(bindingResult);

        vehiclesService.save(convertToVehicle(vehicle));

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/{id}/enterprises")
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid Enterprise enterprise,
                                             BindingResult bindingResult,
                                             @PathVariable("id") Long id) {

        Binding(bindingResult);

        enterprisesService.save(enterprise, id);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    @PutMapping("/{id}/vehicles/{idVehicle}")
    public ResponseEntity<HttpStatus> update(@RequestBody @Valid VehicleDTO vehicle,
                                             BindingResult bindingResult,
                                             @PathVariable("idVehicle") Long id) {
        Binding(bindingResult);

        vehiclesService.update(id, convertToVehicle(vehicle));

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping("/{id}/enterprises/{idEnterprise}")
    public ResponseEntity<HttpStatus> update(@PathVariable("id") Long idManager,
                                             @RequestBody @Valid Enterprise enterprise,
                                             BindingResult bindingResult,
                                             @PathVariable("idEnterprise") Long idEnterprise) {
        Binding(bindingResult);

        enterprisesService.update(idManager, idEnterprise, enterprise);

        return ResponseEntity.ok(HttpStatus.OK);
    }


    @DeleteMapping("/{id}/vehicles/{idVehicle}")
    public ResponseEntity<HttpStatus> deleteVehicle(@PathVariable("idVehicle") Long id) {
        vehiclesService.delete(id);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @DeleteMapping("/{id}/enterprises/{idEnterprise}")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") Long idManager,
                                             @PathVariable("idEnterprise") Long idEnterprise) {

        enterprisesService.delete(idManager, idEnterprise);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    private void Binding(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();

            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors) {
                errorMsg.append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage())
                        .append(";");
            }

            throw new NotCreatedException(errorMsg.toString());
        }
    }


    private Vehicle convertToVehicle(VehicleDTO vehicleDTO) {
        return modelMapper.map(vehicleDTO, Vehicle.class);
    }

    @ExceptionHandler
    private ResponseEntity<VehicleErrorResponse> handlerException(VehicleNotFoundException e) {
        VehicleErrorResponse response = new VehicleErrorResponse(
                "Vehicle with this id wasn't found",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // статус 404
    }

    @ExceptionHandler
    private ResponseEntity<VehicleErrorResponse> handlerException(VehicleNotCreatedException e) {
        VehicleErrorResponse response = new VehicleErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // статус 400
    }
}
