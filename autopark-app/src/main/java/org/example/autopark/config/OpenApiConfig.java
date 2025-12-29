package org.example.autopark.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Autopark API",
                version = "9.0",
                description = "REST API автопарка (менеджеры, предприятия, машины, водители)"
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        },
        servers = {
                // относительный путь — Swagger будет использовать текущий хост и протокол
                @Server(
                        url = "/",
                        description = "Тот же хост, что и Swagger (http/https определяется автоматически)"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}