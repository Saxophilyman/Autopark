//package org.example.autopark.config;
//
//import org.example.autopark.service.VehicleService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class CacheBenchmarkRunner implements CommandLineRunner {
//
//    private final VehicleService vehicleService;
//
//    public CacheBenchmarkRunner(VehicleService vehicleService) {
//        this.vehicleService = vehicleService;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        Long testId = 2L; // убедись, что такой id есть в базе
//
//        System.out.println("=== Замер времени выполнения метода findOne(Long) ===");
//
//        System.out.println(">>> Первый вызов (без кэша):");
//        long start1 = System.nanoTime();
//        vehicleService.findOne(testId);
//        long duration1 = System.nanoTime() - start1;
//        System.out.println("Время первого вызова: " + duration1 / 1_000_000 + " ms");
//
//        System.out.println(">>> Второй вызов (с кэшем):");
//        long start2 = System.nanoTime();
//        vehicleService.findOne(testId);
//        long duration2 = System.nanoTime() - start2;
//        System.out.println("Время второго вызова: " + duration2 + " ns");
////        System.out.println("Время второго вызова: " + duration2 / 1_000_000 + " ms");
//    }
//}