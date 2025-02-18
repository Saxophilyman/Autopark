package org.example.autopark.controllers;

import jakarta.validation.Valid;
import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.dto.DriverDTO;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exception.VehicleErrorResponse;
import org.example.autopark.exception.VehicleNotCreatedException;
import org.example.autopark.exception.VehicleNotFoundException;
import org.example.autopark.security.ManagerDetails;
import org.example.autopark.service.DriverService;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/managers")
public class ApiManagerController {
    private final EnterpriseService enterprisesService;
    private final VehicleService vehicleService;
    private final DriverService driversService;
    private final ModelMapper modelMapper;
    Logger logger = LoggerFactory.getLogger(ApiManagerController.class);

    public ApiManagerController(EnterpriseService enterprisesService, VehicleService vehicleService,
                                DriverService driversService, ModelMapper modelMapper) {
        this.enterprisesService = enterprisesService;
        this.vehicleService = vehicleService;
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




    //CRUD FOR ENTERPRISES
    //GET
    @GetMapping("/{id}/enterprises")
    public List<Enterprise> indexEnterprises(@PathVariable("id") Long id) {
        return enterprisesService.findEnterprisesForManager(id);
    }

    //PUT
    @PutMapping("/{id}/enterprises/{idEnterprise}")
    public ResponseEntity<HttpStatus> update(@PathVariable("id") Long idManager,
                                             @RequestBody @Valid Enterprise enterprise,
                                             BindingResult bindingResult,
                                             @PathVariable("idEnterprise") Long idEnterprise) {
        ValidationBindingUtil.Binding(bindingResult);
        enterprisesService.update(idManager, idEnterprise, enterprise);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    //POST
    @PostMapping("/{id}/enterprises")
    public ResponseEntity<Void> create(@RequestBody @Valid Enterprise enterprise,
                                       BindingResult bindingResult,
                                       @PathVariable("id") Long id) {
        ValidationBindingUtil.Binding(bindingResult);
        enterprisesService.save(enterprise, id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //DELETE
    @DeleteMapping("/{id}/enterprises/{idEnterprise}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long idManager,
                                       @PathVariable("idEnterprise") Long idEnterprise) {
        enterprisesService.delete(idManager, idEnterprise);
        return ResponseEntity.noContent().build(); // Возвращаем 204 No Content
    }

    //UPDATE
//----------------------//

    //CRUD FOR VEHICLES
    //GET
//    @GetMapping("/{id}/vehicles")
//    public List<VehicleDTO> indexVehicles(@PathVariable("id") Long id) {
//        return vehicleService.findVehiclesForManager(id).stream().map(this::convertToVehicleDTO)
//                .collect(Collectors.toList());
//    }

    //GET бывший рабочий контроллер
    @GetMapping("/enterprises/{id}/vehicles")
    public List<VehicleDTO> indexVehicles(
            @PathVariable("id") Long managerId,
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "vehicleName,vehicleId") String sortField,  // Теперь стабильная сортировка
            @RequestParam(defaultValue = "ASC") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

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

                return vehiclesPage.getContent().stream().map(this::convertToVehicleDTO)
                .collect(Collectors.toList());
    }


//    @GetMapping("/{id}/vehicles")
//    public ResponseEntity<Page<VehicleDTO>> getVehicles(
//            @PathVariable("id") Long id,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "15") int size,
//            @RequestParam(defaultValue = "vehicleName") String sortField,
//            @RequestParam(defaultValue = "asc") String sortDirection) {
//
//        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
//
//        Page<VehicleDTO> vehicles = vehicleService.findVehiclesForManager(id, pageable)
//                .map(this::convertToVehicleDTO);
//        return ResponseEntity.ok(vehicles);
//    }



    //PUT
    @PutMapping("/{id}/vehicles/{idVehicle}")
    public ResponseEntity<HttpStatus> update(@RequestBody @Valid VehicleDTO vehicle,
                                             BindingResult bindingResult,
                                             @PathVariable("idVehicle") Long idVehicle) {
        ValidationBindingUtil.Binding(bindingResult);
        vehicleService.update(idVehicle, convertToVehicle(vehicle));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    //POST
    @PostMapping("/{id}/vehicles")
    public ResponseEntity<Void> create(@RequestBody @Valid VehicleDTO vehicle,
                                       BindingResult bindingResult) {
        logger.info("Received vehicle: {}", vehicle);
        ValidationBindingUtil.Binding(bindingResult);
        vehicleService.save(convertToVehicle(vehicle));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //DELETE
    @DeleteMapping("/{id}/vehicles/{idVehicle}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable("idVehicle") Long id) {
        vehicleService.delete(id);

        return ResponseEntity.noContent().build(); // Возвращаем 204 No Content
    }

    //UPDATE
//----------------------//

    //CRUD FOR DRIVERS
    //GET
    @GetMapping("/{id}/drivers")
    public List<DriverDTO> indexDrivers(@PathVariable("id") Long id) {
        return driversService.findDriversForManager(id).stream().map(this::convertToDriverDTO)
                .collect(Collectors.toList());
    }

    //PUT
    @PutMapping("/{id}/drivers/{idDriver}")
    public ResponseEntity<HttpStatus> update(@RequestBody @Valid DriverDTO driverDTO,
                                             BindingResult bindingResult,
                                             @PathVariable("idDriver") Long id) {
        ValidationBindingUtil.Binding(bindingResult);

        driversService.update(id, convertToDriver(driverDTO));

        return ResponseEntity.ok(HttpStatus.OK);
    }

    //
    //POST
    @PostMapping("/{id}/drivers")
    public ResponseEntity<Void> create(@RequestBody @Valid DriverDTO driverDTO,
                                       BindingResult bindingResult) {
        logger.info("Received vehicle: {}", driverDTO);
        ValidationBindingUtil.Binding(bindingResult);
        driversService.save(convertToDriver(driverDTO));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //DELETE
    @DeleteMapping("/{id}/drivers/{idDriver}")
    public ResponseEntity<Void> deleteDriver(@PathVariable("idDriver") Long id) {
        driversService.delete(id);
        return ResponseEntity.noContent().build(); // Возвращаем 204 No Content
    }

    //UPDATE
//----------------------//


    //внутренние методы
    private Vehicle convertToVehicle(VehicleDTO vehicleDTO) {
        return modelMapper.map(vehicleDTO, Vehicle.class);
    }

    private VehicleDTO convertToVehicleDTO(Vehicle vehicle) {
        return modelMapper.map(vehicle, VehicleDTO.class);
    }

    private DriverDTO convertToDriverDTO(Driver driver) {
        return modelMapper.map(driver, DriverDTO.class);
    }

    private Driver convertToDriver(DriverDTO driverDTO) {
        return modelMapper.map(driverDTO, Driver.class);
    }

//    @ExceptionHandler
//    private ResponseEntity<VehicleErrorResponse> handlerException(VehicleNotFoundException e) {
//        VehicleErrorResponse response = new VehicleErrorResponse(
//                "Vehicle with this id wasn't found",
//                System.currentTimeMillis()
//        );
//
//        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // статус 404
//    }

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
