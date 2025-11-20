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
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody AuthRequest req){
        User u = authService.register(req.username(), req.password());
        return ResponseEntity.ok(new RegisterResponse(u.getId(), u.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthRequest req){
        Map<String,String> tokens = authService.login(req.username(), req.password());
        return ResponseEntity.ok(new TokenResponse(tokens.get("access_token"), tokens.get("refresh_token"), "Bearer", 900));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req){
        Map<String,String> tokens = authService.refreshToken(req.refreshToken());
        return ResponseEntity.ok(new TokenResponse(tokens.get("access_token"), tokens.get("refresh_token"), "Bearer", 900));
    }

    @PostMapping("/revoke")
    public ResponseEntity<RevokeResponse> revoke(@Valid @RequestBody RevokeRequest req){
        authService.revokeTokensForUser(req.username());
        return ResponseEntity.ok(new RevokeResponse(true));
    }
}
