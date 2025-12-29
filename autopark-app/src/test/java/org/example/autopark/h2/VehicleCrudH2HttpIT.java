package org.example.autopark.h2;

import org.example.autopark.dto.BrandDTO;
import org.example.autopark.dto.EnterpriseDTO;
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

class VehicleCrudH2HttpIT extends E2eH2HttpTestBase {

    private static final long MANAGER_ID = 1L;

    // ─────────────────────────────────────────────────────────────────────
    // ЛОГИН МЕНЕДЖЕРА ЧЕРЕЗ /auth/login (form-urlencoded)
    // ─────────────────────────────────────────────────────────────────────

    private String loginAsManager(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", username);
        form.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                url("/auth/login"),
                new HttpEntity<>(form, headers),
                String.class
        );

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
            "/sql/h2/clean-h2.sql",
            "/sql/h2/insert_manager_user-h2.sql",
            "/sql/h2/insert_vehicles-h2.sql"
    })
    void createVehicle_success_onH2() {
        String token = loginAsManager("m1", "password");
        HttpHeaders headers = authHeaders(token);

        VehicleDTO newVehicle = new VehicleDTO();
        // для нового объекта id ДОЛЖЕН быть null – его сгенерирует БД
        newVehicle.setVehicleId(null);
        newVehicle.setVehicleName("Skoda тестовая");
        // валидный номер под твой @Pattern ^[А-Я]\d{3}[А-Я]{2}$
        newVehicle.setLicensePlate("А999ВС");
        newVehicle.setVehicleCost(1_300_000);
        newVehicle.setVehicleYearOfRelease(2022);

        // --- Brand (берём существующий из сидовки, id = 1) ---
        BrandDTO brandDTO = new BrandDTO();
        brandDTO.setBrandId(1L);
        newVehicle.setBrand(brandDTO);

        // --- Enterprise (тоже из сидовки, id = 1) ---
        EnterpriseDTO enterpriseDTO = new EnterpriseDTO();
        enterpriseDTO.setEnterpriseId(1L);
        enterpriseDTO.setTimeZone("Europe/Moscow");
        newVehicle.setEnterprise(enterpriseDTO);

        // --- локальное время покупки предприятия ---
        newVehicle.setPurchaseDateEnterpriseTime("2024-01-01 10:00");

        HttpEntity<VehicleDTO> createRequest = new HttpEntity<>(newVehicle, headers);

        ResponseEntity<String> createResponse = restTemplate.exchange(
                url("/api/managers/" + MANAGER_ID + "/vehicles"),
                HttpMethod.POST,
                createRequest,
                String.class
        );

        System.out.println("CREATE STATUS = " + createResponse.getStatusCode());
        System.out.println("CREATE BODY   = " + createResponse.getBody());

        assertThat(createResponse.getStatusCode())
                .as("Ожидали 201 CREATED от POST /api/managers/{id}/vehicles")
                .isEqualTo(HttpStatus.CREATED);

        // Проверяем, что машина реально появилась в списке /api/vehicles
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
            "/sql/h2/clean-h2.sql",
            "/sql/h2/insert_manager_user-h2.sql",
            "/sql/h2/insert_vehicles-h2.sql"
    })
    void updateVehicle_success_onH2() {
        String token = loginAsManager("m1", "password");
        HttpHeaders headers = authHeaders(token);

        // Берём любую существующую машину из /api/vehicles
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

        // Готовим обновлённый DTO (копируем всё, кроме тех полей, что хотим поменять)
        VehicleDTO updated = new VehicleDTO();
        BeanUtils.copyProperties(existing, updated);

        updated.setVehicleName(existing.getVehicleName() + " (обновлённая)");
        // ещё один валидный госномер
        updated.setLicensePlate("Б777ББ");

        HttpEntity<VehicleDTO> updateRequest = new HttpEntity<>(updated, headers);

        ResponseEntity<String> updateResponse = restTemplate.exchange(
                url("/api/managers/" + MANAGER_ID + "/vehicles/" + id),
                HttpMethod.PUT,
                updateRequest,
                String.class
        );

        assertThat(updateResponse.getStatusCode().is2xxSuccessful())
                .as("Ожидали 2xx от PUT /api/managers/{managerId}/vehicles/{idVehicle}")
                .isTrue();

        // Проверяем, что изменения действительно записались в БД (через менеджерский GET)
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
            "/sql/h2/clean-h2.sql",
            "/sql/h2/insert_manager_user-h2.sql",
            "/sql/h2/insert_vehicles-h2.sql"
    })
    void deleteVehicle_success_onH2() {
        String token = loginAsManager("m1", "password");
        HttpHeaders headers = authHeaders(token);

        // Берём любую машину из списка
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

        // Удаляем как менеджер
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                url("/api/managers/" + MANAGER_ID + "/vehicles/" + idToDelete),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(deleteResponse.getStatusCode())
                .as("Ожидали 204 NO CONTENT от DELETE /api/managers/{managerId}/vehicles/{idVehicle}")
                .isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<VehicleDTO[]> afterListResponse = restTemplate.exchange(
                url("/api/vehicles"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                VehicleDTO[].class
        );

        assertThat(afterListResponse.getStatusCode())
                .as("После удаления /api/vehicles должен отдавать 200 OK")
                .isEqualTo(HttpStatus.OK);

        VehicleDTO[] afterBody = afterListResponse.getBody();
        assertThat(afterBody)
                .as("Тело ответа /api/vehicles после удаления не должно быть null")
                .isNotNull();

        List<VehicleDTO> afterList = Arrays.asList(afterBody);

        assertThat(afterList)
                .as("После удаления машина с id=" + idToDelete + " не должна присутствовать в списке")
                .noneSatisfy(v ->
                        assertThat(v.getVehicleId())
                                .as("vehicleId не должен быть равен удалённому id")
                                .isEqualTo(idToDelete)
                );
    }
}
