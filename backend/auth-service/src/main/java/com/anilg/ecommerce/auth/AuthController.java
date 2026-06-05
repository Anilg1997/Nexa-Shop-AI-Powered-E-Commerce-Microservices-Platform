package com.anilg.ecommerce.auth;

import com.anilg.ecommerce.common.ApiResponse;
import com.anilg.ecommerce.common.DomainEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAccountRepository users;
    private final KafkaTemplate<String, DomainEvent> kafka;

    public AuthController(UserAccountRepository users, KafkaTemplate<String, DomainEvent> kafka) {
        this.users = users;
        this.kafka = kafka;
    }

    @PostMapping("/register")
    public ApiResponse<UserAccount> register(@Valid @RequestBody RegisterRequest request) {
        UserAccount user = users.findByEmail(request.email()).orElseGet(UserAccount::new);
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setRole("CUSTOMER");
        UserAccount saved = users.save(user);
        kafka.send("commerce.events", saved.getEmail(), DomainEvent.of(
                "USER_REGISTERED",
                String.valueOf(saved.getId()),
                saved.getEmail(),
                Map.of("email", saved.getEmail(), "fullName", saved.getFullName())
        ));
        return ApiResponse.ok(saved);
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        users.findByEmail(request.email()).orElseThrow();
        kafka.send("commerce.events", request.email(), DomainEvent.of(
                "USER_LOGGED_IN",
                request.email(),
                request.email(),
                Map.of("email", request.email())
        ));
        return ApiResponse.ok(Map.of("token", "demo-token-" + request.email(), "type", "DEMO"));
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("auth-service ready");
    }

    public record RegisterRequest(@Email String email, @NotBlank String fullName) {
    }

    public record LoginRequest(@Email String email, @NotBlank String password) {
    }
}
