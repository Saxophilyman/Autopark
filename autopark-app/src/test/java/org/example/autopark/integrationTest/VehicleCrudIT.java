package org.example.autopark.integrationTest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.autopark.integrationTest.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleCrudIT extends IntegrationTestBase {

    /**
     * Сквозной сценарий:
     *  1) Создаём машину через /api/managers/1/vehicles
     *  2) Читаем список через /api/vehicles и находим нашу машину
     *  3) Читаем её отдельно через /api/vehicles/{id}
     *  4) Удаляем через /api/managers/1/vehicles/{id}
     *  5) Проверяем, что в списке /api/vehicles её больше нет
     */
    @Test
    @Sql({"/sql/enterprise_seed.sql", "/sql/manager_seed.sql"})
    void create_read_delete_vehicle_via_http() {
        // 1. Создаём авто через менеджерский API.
        var req = new VehicleCreate(
                "Kalina",              // vehicleName
                "Ф001АИ",             // licensePlate
                500_000,              // vehicleCost
                2015,                 // vehicleYearOfRelease
                new EnterpriseRef(1L) // enterprise.enterpriseId = 1
        );

        var createResp = http.postForEntity(
                url("/api/managers/{managerId}/vehicles"),
                req,
                String.class,
                1L // {managerId}
        );

        assertThat(createResp.getStatusCode())
                .as("POST /api/managers/1/vehicles должен вернуть 201 CREATED")
                .isEqualTo(HttpStatus.CREATED);

        // 2. Читаем список машин и находим только что созданную по номеру
        var listResp = http.getForEntity(
                url("/api/vehicles"),
                VehicleDto[].class
        );

        assertThat(listResp.getStatusCode().is2xxSuccessful())
                .as("GET /api/vehicles должен вернуть 2xx")
                .isTrue();

        var list = Arrays.asList(listResp.getBody());
        var created = list.stream()
                .filter(v -> "Ф001АИ".equals(v.getLicensePlate()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Созданная машина с номером Ф001АИ не найдена в /api/vehicles"));

        Long id = created.getVehicleId();

        // 3. Дополнительно проверяем GET /api/vehicles/{id}
        var getResp = http.getForEntity(
                url("/api/vehicles/{id}"),
                VehicleDto.class,
                id
        );

        assertThat(getResp.getStatusCode().is2xxSuccessful())
                .as("GET /api/vehicles/{id} должен вернуть 2xx")
                .isTrue();
        assertThat(getResp.getBody().getLicensePlate())
                .as("Номер машины должен совпадать")
                .isEqualTo("Ф001АИ");

        // 4. Удаляем машину через менеджерский API
        var deleteUrl = url("/api/managers/{managerId}/vehicles/{vehicleId}");
        http.delete(deleteUrl, 1L, id);

        // 5. Проверяем, что в списке машин её больше нет
        var afterListResp = http.getForEntity(
                url("/api/vehicles"),
                VehicleDto[].class
        );

        assertThat(afterListResp.getStatusCode().is2xxSuccessful())
                .isTrue();

        var afterList = Arrays.asList(afterListResp.getBody());
        assertThat(afterList)
                .as("После удаления машина не должна присутствовать в списке /api/vehicles")
                .noneMatch(v -> id.equals(v.getVehicleId()));
    }

    /**
     * Негативный сценарий:
     *  - отправляем машину с заведомо неверным годом (1700)
     *  - ожидаем 400 BAD_REQUEST + текст ошибки из @Min
     */
    @Test
    @Sql({"/sql/enterprise_seed.sql", "/sql/manager_seed.sql"})
    void validation_error_on_bad_year() {
        var bad = new VehicleCreate(
                "BadCar",
                "Ф002АИ",
                100_000,
                1700,              // неправильный год
                new EnterpriseRef(1L)
        );

        var resp = http.postForEntity(
                url("/api/managers/{managerId}/vehicles"),
                bad,
                String.class,
                1L
        );

        assertThat(resp.getStatusCode())
                .as("При некорректном году выпуска должен быть 400 BAD_REQUEST")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(resp.getBody())
                .as("Ответ должен содержать текст валидационной ошибки по году")
                .contains("Транспорт может быть только старше 1900 г.");
    }

    // ------------------------
    // Вспомогательные DTO для теста
    // ------------------------

    /**
     * Мини-DTO для вложенного enterprise в JSON.
     * JSON будет вида:
     * {
     *   "vehicleName": "...",
     *   "licensePlate": "...",
     *   "vehicleCost": 500000,
     *   "vehicleYearOfRelease": 2015,
     *   "enterprise": { "enterpriseId": 1 }
     * }
     */
    @Value
    static class EnterpriseRef {
        Long enterpriseId;
    }

    /**
     * DTO, совпадающее по полям с твоим VehicleDTO
     * в части, которая нам нужна для запроса.
     */
    @Value
    static class VehicleCreate {
        String vehicleName;
        String licensePlate;
        int vehicleCost;
        int vehicleYearOfRelease;
        EnterpriseRef enterprise;
    }

    /**
     * DTO для чтения ответа от /api/vehicles.
     * Держим только те поля, которые реально проверяем.
     * ВАЖНО: здесь нужен пустой конструктор и сеттеры для Jackson.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class VehicleDto {
        private Long vehicleId;
        private String vehicleName;
        private String licensePlate;
        private Integer vehicleYearOfRelease;
    }
}
