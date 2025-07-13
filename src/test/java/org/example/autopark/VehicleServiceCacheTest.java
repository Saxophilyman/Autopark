package org.example.autopark;


import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VehicleServiceCacheTest {

    @Autowired
    private VehicleService vehicleService;

    private Long testId;

    @BeforeEach
    void setUp() {
        testId = 2L; // Убедись, что в БД есть авто с таким ID
    }

    @Test
    void testCachingPerformance() {
        System.out.println("=== Тест производительности кэша ===");

        // Первый вызов
        long start1 = System.nanoTime();
        Vehicle vehicle1 = vehicleService.findOne(testId);
        long duration1 = System.nanoTime() - start1;
        assertNotNull(vehicle1);
        System.out.println("⏱ Первый вызов (без кэша): " + duration1 / 1_000_000 + " ms");

        // Второй вызов (должен быть из кэша)
        long start2 = System.nanoTime();
        Vehicle vehicle2 = vehicleService.findOne(testId);
        long duration2 = System.nanoTime() - start2;
        assertNotNull(vehicle2);
        System.out.println("⚡ Второй вызов (с кэшем): " + duration2 + " ns (~" + duration2 / 1_000_000.0 + " ms)");

        assertEquals(vehicle1.getVehicleId(), vehicle2.getVehicleId());
    }
}
