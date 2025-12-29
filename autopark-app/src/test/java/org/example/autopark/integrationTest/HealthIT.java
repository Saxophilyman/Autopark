package org.example.autopark.integrationTest;


import org.example.autopark.integrationTest.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthIT extends IntegrationTestBase {

    @Test
    void appIsHealthy() {
        ResponseEntity<Map> resp = http.getForEntity(url("/actuator/health"), Map.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().get("status")).isEqualTo("UP");
    }
}

