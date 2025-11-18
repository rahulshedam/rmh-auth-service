package com.rmh.auth.controller;

import com.rmh.auth.dto.*;
import com.rmh.auth.service.AuthService;
import com.rmh.auth.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService a){ this.authService = a; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest req){
        User u = authService.register(req.username(), req.password());
        return ResponseEntity.ok(Map.of("id", u.getId(), "username", u.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req){
        Map<String,String> tokens = authService.login(req.username(), req.password());
        return ResponseEntity.ok(new TokenResponse(tokens.get("access_token"), tokens.get("refresh_token"), "Bearer", 900));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest req){
        Map<String,String> tokens = authService.refreshToken(req.refreshToken());
        return ResponseEntity.ok(new TokenResponse(tokens.get("access_token"), tokens.get("refresh_token"), "Bearer", 900));
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revoke(@RequestBody Map<String,String> body){
        String username = body.get("username");
        authService.revokeTokensForUser(username);
        return ResponseEntity.ok(Map.of("revoked", true));
    }
}
