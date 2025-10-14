package org.example.autopark.controllers;

import org.example.autopark.dto.VehicleDTO;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Profile("!reactive")
@RequestMapping("/api/enterprises")
public class ApiEnterpriseController {
    private final EnterpriseService enterprisesService;

    @Autowired
    public ApiEnterpriseController(EnterpriseService enterprisesService) {
        this.enterprisesService = enterprisesService;
    }

    @GetMapping
    public List<Enterprise> index() {
        return enterprisesService.findAll();
    }

    @GetMapping("/{id}")
    public Enterprise show(@PathVariable("id") Long id) {
        return enterprisesService.findOne(id);
    }


}
