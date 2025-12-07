package org.example.autopark.h2;

import jakarta.validation.ConstraintViolationException;
import org.example.autopark.entity.Brand;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.TypeVehicle;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test-h2-http")
class VehicleH2SmokeIT {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TestEntityManager em;
    /**
     * 1. Базовый дымовой тест:
     * - сохраняем Brand, Enterprise, Vehicle
     * - читаем Vehicle из репозитория и проверяем поля и связи.
     */
    @Test
    void canSaveAndLoadVehicleOnH2() {
        // Подготовим бренд и предприятие — простым образом, без лишней логики
        Brand brand = new Brand();
        brand.setBrandName("Lada");
        brand.setBrandType(TypeVehicle.CAR);
        brand.setCapacityFuelTank(50);
        brand.setLoadCapacity(400);
        brand.setNumberOfSeats(4);
        em.persist(brand);

        Enterprise enterprise = new Enterprise();
        enterprise.setName("Автоваз");
        enterprise.setCityOfEnterprise("Москва");
        enterprise.setTimeZone("Europe/Moscow");
        em.persist(enterprise);

        // Теперь создаём транспорт
        Vehicle v = new Vehicle();
        v.setVehicleName("H2 тестовое ТС");
        v.setLicensePlate("А777ВС");
        v.setVehicleCost(1_000_000);
        v.setVehicleYearOfRelease(2020);
        v.setPurchaseDateUtc(Instant.now());
        v.setBrandOwner(brand);
        v.setEnterpriseOwnerOfVehicle(enterprise);

        Vehicle saved = vehicleRepository.save(v);

        assertThat(saved.getVehicleId()).isNotNull();

        // Читаем обратно из репозитория
        Vehicle found = vehicleRepository.findById(saved.getVehicleId())
                .orElseThrow(() -> new IllegalStateException("Vehicle not found in H2"));

        assertThat(found.getVehicleName()).isEqualTo("H2 тестовое ТС");
        assertThat(found.getLicensePlate()).isEqualTo("А777ВС");
        assertThat(found.getEnterpriseOwnerOfVehicle().getName()).isEqualTo("Автоваз");
    }

    /**
     * 2. Содержательный тест №1:
     * Один Brand, два предприятия и три ТС:
     * - 2 машины у Enterprise-1
     * - 1 машина у Enterprise-2
     *
     * Проверяем, что связи Vehicle → Enterprise сохраняются и читаются корректно.
     */
    @Test
    void canStoreSeveralVehiclesForSameEnterprise() {
        Brand brand = new Brand();
        brand.setBrandName("GAZ");
        brand.setBrandType(TypeVehicle.CAR);     // любой существующий enum
        brand.setCapacityFuelTank(120);
        brand.setLoadCapacity(3_000);
        brand.setNumberOfSeats(2);
        em.persist(brand);

        Enterprise e1 = new Enterprise();
        e1.setName("Enterprise-1");
        e1.setCityOfEnterprise("Москва");
        e1.setTimeZone("Europe/Moscow");
        em.persist(e1);

        Enterprise e2 = new Enterprise();
        e2.setName("Enterprise-2");
        e2.setCityOfEnterprise("Тверь");
        e2.setTimeZone("Europe/Moscow");
        em.persist(e2);

        Vehicle v1 = new Vehicle();
        v1.setVehicleName("ТС-1");
        v1.setLicensePlate("Н111ПЕ");
        v1.setVehicleCost(500_000);
        v1.setVehicleYearOfRelease(2018);
        v1.setPurchaseDateUtc(Instant.now());
        v1.setBrandOwner(brand);
        v1.setEnterpriseOwnerOfVehicle(e1);

        Vehicle v2 = new Vehicle();
        v2.setVehicleName("ТС-2");
        v2.setLicensePlate("Н123ПЕ");
        v2.setVehicleCost(600_000);
        v2.setVehicleYearOfRelease(2019);
        v2.setPurchaseDateUtc(Instant.now());
        v2.setBrandOwner(brand);
        v2.setEnterpriseOwnerOfVehicle(e1);

        Vehicle v3 = new Vehicle();
        v3.setVehicleName("ТС-3");
        v3.setLicensePlate("Н131ПЕ");
        v3.setVehicleCost(700_000);
        v3.setVehicleYearOfRelease(2020);
        v3.setPurchaseDateUtc(Instant.now());
        v3.setBrandOwner(brand);
        v3.setEnterpriseOwnerOfVehicle(e2);

        vehicleRepository.saveAll(List.of(v1, v2, v3));

        List<Vehicle> all = vehicleRepository.findAll();
        assertThat(all).hasSize(3);

        long forEnterprise1 = all.stream()
                .filter(v -> v.getEnterpriseOwnerOfVehicle().getName().equals("Enterprise-1"))
                .count();

        long forEnterprise2 = all.stream()
                .filter(v -> v.getEnterpriseOwnerOfVehicle().getName().equals("Enterprise-2"))
                .count();

        assertThat(forEnterprise1).isEqualTo(2);
        assertThat(forEnterprise2).isEqualTo(1);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. UPDATE: меняем поля и проверяем, что изменения сохранились
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void canUpdateVehicleOnH2() {
        // 1) подготовка: бренд + предприятие + ТС
        Brand brand = new Brand();
        brand.setBrandName("Skoda");
        brand.setBrandType(TypeVehicle.CAR);
        brand.setCapacityFuelTank(55);
        brand.setLoadCapacity(450);
        brand.setNumberOfSeats(5);
        em.persist(brand);

        Enterprise enterprise = new Enterprise();
        enterprise.setName("Enterprise-Update");
        enterprise.setCityOfEnterprise("Москва");
        enterprise.setTimeZone("Europe/Moscow");
        em.persist(enterprise);

        Vehicle v = new Vehicle();
        v.setVehicleName("До обновления");
        v.setLicensePlate("Р111РР");
        v.setVehicleCost(900_000);
        v.setVehicleYearOfRelease(2019);
        v.setPurchaseDateUtc(Instant.now());
        v.setBrandOwner(brand);
        v.setEnterpriseOwnerOfVehicle(enterprise);

        Vehicle saved = vehicleRepository.save(v);

        // 2) обновляем сущность
        saved.setVehicleName("После обновления");
        saved.setLicensePlate("Р222РР");     // другой валидный номер
        saved.setVehicleCost(950_000);

        Vehicle updated = vehicleRepository.save(saved);

        // 3) читаем из базы заново и проверяем
        Vehicle reloaded = vehicleRepository.findById(updated.getVehicleId())
                .orElseThrow(() -> new IllegalStateException("Vehicle not found after update"));

        assertThat(reloaded.getVehicleName()).isEqualTo("После обновления");
        assertThat(reloaded.getLicensePlate()).isEqualTo("Р222РР");
        assertThat(reloaded.getVehicleCost()).isEqualTo(950_000);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. DELETE: удаляем Vehicle и проверяем, что его больше нет
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void canDeleteVehicleOnH2() {
        Brand brand = new Brand();
        brand.setBrandName("VW");
        brand.setBrandType(TypeVehicle.CAR);
        brand.setCapacityFuelTank(60);
        brand.setLoadCapacity(500);
        brand.setNumberOfSeats(5);
        em.persist(brand);

        Enterprise enterprise = new Enterprise();
        enterprise.setName("Enterprise-Delete");
        enterprise.setCityOfEnterprise("Казань");
        enterprise.setTimeZone("Europe/Moscow");
        em.persist(enterprise);

        Vehicle v = new Vehicle();
        v.setVehicleName("Удаляемое ТС");
        v.setLicensePlate("О333ОО");
        v.setVehicleCost(800_000);
        v.setVehicleYearOfRelease(2017);
        v.setPurchaseDateUtc(Instant.now());
        v.setBrandOwner(brand);
        v.setEnterpriseOwnerOfVehicle(enterprise);

        Vehicle saved = vehicleRepository.save(v);
        Long id = saved.getVehicleId();
        assertThat(id).isNotNull();

        // удаляем
        vehicleRepository.deleteById(id);

        Optional<Vehicle> afterDelete = vehicleRepository.findById(id);
        assertThat(afterDelete).isEmpty();
    }

    /**
     * 3. Содержательный тест №2:
     * Проверяем, что Bean Validation реально работает на уровне JPA:
     * Brand.capacityFuelTank помечен @Min(0), мы пытаемся сохранить -10
     * и ожидаем ConstraintViolationException.
     */
    @Test
    void brandValidationFailsOnNegativeCapacity() {
        Brand brand = new Brand();
        brand.setBrandName("BadBrand");
        brand.setBrandType(TypeVehicle.CAR);
        brand.setCapacityFuelTank(-10); // нарушаем @Min(0)
        brand.setLoadCapacity(500);
        brand.setNumberOfSeats(4);

        assertThatThrownBy(() -> em.persistAndFlush(brand))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
