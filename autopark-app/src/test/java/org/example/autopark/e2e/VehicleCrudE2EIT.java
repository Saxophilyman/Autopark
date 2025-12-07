package org.example.autopark.e2e;

import org.example.autopark.dto.BrandDTO;
import org.example.autopark.dto.EnterpriseDTO;
import org.example.autopark.dto.VehicleApiDto;
import org.example.autopark.dto.VehicleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleCrudE2EIT extends E2eTestBase {

    // ID менеджера из сидовки insert_manager_user.sql
    private static final long MANAGER_ID = 1L;

    // ─────────────────────────────────────────────────────────────────────
    // ЛОГИН МЕНЕДЖЕРА ЧЕРЕЗ /auth/login (form-urlencoded)
    // ─────────────────────────────────────────────────────────────────────

    private String loginAsManager(String username, String password) {
        // Формируем тело как обычную HTML-форму:
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", username);   // должно совпадать с insert_manager_user.sql
        form.add("password", password);   // то же самое

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> loginRequest =
                new HttpEntity<>(form, loginHeaders);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                url("/auth/login"),
                loginRequest,
                String.class
        );

        // Ожидаем успешный логин (2xx) и JWT в теле ответа
        assertThat(loginResponse.getStatusCode().is2xxSuccessful())
                .as("Ожидали 2xx от /auth/login")
                .isTrue();

        String token = loginResponse.getBody();
        assertThat(token)
                .as("JWT-токен не должен быть пустым")
                .isNotBlank();

        return token;
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 1) CREATE: POST /api/managers/{id}/vehicles
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @Sql({
            "/sql/e2e/clean.sql",
            "/sql/e2e/insert_manager_user.sql",
            "/sql/e2e/insert_vehicles.sql"
    })
    void createVehicle_success() {
        String token = loginAsManager("m1", "password");
        HttpHeaders headers = authHeaders(token);

        VehicleDTO newVehicle = new VehicleDTO();
// ДОЛЖНО быть null, а не 1L
        newVehicle.setVehicleId(null);
        newVehicle.setVehicleName("Skoda тестовая");
        newVehicle.setLicensePlate("А999ВС");
        newVehicle.setVehicleCost(1_300_000);
        newVehicle.setVehicleYearOfRelease(2022);

// --- Brand ---
        BrandDTO brandDTO = new BrandDTO();
        brandDTO.setBrandId(1L);
        newVehicle.setBrand(brandDTO);

// --- Enterprise ---
        EnterpriseDTO enterpriseDTO = new EnterpriseDTO();
        enterpriseDTO.setEnterpriseId(1L);
        enterpriseDTO.setTimeZone("Europe/Moscow");
        newVehicle.setEnterprise(enterpriseDTO);

// --- Локальное время покупки ---
        newVehicle.setPurchaseDateEnterpriseTime("2024-01-01 10:00");
        HttpEntity<VehicleDTO> createRequest = new HttpEntity<>(newVehicle, headers);
        ResponseEntity<String> createResponse = restTemplate.exchange(
                url("/api/managers/" + MANAGER_ID + "/vehicles"),
                HttpMethod.POST,
                createRequest,
                String.class
        );
        System.out.println("STATUS = " + createResponse.getStatusCode());
        System.out.println("BODY   = " + createResponse.getBody());
        assertThat(createResponse.getStatusCode())
                .as("Ожидали 201 CREATED от POST /api/managers/{id}/vehicles")
                .isEqualTo(HttpStatus.CREATED);

        // Дальше проверка, что машина реально появилась
        ResponseEntity<VehicleDTO[]> afterListResponse = restTemplate.exchange(
                url("/api/vehicles"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                VehicleDTO[].class
        );

        assertThat(afterListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        VehicleDTO[] afterBody = afterListResponse.getBody();
        assertThat(afterBody).isNotNull();

        List<VehicleDTO> vehicles = Arrays.asList(afterBody);

        assertThat(vehicles)
                .anySatisfy(v -> {
                    assertThat(v.getLicensePlate())
                            .as("После создания должна быть машина с номером А999ВС")
                            .isEqualTo("А999ВС");
                    assertThat(v.getVehicleName())
                            .as("Имя машины должно совпасть")
                            .isEqualTo("Skoda тестовая");
                });
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2) UPDATE: PUT /api/managers/{id}/vehicles/{idVehicle}
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @Sql({
            "/sql/e2e/clean.sql",
            "/sql/e2e/insert_manager_user.sql",
            "/sql/e2e/insert_vehicles.sql"
    })
    void updateVehicle_success() {
        String token = loginAsManager("m1", "password");
        HttpHeaders headers = authHeaders(token);

        // Берём существующую машину (например, Camry из сидовки) — можно через общий список
        ResponseEntity<VehicleDTO[]> listResponse = restTemplate.exchange(
                url("/api/vehicles"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                VehicleDTO[].class
        );

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        VehicleDTO[] body = listResponse.getBody();
        assertThat(body).isNotNull();
        assertThat(body).isNotEmpty();

        VehicleDTO existing = body[0];
        Long id = existing.getVehicleId();

        // Готовим обновлённый DTO
        VehicleDTO updated = new VehicleDTO();
        BeanUtils.copyProperties(existing, updated);

        updated.setVehicleName(existing.getVehicleName() + " (обновлённая)");
        // Новый валидный номер по шаблону ^[А-Я]\d{3}[А-Я]{2}$
        updated.setLicensePlate("Б777ББ");

        HttpEntity<VehicleDTO> updateRequest = new HttpEntity<>(updated, headers);

        // Обновляем через менеджерский эндпоинт
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                url("/api/managers/" + MANAGER_ID + "/vehicles/" + id),
                HttpMethod.PUT,
                updateRequest,
                String.class // тело нам не важно, главное — статус
        );

        assertThat(updateResponse.getStatusCode().is2xxSuccessful())
                .as("Ожидали 2xx от PUT /api/managers/{managerId}/vehicles/{idVehicle}")
                .isTrue();

        // Проверяем, что данные реально обновились — тоже через менеджерский эндпоинт
        ResponseEntity<VehicleDTO> getResponse = restTemplate.exchange(
                url("/api/managers/" + MANAGER_ID + "/vehicles/" + id),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                VehicleDTO.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        VehicleDTO after = getResponse.getBody();
        assertThat(after).isNotNull();
        assertThat(after.getLicensePlate()).isEqualTo("Б777ББ");
        assertThat(after.getVehicleName()).endsWith("(обновлённая)");
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3) DELETE: DELETE /api/managers/{id}/vehicles/{idVehicle}
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @Sql({
            "/sql/e2e/clean.sql",
            "/sql/e2e/insert_manager_user.sql",
            "/sql/e2e/insert_vehicles.sql"
    })
    void deleteVehicle_success() {
        String token = loginAsManager("m1", "password");
        HttpHeaders headers = authHeaders(token);

        // Берём любую существующую машину из общего списка
        ResponseEntity<VehicleDTO[]> listResponse = restTemplate.exchange(
                url("/api/vehicles"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                VehicleDTO[].class
        );

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        VehicleDTO[] body = listResponse.getBody();
        assertThat(body).isNotNull();
        assertThat(body).isNotEmpty();

        Long idToDelete = body[0].getVehicleId();

        // Удаляем её как менеджер
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                url("/api/managers/" + MANAGER_ID + "/vehicles/" + idToDelete),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(deleteResponse.getStatusCode())
                .as("Ожидали 204 NO CONTENT от DELETE /api/managers/{managerId}/vehicles/{idVehicle}")
                .isEqualTo(HttpStatus.NO_CONTENT);

        // Проверяем, что теперь GET через менеджерский эндпоинт возвращает 4xx (404)
        ResponseEntity<String> afterDeleteResponse = restTemplate.exchange(
                url("/api/managers/" + MANAGER_ID + "/vehicles/" + idToDelete),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(afterDeleteResponse.getStatusCode().is4xxClientError())
                .as("После удаления ожидаем 4xx от GET /api/managers/{managerId}/vehicles/{idVehicle}")
                .isTrue();
    }
}