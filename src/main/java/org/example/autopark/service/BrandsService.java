package org.example.autopark.service;

import lombok.extern.slf4j.Slf4j;
import org.example.autopark.entity.Brand;
import org.example.autopark.exception.ResourceNotFoundException;
import org.example.autopark.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Slf4j
public class BrandsService {
    private final BrandRepository brandRepository;

    @Autowired
    public BrandsService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }


    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    public Brand findOne(Long id){
        return brandRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Brand with id " + id + " not found"));
    }

    @Transactional
    public void save(Brand brand) {
        brandRepository.save(brand);
    }

    @Transactional
    public void update(Long id, Brand updatedBrand) {
        updatedBrand.setBrandId(id);
        brandRepository.save(updatedBrand);
    }

    @Transactional
    public void delete(Long id) {
        brandRepository.deleteById(id);
    }

    @Cacheable(value = "brandByName", key = "#brandName")
    public Brand findByName(String brandName) {
        log.info("Поиск бренда по имени из базы: {}", brandName);
        return brandRepository.findByBrandName(brandName)
                .orElseThrow(() -> new ResourceNotFoundException("Бренд с именем '" + brandName + "' не найден"));
    }

}
