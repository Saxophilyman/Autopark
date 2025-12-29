package org.example.autopark.controllers;

import jakarta.validation.Valid;
import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.VehicleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Profile("!reactive")
@RequestMapping("/api/vehicles")
public class ApiVehicleController {
    private final VehicleService vehiclesService;
    private final ModelMapper modelMapper;

    @Autowired
    public ApiVehicleController(VehicleService vehiclesService, ModelMapper modelMapper) {
        this.vehiclesService = vehiclesService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<VehicleDTO> index() {
        return vehiclesService.findAll().stream().map(this::convertToVehicleDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public VehicleDTO show(@PathVariable("id") Long id) {
        return convertToVehicleDTO(vehiclesService.findOne(id));
    }


    private VehicleDTO convertToVehicleDTO(Vehicle vehicle) {
        return modelMapper.map(vehicle, VehicleDTO.class);
    }
}