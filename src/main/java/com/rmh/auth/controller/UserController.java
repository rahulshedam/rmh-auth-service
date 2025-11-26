package com.rmh.auth.controller;

import com.rmh.auth.dto.ApiResponse;
import com.rmh.auth.dto.UpdateUserProfileRequest;
import com.rmh.auth.dto.UserProfileResponse;
import com.rmh.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        UserProfileResponse profile = userService.getUserProfileById(userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, profile, request.getRequestURI()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> searchUsersByName(
            @RequestParam("pattern") String regexPattern,
            HttpServletRequest request
    ) {
        if (regexPattern == null || regexPattern.isBlank()) {
            throw new IllegalArgumentException("search string must not be blank");
        }
        List<UserProfileResponse> matches = userService.searchUsersByNameRegex(regexPattern);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, matches, request.getRequestURI()));
    }

}

