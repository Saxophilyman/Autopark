package org.example.autopark.reactive;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@Profile("reactive")
@EnableR2dbcRepositories(basePackages = "org.example.autopark.reactive")
public class ReactiveDataConfig {
    // Автоконфигурации достаточно
}
