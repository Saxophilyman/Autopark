package org.example.autopark.controllers.managers.APIControllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.autopark.dto.AuthenticationDTO;
import org.example.autopark.dto.JwtTokenDTO;
import org.example.autopark.securityConfig.jwt.JwtUtil;
import org.example.autopark.service.GeneralDetailsService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// Swagger / OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@Profile("!reactive")
@RequestMapping("/auth/api")
@RequiredArgsConstructor
@Tag(
        name = "Auth API",
        description = "Эндпоинты авторизации и получения JWT для работы с защищёнными API"
)
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final GeneralDetailsService generalDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(
            summary = "Логин и получение JWT",
            description = "Принимает логин и пароль, возвращает JWT-токен. Токен затем используется в Swagger через кнопку Authorize.",
            requestBody = @RequestBody(
                    description = "Учётные данные пользователя",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная аутентификация, возвращён JWT",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = JwtTokenDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неверные учетные данные"
                    )
            }
    )
    public ResponseEntity<JwtTokenDTO> login(
            @org.springframework.web.bind.annotation.RequestBody @Valid AuthenticationDTO authDTO
    ) {
        String username = authDTO.getUsername();
        String password = authDTO.getPassword();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            UserDetails userDetails = generalDetailsService.loadUserByUsername(username);
            String token = jwtUtil.generateToken(userDetails.getUsername());

            log.info("Пользователь {} успешно залогинен через /auth/api/login", username);
            return ResponseEntity.ok(new JwtTokenDTO(token));
        } catch (AuthenticationException e) {
            log.warn("Неуспешная попытка логина для пользователя {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
