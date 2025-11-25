package com.rmh.auth.exception;

import com.rmh.auth.dto.ApiResponse;
import com.rmh.auth.dto.ErrorDetail;
import com.rmh.auth.dto.MessageDetail;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req){
        log.warn("Authentication failed: {}", ex.getMessage());
        // Security: Don't reveal which field is wrong or if user exists
        ErrorDetail error = ErrorDetail.of("AUTH-401", "Invalid credentials");
        MessageDetail messageDetail = MessageDetail.of("UNAUTHORIZED", "Authentication failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED, req.getRequestURI(), error, messageDetail));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex, HttpServletRequest req){
        log.warn("Registration conflict: {}", ex.getMessage());
        ErrorDetail error = ErrorDetail.of("AUTH-409", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT, req.getRequestURI(), error));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req){
        log.warn("Bad request: {}", ex.getMessage());
        ErrorDetail error = ErrorDetail.of("AUTH-400", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, req.getRequestURI(), error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req){
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        log.warn("Validation error on {}: {}", req.getRequestURI(), fieldErrors);
        
        Map<String, Object> sanitizedErrors = sanitizeValidationErrors(req.getRequestURI(), fieldErrors);
        
        ErrorDetail error = ErrorDetail.of("AUTH-VALIDATION", "Validation failed", sanitizedErrors);
        MessageDetail messageDetail = MessageDetail.of(HttpStatus.BAD_REQUEST.name(), "Validation error: inspect error.details for specifics");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, req.getRequestURI(), error, messageDetail));
    }
    
    /**
     * Sanitizes validation errors for security-sensitive endpoints.
     * For login endpoint, we show actual validation errors (format/required field errors)
     * as they don't reveal user existence - only authentication failures are sanitized.
     */
    private Map<String, Object> sanitizeValidationErrors(String requestUri, Map<String, String> fieldErrors) {
        // Show actual validation errors for login - these are format/required errors,
        // not authentication errors, so they're safe to show
        // Authentication failures are handled separately by InvalidCredentialsException
        return new HashMap<>(fieldErrors);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex, HttpServletRequest req){
        log.warn("Not found: {}", ex.getMessage());
        ErrorDetail error = ErrorDetail.of("AUTH-404", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND, req.getRequestURI(), error));
    }

    @ExceptionHandler(PasswordSameAsCurrentException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordSameAsCurrent(PasswordSameAsCurrentException ex, HttpServletRequest req){
        log.warn("New password same as current: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("newPassword", "New password must be different from current password");
        ErrorDetail error = ErrorDetail.of("AUTH-VALIDATION", "Validation failed", details);
        MessageDetail messageDetail = MessageDetail.of("BAD_REQUEST", "Validation error: inspect error.details for specifics");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, req.getRequestURI(), error, messageDetail));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException ex, HttpServletRequest req){
        log.warn("Invalid refresh token: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("refreshToken", "Invalid or malformed refresh token");
        ErrorDetail error = ErrorDetail.of("AUTH-REFRESH-INVALID", "Invalid refresh token", details);
        MessageDetail messageDetail = MessageDetail.of("UNAUTHORIZED", "Authentication failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED, req.getRequestURI(), error, messageDetail));
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenExpired(RefreshTokenExpiredException ex, HttpServletRequest req){
        log.warn("Expired refresh token: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("refreshToken", "Refresh token has expired");
        ErrorDetail error = ErrorDetail.of("AUTH-REFRESH-EXPIRED", "Refresh token expired", details);
        MessageDetail messageDetail = MessageDetail.of("UNAUTHORIZED", "Authentication failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED, req.getRequestURI(), error, messageDetail));
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleIncorrectPassword(IncorrectPasswordException ex, HttpServletRequest req){
        log.warn("Incorrect password provided: {}", ex.getMessage());
        // Security: Don't reveal which field is wrong
        ErrorDetail error = ErrorDetail.of("AUTH-403", "Access denied");
        MessageDetail messageDetail = MessageDetail.of("FORBIDDEN", "Insufficient permissions");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN, req.getRequestURI(), error, messageDetail));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req){
        log.warn("Access denied: {}", ex.getMessage());
        // Security: Don't reveal what role is required
        ErrorDetail error = ErrorDetail.of("AUTH-403", "Access denied");
        MessageDetail messageDetail = MessageDetail.of("FORBIDDEN", "Insufficient permissions");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN, req.getRequestURI(), error, messageDetail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex, HttpServletRequest req){
        log.error("Unhandled exception for {}", req.getRequestURI(), ex);
        ErrorDetail error = ErrorDetail.of("AUTH-500", "internal_error");
        MessageDetail messageDetail = MessageDetail.of(HttpStatus.INTERNAL_SERVER_ERROR.name(), "Internal Server Error: inspect logs for stack trace");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, req.getRequestURI(), error, messageDetail));
    }
}
