package com.anilg.ecommerce.auth;

import com.anilg.ecommerce.common.ApiResponse;
import com.anilg.ecommerce.common.DomainEvent;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserAccountRepository users;
    private final KafkaTemplate<String, DomainEvent> kafka;
    private final SecretKey secretKey;
    private final long expiryMs;

    public AuthController(UserAccountRepository users, KafkaTemplate<String, DomainEvent> kafka,
                          @Value("${jwt.secret}") String secret,
                          @Value("${jwt.expiry-ms:86400000}") long expiryMs) {
        this.users = users;
        this.kafka = kafka;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMs = expiryMs;
    }

    private String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(secretKey)
                .compact();
    }

    private String extractEmail(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token.replace("Bearer ", ""))
                    .getPayload().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractRole(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token.replace("Bearer ", ""))
                    .getPayload().get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserAccount user = users.findByEmail(request.email()).orElseGet(UserAccount::new);
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPassword(request.password());
        user.setRole(user.getId() == null ? "CUSTOMER" : user.getRole());
        UserAccount saved = users.save(user);
        String token = generateToken(saved.getEmail(), saved.getRole());
        try {
            kafka.send("commerce.events", saved.getEmail(), DomainEvent.of(
                    "USER_REGISTERED", String.valueOf(saved.getId()), saved.getEmail(),
                    Map.of("email", saved.getEmail(), "fullName", saved.getFullName(), "role", saved.getRole())
            ));
        } catch (Exception e) {
            log.warn("Kafka send failed for register: {}", e.getMessage());
        }
        return ApiResponse.ok(new AuthResponse(token, "JWT", saved.getEmail(), saved.getFullName(), saved.getRole()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = users.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("Invalid credentials"));
        String token = generateToken(user.getEmail(), user.getRole());
        try {
            kafka.send("commerce.events", request.email(), DomainEvent.of(
                    "USER_LOGGED_IN", request.email(), request.email(),
                    Map.of("email", request.email())
            ));
        } catch (Exception e) {
            log.warn("Kafka send failed for login: {}", e.getMessage());
        }
        return ApiResponse.ok(new AuthResponse(token, "JWT", user.getEmail(), user.getFullName(), user.getRole()));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, String>> me(@RequestHeader("Authorization") String auth) {
        String email = extractEmail(auth);
        String role = extractRole(auth);
        if (email == null) return ApiResponse.error("Invalid token");
        return ApiResponse.ok(Map.of("email", email, "role", role));
    }

    @GetMapping("/admin/users")
    public ApiResponse<List<UserAccount>> adminUsers(@RequestHeader("Authorization") String auth) {
        if (!"ADMIN".equals(extractRole(auth))) return ApiResponse.error("Unauthorized");
        return ApiResponse.ok(users.findAll());
    }

    @PutMapping("/admin/users/{id}/role")
    public ApiResponse<UserAccount> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body,
                                                @RequestHeader("Authorization") String auth) {
        if (!"ADMIN".equals(extractRole(auth))) throw new RuntimeException("Unauthorized");
        UserAccount user = users.findById(id).orElseThrow();
        user.setRole(body.get("role"));
        return ApiResponse.ok(users.save(user));
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("auth-service ready");
    }

    public record RegisterRequest(@Email String email, @NotBlank String fullName, @NotBlank String password) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, String type, String email, String fullName, String role) {}
}
