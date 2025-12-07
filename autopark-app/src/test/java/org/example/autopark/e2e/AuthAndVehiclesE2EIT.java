package org.example.autopark.e2e;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthAndVehiclesE2EIT extends E2eTestBase {

    /**
     * Хелпер: логинимся менеджером и возвращаем JWT-токен как строку.
     */
    private String loginAsManager(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", username);       // ← должен совпадать с insert_manager_user.sql
        form.add("password", password); // ← тоже из сидовки

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> loginRequest =
                new HttpEntity<>(form, loginHeaders);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                url("/auth/login"),
                loginRequest,
                String.class
        );

        // Здесь мы предполагаем, что при успешном логине приходит 200 и в теле — сам JWT
        assertThat(loginResponse.getStatusCode().is2xxSuccessful())
                .as("Ожидали 2xx от /auth/login")
                .isTrue();

        String token = loginResponse.getBody();
        assertThat(token)
                .as("JWT-токен не должен быть пустым")
                .isNotBlank();

        return token;
    }

    /**
     * Плотный сквозной тест:
     * <p>
     * 1) Поднимает всё приложение (через E2eTestBase).
     * 2) Логинится менеджером через /auth/login.
     * 3) С токеном вызывает:
     * - /api/vehicles
     * - /api/managers/enterprises/1/vehicles
     * - /api/managers/1/drivers
     */
    @Test
    @Sql({
            "/sql/e2e/clean.sql",
            "/sql/e2e/insert_manager_user.sql",
            "/sql/e2e/insert_vehicles.sql"
    })
    void managerFullApiFlow() {
        // 1. Логинимся
        String token = loginAsManager("m1", "password");

        // Заголовки с Bearer-токеном для дальнейших запросов
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token);

        // =========================
        // 2. GET /api/vehicles
        // =========================

        HttpEntity<Void> vehiclesRequest = new HttpEntity<>(authHeaders);

        ResponseEntity<String> debugResponse = restTemplate.exchange(
                url("/api/vehicles"),
                HttpMethod.GET,
                vehiclesRequest,
                String.class
        );

        System.out.println("RAW JSON: " + debugResponse.getBody());

        ResponseEntity<SimpleVehicleDTO[]> vehiclesResponse = restTemplate.exchange(
                url("/api/vehicles"),
                HttpMethod.GET,
                vehiclesRequest,
                SimpleVehicleDTO[].class
        );

        assertThat(vehiclesResponse.getStatusCode())
                .as("Ожидали 200 OK от /api/vehicles")
                .isEqualTo(HttpStatus.OK);

        SimpleVehicleDTO[] bodyArray = vehiclesResponse.getBody();
        assertThat(bodyArray)
                .as("Тело ответа /api/vehicles не должно быть null")
                .isNotNull();

        List<SimpleVehicleDTO> vehicles = Arrays.asList(bodyArray);
        assertThat(vehicles)
                .as("Список машин не должен быть пустым")
                .isNotEmpty();

        assertThat(vehicles)
                .anySatisfy(v -> assertThat(v.getLicensePlate())
                        .as("В JSON-ответе по предприятию должна быть машина А123ВС ")
                        .contains("А123ВС"));

        // ============================================
        // 3. GET /api/managers/enterprises/1/vehicles
        //    (страница машин для предприятия менеджера)
        // ============================================

        String urlEnterpriseVehicles = url(
                "/api/managers/enterprises/1/vehicles" +
                        "?sortField=vehicleName,vehicleId" +
                        "&sortDir=ASC" +
                        "&page=0" +
                        "&size=10"
        );

        ResponseEntity<String> enterpriseVehiclesResponse = restTemplate.exchange(
                urlEnterpriseVehicles,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );

        assertThat(enterpriseVehiclesResponse.getStatusCode())
                .as("Ожидали 200 OK от /api/managers/enterprises/1/vehicles")
                .isEqualTo(HttpStatus.OK);

        String enterpriseVehiclesBody = enterpriseVehiclesResponse.getBody();
        assertThat(enterpriseVehiclesBody)
                .as("Ответ /api/managers/enterprises/1/vehicles не должен быть пустым")
                .isNotBlank();

        assertThat(enterpriseVehiclesBody)
                .as("В JSON-ответе по предприятию должна быть машина А123ВС")
                .contains("А123ВС");

        // ===============================
        // 4. GET /api/managers/1/drivers
        // ===============================

        ResponseEntity<String> driversResponse = restTemplate.exchange(
                url("/api/managers/1/drivers"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );

        assertThat(driversResponse.getStatusCode())
                .as("Ожидали 200 OK от /api/managers/1/drivers")
                .isEqualTo(HttpStatus.OK);
    }

    /**
     * Негативный кейс: неверный пароль → не 2xx.
     * Какой именно код (401/400) — зависит от твоей реализации контроллера.
     */
    @Test
    @Sql({
            "/sql/e2e/clean.sql",
            "/sql/e2e/insert_manager_user.sql"
    })
    void loginWithWrongPasswordShouldFail() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "m1");
        form.add("password", "WRONG_PASSWORD");

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> loginRequest =
                new HttpEntity<>(form, loginHeaders);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                url("/auth/login"),
                loginRequest,
                String.class
        );

        assertThat(loginResponse.getStatusCode().is2xxSuccessful())
                .as("При неверном пароле не должны получать 2xx")
                .isFalse();
    }

    /**
     * Проверяем, что без токена мы не видим бизнес-данные.
     *
     * Из-за formLogin статус может быть 200 (страница логина),
     * поэтому проверяем по содержимому: нет нашей тестовой машины.
     */
    @Test
    @Sql({
            "/sql/e2e/clean.sql",
            "/sql/e2e/insert_manager_user.sql",
            "/sql/e2e/insert_vehicles.sql"
    })
    void accessProtectedEndpointWithoutTokenShouldNotLeakVehicles() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/api/vehicles"),
                String.class
        );

        // 1) Самое главное: без токена не должно быть 2xx
        assertThat(response.getStatusCode().is2xxSuccessful())
                .as("/api/vehicles без токена не должен отдавать 2xx")
                .isFalse();

        // 2) Дополнительная страховка:
        // если вдруг когда-то начнём отдавать 200,
        // то в теле не должно быть секретных данных.
        String body = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && body != null) {
            assertThat(body)
                    .as("При 2xx статусе без токена тело не должно содержать номер A123BC")
                    .doesNotContain("A123BC");
        }
    }



    /**
     * Упрощённый DTO для чтения /api/vehicles.
     * Берём только те поля, которые нам реально нужны в тесте.
     * Jackson спокойно проигнорирует остальные поля из JSON.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) // <- все поля, которых нет в DTO, игнорируются
    static class SimpleVehicleDTO {

        @JsonProperty("vehicleId")  // <- JSON: "vehicleId": 1 → Java: id = 1
        private Long id;

        private String licensePlate;
    }
}
