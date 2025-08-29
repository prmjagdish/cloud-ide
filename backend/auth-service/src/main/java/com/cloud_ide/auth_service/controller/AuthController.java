package com.cloud_ide.auth_service.controller;

import com.cloud_ide.auth_service.model.User;
import com.cloud_ide.auth_service.repository.UserRepository;
import com.cloud_ide.auth_service.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    // --- Register ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isBlank() ||
                user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Username and password must not be empty"
            ));
        }

        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "message", "Username already exists"
            ));
        }

        user.setPassword(encoder.encode(user.getPassword()));
        User savedUser = userRepo.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User registered successfully",
                "userId", savedUser.getId().toString()
        ));
    }

    // --- Login ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Username and password are required"
            ));
        }

        Optional<User> optionalUser = userRepo.findByUsername(user.getUsername());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid username or password"
            ));
        }

        User dbUser = optionalUser.get();
        if (!encoder.matches(user.getPassword(), dbUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid username or password"
            ));
        }

        String token = jwtUtil.generateToken(dbUser.getUsername(), dbUser.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token
        ));
    }

    // --- Validate Token ---
    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestParam String token) {
        try {
            Claims claims = jwtUtil.validateToken(token);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", claims.get("userId"),
                    "username", claims.getSubject()
            ));
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token expired"
            ));
        } catch (MalformedJwtException | SignatureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid token"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token validation failed"
            ));
        }
    }

    // --- Get Current User ---
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "userId", required = false) String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing userId header"
            ));
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid userId format"
            ));
        }

        return userRepo.findById(userId)
                .map(user -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "user", user
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "User not found"
                )));
    }
}
