package com.anilg.ecommerce.auth;

import com.anilg.ecommerce.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAccountRepository users;

    public AuthController(UserAccountRepository users) {
        this.users = users;
    }

    @PostMapping("/register")
    public ApiResponse<UserAccount> register(@Valid @RequestBody RegisterRequest request) {
        UserAccount user = users.findByEmail(request.email()).orElseGet(UserAccount::new);
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setRole("CUSTOMER");
        return ApiResponse.ok(users.save(user));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        users.findByEmail(request.email()).orElseThrow();
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
