package org.example.autopark.telegrambot.tg;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.example.autopark.repository.ManagerRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Hidden
@Profile("!reactive")
@RestController
@RequestMapping("/internal/tg")
@RequiredArgsConstructor
public class TgAuthController {
    private final AuthenticationManager authManager;
    private final ManagerRepository managers;

    public record LoginRq(String username, String password) {}
    public record LoginRs(Long managerId) {}

    @PostMapping("/login")
    public ResponseEntity<LoginRs> login(@RequestBody LoginRq rq) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(rq.username(), rq.password()));
        var m = managers.findByUsername(rq.username()).orElseThrow();
        return ResponseEntity.ok(new LoginRs(m.getManagerId()));
    }
}
