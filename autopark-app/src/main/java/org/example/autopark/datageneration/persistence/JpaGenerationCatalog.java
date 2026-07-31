package org.example.autopark.datageneration.persistence;

import org.example.autopark.datageneration.contract.GenerationCatalog;
import org.example.autopark.datageneration.model.GenerationContext;
import org.example.autopark.entity.Brand;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.exception.ResourceNotFoundException;
import org.example.autopark.repository.BrandRepository;
import org.example.autopark.repository.EnterpriseRepository;
import org.example.autopark.repository.VehicleRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("!reactive")
public class JpaGenerationCatalog implements GenerationCatalog {

    private static final Map<String, String> BRAND_BY_ENTERPRISE = Map.of(
            "BMW AG", "BMW",
            "Автоваз", "Lada",
            "Toyota Motor Corporation", "Toyota"
    );

    private final EnterpriseRepository enterpriseRepository;
    private final BrandRepository brandRepository;
    private final VehicleRepository vehicleRepository;

    public JpaGenerationCatalog(
            EnterpriseRepository enterpriseRepository,
            BrandRepository brandRepository,
            VehicleRepository vehicleRepository
    ) {
        this.enterpriseRepository = enterpriseRepository;
        this.brandRepository = brandRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public GenerationContext load(Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Предприятие с id " + enterpriseId + " не найдено"
                ));

        String brandName = BRAND_BY_ENTERPRISE.get(enterprise.getName());
        if (brandName == null) {
            throw new IllegalArgumentException(
                    "Для предприятия '" + enterprise.getName() + "' не настроен бренд генерации"
            );
        }

        Brand brand = brandRepository.findByBrandName(brandName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Бренд '" + brandName + "' не найден"
                ));

        Set<String> occupiedLicensePlates = vehicleRepository.findAll().stream()
                .map(Vehicle::getLicensePlate)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());

        return new GenerationContext(
                enterprise.getEnterpriseId(),
                enterprise.getName(),
                brand.getBrandId(),
                brand.getBrandName(),
                occupiedLicensePlates
        );
    }
}
