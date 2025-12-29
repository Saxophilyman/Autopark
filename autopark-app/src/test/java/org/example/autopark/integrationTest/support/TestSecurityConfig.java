package org.example.autopark.integrationTest.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. В тестах CSRF убираем, чтобы POST/DELETE спокойно проходили
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Все запросы разрешаем без аутентификации
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // 3. Без сессий
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // Парольный энкодер для RegistrationService
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    // Заглушка AuthenticationManager для AuthApiController.
//    // В интеграционных тестах мы /auth/login не вызываем,
//    // так что этот AuthenticationManager просто нужен, чтобы DI не падал.
//    @Bean
//    public AuthenticationManager authenticationManager() {
//        return authentication -> {
//            throw new UnsupportedOperationException(
//                    "AuthenticationManager не используется в интеграционных тестах");
//        };
//    }

    // НОРМАЛЬНЫЙ AuthenticationManager для AuthApiController.login(...)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
