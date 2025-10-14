package org.example.autopark.controllers;

import org.example.autopark.entity.Brand;
import org.example.autopark.service.BrandsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Profile("!reactive")
@RequestMapping("/api/brands")
public class ApiBrandController {

    private final BrandsService brandsService;

    @Autowired
    public ApiBrandController(BrandsService brandsService) {
        this.brandsService = brandsService;
    }

    @GetMapping
    public List<Brand> index() {
        return brandsService.findAll();
    }

    @GetMapping("/{id}")
    public Brand show(@PathVariable("id") Long id) {
        return brandsService.findOne(id);
    }
}
