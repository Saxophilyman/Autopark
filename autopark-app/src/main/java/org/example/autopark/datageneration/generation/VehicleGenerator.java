package org.example.autopark.datageneration.generation;

import org.example.autopark.datageneration.model.GenerationContext;
import org.example.autopark.datageneration.model.VehicleDraft;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Component
@Profile("!reactive")
public class VehicleGenerator {

    private static final int MIN_COST = 100_000;
    private static final int MAX_COST = 1_000_000;
    private static final int MIN_YEAR = 2018;
    private static final int MAX_YEAR = 2024;
    private static final int MAX_LICENSE_PLATE_ATTEMPTS = 10_000;
    private static final String LICENSE_PLATE_LETTERS = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ";

    private static final Map<String, List<String>> MODELS_BY_BRAND = createModelCatalog();

    private final Random random = new Random();

    public List<VehicleDraft> generate(
            GenerationContext context,
            int vehicleCount,
            Set<String> occupiedLicensePlates
    ) {
        if (vehicleCount < 0) {
            throw new IllegalArgumentException("Число автомобилей не может быть отрицательным");
        }

        List<String> models = MODELS_BY_BRAND.get(context.brandName());
        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("Неизвестный бренд для генерации: " + context.brandName());
        }

        Set<String> reservedLicensePlates = new HashSet<>(occupiedLicensePlates);
        List<VehicleDraft> result = new ArrayList<>(vehicleCount);

        for (int i = 0; i < vehicleCount; i++) {
            result.add(new VehicleDraft(
                    randomElement(models),
                    generateUniqueLicensePlate(reservedLicensePlates),
                    randomIntInclusive(MIN_COST, MAX_COST),
                    randomIntInclusive(MIN_YEAR, MAX_YEAR),
                    context.brandId()
            ));
        }

        return List.copyOf(result);
    }

    private String generateUniqueLicensePlate(Set<String> reservedLicensePlates) {
        for (int attempt = 0; attempt < MAX_LICENSE_PLATE_ATTEMPTS; attempt++) {
            String licensePlate = generateLicensePlate();
            if (reservedLicensePlates.add(licensePlate)) {
                return licensePlate;
            }
        }

        throw new IllegalStateException("Не удалось сгенерировать уникальный номер автомобиля");
    }

    private String generateLicensePlate() {
        char firstLetter = randomLetter();
        int digits = random.nextInt(1_000);
        char secondLetter = randomLetter();
        char thirdLetter = randomLetter();

        return "%c%03d%c%c".formatted(firstLetter, digits, secondLetter, thirdLetter);
    }

    private char randomLetter() {
        return LICENSE_PLATE_LETTERS.charAt(random.nextInt(LICENSE_PLATE_LETTERS.length()));
    }

    private String randomElement(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }

    private int randomIntInclusive(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private static Map<String, List<String>> createModelCatalog() {
        Map<String, List<String>> catalog = new HashMap<>();
        catalog.put("BMW", List.of(
                "118i", "220i", "M240i xDrive", "330i", "520i", "X6", "X5",
                "Z4", "iX", "i7", "M5", "M3"
        ));
        catalog.put("Lada", List.of(
                "XRAY Cross", "Niva Travel", "Vesta Cross", "Granta Cross",
                "Granta Drive Active", "Granta Classic", "Vesta NG", "Niva Legend"
        ));
        catalog.put("Toyota", List.of(
                "Corolla", "Camry", "Avalon", "Yaris", "Auris", "RAV4", "Highlander",
                "Land Cruiser 300", "Hilux", "Alphard"
        ));
        return Map.copyOf(catalog);
    }
}
