package org.example.autopark.repository;

import org.example.autopark.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByBrandName(String brandName);


//    Optional<Brand> findByNameIgnoreCase(String name);

}
