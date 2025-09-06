package org.example.autopark.controllers;

import org.example.autopark.dto.DriverDTO;
import org.example.autopark.entity.Driver;
import org.example.autopark.service.DriverService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Profile("!reactive")
@RequestMapping("/api/drivers")
public class ApiDriverController {

    private final DriverService driversService;
    private final ModelMapper modelMapper;

    @Autowired
    public ApiDriverController(DriverService driversService, ModelMapper modelMapper) {
        this.driversService = driversService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<DriverDTO> index() {
        return driversService.findAll().stream().map(this::convertToDriverDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public DriverDTO show(@PathVariable("id") Long id) {
        return convertToDriverDTO(driversService.findOne(id));
    }

    private DriverDTO convertToDriverDTO(Driver driver) {
        return modelMapper.map(driver, DriverDTO.class);
    }
}
