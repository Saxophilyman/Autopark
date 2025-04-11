package org.example.autopark.securityConfig;

import org.example.autopark.securityConfig.jwt.JwtAuthFilter;
import org.example.autopark.service.GeneralDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final GeneralDetailsService generalDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Autowired
    public SecurityConfig(GeneralDetailsService generalDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.generalDetailsService = generalDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        //http.csrf(csrf -> csrf.disable());

         http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        //http.csrf().ignoringRequestMatchers("/auth/login");
//        http.authenticationManager(authenticationManager) "/api/generate/**",
        http
                .authorizeHttpRequests(authz -> authz
                        // Разрешаем доступ без аутентификации к указанным ресурсам
                        .requestMatchers("/auth/login", "/auth/login2","/auth/registration", "/favicon.ico", "/css/**", "/js/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/managers/**","/api/generate/**","/managers/**").hasRole("MANAGER")
//                        .requestMatchers("/api/generate/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("USER")
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                );

        // Добавляем кастомный JWT-фильтр перед стандартным фильтром аутентификации
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http.formLogin((formLogin) -> formLogin
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/process_login")
                        .successHandler(new BaseAuthenticationSuccessHandler())
                        .failureUrl("/auth/login?error"))
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
