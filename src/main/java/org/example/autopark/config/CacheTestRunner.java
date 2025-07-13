//package org.example.autopark.config;
//import org.example.autopark.service.VehicleService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class CacheTestRunner {
//
//    @Bean
//    public CommandLineRunner runCacheTest(VehicleService vehicleService) {
//        return args -> {
//            System.out.println("=== Кэш отключён? Смотри @Cacheable ===");
//
//            System.out.println(">>> Первый вызов:");
//            vehicleService.findOne(1L); // Первый раз — запрос в БД
//
//            System.out.println(">>> Второй вызов:");
//            vehicleService.findOne(1L); // Второй раз — должен быть из кэша
//        };
//    }
//}
