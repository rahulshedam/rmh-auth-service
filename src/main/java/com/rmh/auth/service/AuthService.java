package com.rmh.auth.service;

import com.rmh.auth.dto.ChangePasswordRequest;
import com.rmh.auth.dto.TokenResponse;
import com.rmh.auth.exception.IncorrectPasswordException;
import com.rmh.auth.exception.InvalidCredentialsException;
import com.rmh.auth.exception.InvalidRefreshTokenException;
import com.rmh.auth.exception.NotFoundException;
import com.rmh.auth.exception.PasswordSameAsCurrentException;
import com.rmh.auth.exception.RefreshTokenExpiredException;
import com.rmh.auth.exception.UsernameAlreadyExistsException;
import com.rmh.auth.model.User;
import com.rmh.auth.model.RefreshToken;
import com.rmh.auth.repository.UserRepository;
import com.rmh.auth.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepo;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final long refreshTokenExpireSeconds;

    public AuthService(
            UserRepository ur,
            RefreshTokenService refreshTokenService,
            PasswordEncoder pe,
            JwtUtil ju,
            @Value("${jwt.refreshTokenExpirationSeconds:1209600}") long refreshTokenExpireSeconds
    ){
        this.userRepo = ur;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = pe;
        this.jwtUtil = ju;
        this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
    }

    @Transactional
    public User register(String email, String name, String password){
        if (userRepo.existsByEmailAndActiveTrue(email)){
            throw new UsernameAlreadyExistsException("Email already registered");
        }
        User u = new User();
        u.setEmail(email);
        u.setName(name);
        u.setPassword(passwordEncoder.encode(password));
        u.getRoles().add("ROLE_USER");
        return userRepo.save(u);
    }

    @Transactional
    public TokenResponse login(String email, String password){
        // Security: Don't reveal if user exists - return same error for both cases
        User user = userRepo.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new InvalidCredentialsException("Invalid credentials");
        }
        RefreshToken rt = issueRefreshToken(user);
        String access = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles());
        return new TokenResponse(access, rt.getToken(), "Bearer", jwtUtil.getAccessTokenExpirySeconds());
    }

    @Transactional
    public TokenResponse refreshToken(String oldRefreshToken){
        // Fetch token (active or inactive) with user + roles
        RefreshToken rt = refreshTokenService.findByToken(oldRefreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
        
        User user = rt.getUser();

        // Old/inactive token reuse → consider as theft, wipe everything for that user
        if (!rt.isActive()) {
            handleStolenOrInactiveToken(user, oldRefreshToken, rt);
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        
        // Active token but expired
        if (rt.getExpiryDate().isBefore(Instant.now())){
            refreshTokenService.deactivate(rt);
            throw new RefreshTokenExpiredException("Refresh token expired");
        }

        if (user == null) {
            log.error("Orphaned active refresh token detected (user is null) for token: {}", oldRefreshToken);
            refreshTokenService.deactivate(rt);
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        
        if (!user.isActive()) {
            refreshTokenService.deactivateAllTokensForUser(user);
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        
        // Success path: rotate only the current token
        return rotateRefreshToken(user, rt);
    }
    
    private void handleStolenOrInactiveToken(User user, String oldRefreshToken, RefreshToken token) {
        if (user != null) {
            refreshTokenService.deactivateAllTokensForUser(user);
            log.warn("Security alert: Attempted reuse of inactive refresh token for user {}", user.getEmail());
        } else {
            log.error("Orphaned refresh token detected (user is null) for token: {}", oldRefreshToken);
            refreshTokenService.deactivate(token);
        }
    }
    
    private TokenResponse rotateRefreshToken(User user, RefreshToken oldToken) {
        refreshTokenService.deactivate(oldToken);
        RefreshToken newRt = issueRefreshToken(user);
        String access = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles());
        return new TokenResponse(access, newRt.getToken(), "Bearer", jwtUtil.getAccessTokenExpirySeconds());
    }

    @Transactional
    public void logoutEverywhere(String email){
        User user = userRepo.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        refreshTokenService.deactivateAllTokensForUser(user);
    }

    @Transactional
    public void revokeUser(String email){
        User user = userRepo.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.deactivate();
        userRepo.save(user);
        refreshTokenService.deactivateAllTokensForUser(user);
    }

    @Transactional
    public void logout(String refreshToken){
        RefreshToken token = refreshTokenService.findByTokenAndActiveTrue(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
        deactivateToken(token);
    }

    @Transactional
    public TokenResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepo.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("Current password is incorrect");
        }
        
        // Validation: New password must be different from current password
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new PasswordSameAsCurrentException("New password must be different from current password");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);
        
        // Security best practice: invalidate all existing tokens when password changes
        refreshTokenService.deactivateAllTokensForUser(user);
        
        // Issue new tokens
        RefreshToken newRefreshToken = issueRefreshToken(user);
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles());
        
        return new TokenResponse(newAccessToken, newRefreshToken.getToken(), "Bearer", jwtUtil.getAccessTokenExpirySeconds());
    }

    private RefreshToken issueRefreshToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiryDate(Instant.now().plusSeconds(refreshTokenExpireSeconds));
        return refreshTokenService.save(rt);
    }

    private void deactivateToken(RefreshToken token) {
        refreshTokenService.deactivate(token);
    }
    
}
