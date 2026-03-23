package org.example.notify.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestConfig {

    @Bean("autoparkRestClient")
    @Primary
    RestClient autoparkRestClient(
            @Value("${autopark.base-url:http://localhost:8080}") String baseUrl,
            @Value("${internal.api.token:dev-token}") String token
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Token", token)
                .build();
    }

    @Bean("telegramRestClient")
    RestClient telegramRestClient() {
        return RestClient.builder().baseUrl("https://api.telegram.org").build();
    }
}
