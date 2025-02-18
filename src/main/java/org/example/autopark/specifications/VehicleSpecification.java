package org.example.autopark.specifications;

import org.example.autopark.entity.Vehicle;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class VehicleSpecification {

    public static Specification<Vehicle> hasAnyEnterprise(List<Long> enterpriseIds) {
        return (root, query, criteriaBuilder) -> {
            if (enterpriseIds == null || enterpriseIds.isEmpty()) {
                return criteriaBuilder.conjunction(); // Без фильтра
            }
            return root.get("enterpriseOwnerOfVehicle").get("enterpriseId").in(enterpriseIds);
        };
    }

    // Фильтрация по конкретному enterpriseId или по умолчанию первому в списке
    public static Specification<Vehicle> hasEnterpriseOrDefault(Long enterpriseId, List<Long> enterpriseIds) {
        return (root, query, criteriaBuilder) -> {
            if (enterpriseId != null) {
                return criteriaBuilder.equal(root.get("enterpriseOwnerOfVehicle").get("enterpriseId"), enterpriseId);
            } else if (enterpriseIds != null && !enterpriseIds.isEmpty()) {
                return criteriaBuilder.equal(root.get("enterpriseOwnerOfVehicle").get("enterpriseId"), enterpriseIds.get(0));
            }
            return criteriaBuilder.conjunction(); // Без фильтрации
        };
    }

    public static Specification<Vehicle> hasEnterprise(Long enterpriseId) {
        return (root, query, criteriaBuilder) ->
                enterpriseId == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("enterpriseOwnerOfVehicle").get("enterpriseId"), enterpriseId);
    }

    public static Specification<Vehicle> hasBrand(Long brandId) {
        return (root, query, criteriaBuilder) ->
                brandId == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("brandOwner").get("brandId"), brandId);
    }

    public static Specification<Vehicle> hasMinPrice(Integer minPrice) {
        return (root, query, criteriaBuilder) ->
                minPrice == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.greaterThanOrEqualTo(root.get("vehicleCost"), minPrice);
    }

    public static Specification<Vehicle> hasMaxPrice(Integer maxPrice) {
        return (root, query, criteriaBuilder) ->
                maxPrice == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.lessThanOrEqualTo(root.get("vehicleCost"), maxPrice);
    }

    public static Specification<Vehicle> hasYear(Integer year) {
        return (root, query, criteriaBuilder) ->
                year == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("vehicleYearOfRelease"), year);
    }
}