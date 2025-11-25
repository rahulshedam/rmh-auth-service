package com.rmh.auth.controller;

import com.rmh.auth.dto.ApiResponse;
import com.rmh.auth.dto.UpdateUserProfileRequest;
import com.rmh.auth.dto.UserProfileResponse;
import com.rmh.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(Authentication authentication, HttpServletRequest request) {
        UserProfileResponse profile = userService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, profile, request.getRequestURI()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest requestBody,
            Authentication authentication,
            HttpServletRequest request
    ) {
        UserProfileResponse profile = userService.updateCurrentUserProfile(authentication.getName(), requestBody);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, profile, request.getRequestURI()));
    }

}

