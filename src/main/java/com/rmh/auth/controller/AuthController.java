package com.rmh.auth.controller;

import com.rmh.auth.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import com.rmh.auth.service.AuthService;
import com.rmh.auth.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService a){ this.authService = a; }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest req, HttpServletRequest request) {
        User u = authService.register(req.email(), req.name(), req.password());
        RegisterResponse response = new RegisterResponse(u.getId(), u.getEmail(), u.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, response, request.getRequestURI()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request){
        TokenResponse tokenResponse = authService.login(req.email(), req.password());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tokenResponse, request.getRequestURI()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest request){
        TokenResponse tokenResponse = authService.refreshToken(req.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tokenResponse, request.getRequestURI()));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<LogoutResponse>> logoutAll(HttpServletRequest request,
                                                                 Principal principal){
        authService.logoutEverywhere(principal.getName());
        LogoutResponse response = new LogoutResponse(true);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response, request.getRequestURI()));
    }

    @PostMapping("/revoke")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RevokeResponse>> revoke(@Valid @RequestBody RevokeRequest req,
                                                              HttpServletRequest request){
        authService.revokeUser(req.email());
        RevokeResponse response = new RevokeResponse(true);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response, request.getRequestURI()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(@Valid @RequestBody LogoutRequest req, HttpServletRequest request){
        authService.logout(req.refreshToken());
        LogoutResponse logoutResponse = new LogoutResponse(true);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, logoutResponse, request.getRequestURI()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<TokenResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            HttpServletRequest request,
            Principal principal
    ){
        TokenResponse response = authService.changePassword(principal.getName(), req);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response, request.getRequestURI()));
    }
}
