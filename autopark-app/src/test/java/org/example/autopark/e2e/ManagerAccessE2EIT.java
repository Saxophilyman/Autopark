package org.example.autopark.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E-проверка доступа менеджера к предприятиям:
 *
 * 1) Менеджер m1 с токеном:
 *    - может получить машины своего предприятия (ID = 1);
 * 2) Тот же менеджер не может получить машины "чужого/несуществующего"
 *    предприятия (ID = 999) – ожидаем 4xx.
 */
class ManagerAccessE2EIT extends E2eTestBase {

    // ─────────────────────────────────────────────────────────────
    // ХЕЛПЕРЫ: ЛОГИН ЧЕРЕЗ form-urlencoded + ЗАГОЛОВКИ С TOKEN
    // ─────────────────────────────────────────────────────────────

    private String loginAsManager(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", username);   // m1 — как в insert_manager_user.sql
        form.add("password", password);   // password — как в сидовке

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
                .as("Ожидали 2xx от /auth/login для корректной пары m1/password")
                .isTrue();

        String token = loginResponse.getBody();
        assertThat(token)
                .as("JWT-токен не должен быть пустым")
                .isNotBlank();

        return token;
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);  // Authorization: Bearer <token>
        return headers;
    }

    // ─────────────────────────────────────────────────────────────
    // 1) ПОЗИТИВНЫЙ КЕЙС: СВОЁ ПРЕДПРИЯТИЕ (ID = 1)
    // ─────────────────────────────────────────────────────────────

    @Test
    @Sql({
            "/sql/e2e/clean.sql",
            "/sql/e2e/insert_manager_user.sql",
            "/sql/e2e/insert_vehicles.sql"
    })
    void managerCanAccessOwnEnterpriseVehicles() {
        String token = loginAsManager("m1", "password");
        HttpHeaders headers = authHeaders(token);

        // Предприятие ID=1 — то самое, к которому привязаны тестовые машины (A123BC и т.д.)
        String urlOwn = url(
                "/api/managers/enterprises/1/vehicles" +
                        "?sortField=vehicleName,vehicleId" +
                        "&sortDir=ASC" +
                        "&page=0" +
                        "&size=10"
        );

        ResponseEntity<String> response = restTemplate.exchange(
                urlOwn,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode())
                .as("Для своего предприятия (ID=1) ожидаем 200 OK")
                .isEqualTo(HttpStatus.OK);

        String body = response.getBody();
        assertThat(body)
                .as("Ответ по своему предприятию не должен быть пустым")
                .isNotBlank();

        // Тут мы завязываемся на конкретные данные из insert_vehicles.sql:
        assertThat(body)
                .as("В JSON по своему предприятию должна быть тестовая машина А123ВС")
                .contains("А123ВС");
    }

    // ─────────────────────────────────────────────────────────────
    // 2) НЕГАТИВНЫЙ КЕЙС: ЧУЖОЕ/НЕСУЩЕСТВУЮЩЕЕ ПРЕДПРИЯТИЕ (ID = 999)
    // ─────────────────────────────────────────────────────────────

    @Test
    @Sql({
            "/sql/e2e/clean.sql",
            "/sql/e2e/insert_manager_user.sql",
            "/sql/e2e/insert_vehicles.sql"
    })
    void managerCannotAccessForeignOrNonExistingEnterpriseVehicles() {
        String token = loginAsManager("m1", "password");
        HttpHeaders headers = authHeaders(token);

        // ID=999 — намеренно берём предприятие, которого нет в сидовке
        // (или которое будет считаться "чужим").
        String urlForeign = url(
                "/api/managers/enterprises/999/vehicles" +
                        "?sortField=vehicleName,vehicleId" +
                        "&sortDir=ASC" +
                        "&page=0" +
                        "&size=10"
        );

        ResponseEntity<String> response = restTemplate.exchange(
                urlForeign,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Здесь не жёстко привязываемся к 403 или 404:
        // главное — это не успешный 2xx, а именно клиентская ошибка.
        assertThat(response.getStatusCode().is4xxClientError())
                .as("Для чужого/несуществующего предприятия должен быть 4xx (404/403 и т.п.)")
                .isTrue();
    }
}
