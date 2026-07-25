package org.example.autopark.monitoring.config;

import lombok.RequiredArgsConstructor;
import org.example.autopark.monitoring.MonitoringHttpClientInterceptor;
import org.example.autopark.monitoring.MonitoringMetrics;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class MonitoringRestTemplateConfig {

    private final MonitoringMetrics metrics;

    @Bean
    public MonitoringHttpClientInterceptor monitoringHttpClientInterceptor() {
        return new MonitoringHttpClientInterceptor(metrics);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     MonitoringHttpClientInterceptor interceptor) {
        return builder
                .additionalInterceptors(interceptor)
                .build();
    }
}
