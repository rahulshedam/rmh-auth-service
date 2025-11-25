package com.rmh.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rmh.auth.dto.ApiStatus;
import com.rmh.auth.security.JwtUtil;
import com.rmh.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final ObjectWriter RESPONSE_WRITER = new ObjectMapper().writer();
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil ju, CustomUserDetailsService uds){
        this.jwtUtil = ju;
        this.userDetailsService = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")){
            String token = header.substring(7);
            if (!jwtUtil.validateToken(token)){
                writeUnauthorizedResponse(response, request, "Invalid or expired token");
                return;
            }
            String principal = jwtUtil.getSubjectFromToken(token);
            UserDetails ud = userDetailsService.loadUserByUsername(principal);
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, HttpServletRequest request, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ApiStatus.FAILURE.name());
        body.put("httpStatus", HttpStatus.UNAUTHORIZED.value());

        Map<String, Object> messageNode = new LinkedHashMap<>();
        messageNode.put("code", HttpStatus.UNAUTHORIZED.name());
        messageNode.put("message", "Authentication failed");
        body.put("message", messageNode);

        body.put("path", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());
        body.put("data", null);

        Map<String, Object> errorNode = new LinkedHashMap<>();
        errorNode.put("code", "AUTH-401");
        errorNode.put("message", message);
        body.put("error", errorNode);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        RESPONSE_WRITER.writeValue(response.getOutputStream(), body);
    }
}
