package org.example.autopark.h2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

class AuthAndVehiclesH2HttpIT extends E2eH2HttpTestBase {

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

        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String token = loginResponse.getBody();
        assertThat(token).isNotBlank();
        return token;
    }

    @Test
    @Sql({
            "/sql/h2/clean-h2.sql",
            "/sql/h2/insert_manager_user-h2.sql",
            "/sql/h2/insert_vehicles-h2.sql"
    })
    void managerCanSeeVehicles_onH2() {
        String token = loginAsManager("m1", "password");

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token);

        ResponseEntity<SimpleVehicleDTO[]> response = restTemplate.exchange(
                url("/api/vehicles"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                SimpleVehicleDTO[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SimpleVehicleDTO> vehicles = Arrays.asList(response.getBody());
        assertThat(vehicles).isNotEmpty();
        assertThat(vehicles)
                .anySatisfy(v -> assertThat(v.getLicensePlate()).contains("А123ВС"));
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SimpleVehicleDTO {
        @JsonProperty("vehicleId")
        private Long id;
        private String licensePlate;
    }
}
