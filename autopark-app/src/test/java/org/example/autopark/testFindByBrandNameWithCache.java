package org.example.autopark;

import org.example.autopark.entity.Brand;
import org.example.autopark.service.BrandsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BrandCacheTest {

    @Autowired
    private BrandsService brandsService;

    @Test
    void testFindByBrandNameWithCache() {
        String brandName = "Toyota"; // убедись, что есть в базе

        // Первый вызов — без кэша
        long start1 = System.nanoTime();
        Brand first = brandsService.findByName(brandName);
        long duration1 = System.nanoTime() - start1;

        // Второй вызов — с кэшем
        long start2 = System.nanoTime();
        Brand second = brandsService.findByName(brandName);
        long duration2 = System.nanoTime() - start2;

        System.out.println("Первый вызов (без кэша): " + duration1 / 1_000_000 + " мс");
        System.out.println("Время второго вызова: " + duration2 + " ns (" + duration2 / 1_000_000.0 + " ms)");
//        System.out.println("⚡ Второй вызов (с кэшем): " + duration2 / 1_000_000 + " мс");

        Assertions.assertEquals(first.getBrandId(), second.getBrandId());
    }
}
