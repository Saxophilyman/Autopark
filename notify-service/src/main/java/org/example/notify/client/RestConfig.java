package org.example.notify.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestConfig {

    @Bean
    RestClient restClient(@Value("${autopark.base-url:http://localhost:8080}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}

