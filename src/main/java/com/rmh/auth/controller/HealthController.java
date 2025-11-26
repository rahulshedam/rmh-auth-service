package com.rmh.auth.controller;

import com.rmh.auth.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/actuator/health")
    public ResponseEntity<ApiResponse<Map<String,String>>> health(HttpServletRequest request){
        Map<String,String> payload = Map.of("status","UP");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, payload, request.getRequestURI()));
    }
}
