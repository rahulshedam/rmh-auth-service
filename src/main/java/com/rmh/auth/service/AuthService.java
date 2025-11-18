package com.rmh.auth.service;

import com.rmh.auth.model.User;
import com.rmh.auth.model.RefreshToken;
import com.rmh.auth.repository.UserRepository;
import com.rmh.auth.repository.RefreshTokenRepository;
import com.rmh.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final long refreshTokenExpireSeconds;

    public AuthService(UserRepository ur, RefreshTokenRepository rr, PasswordEncoder pe, JwtUtil ju){
        this.userRepo = ur;
        this.refreshRepo = rr;
        this.passwordEncoder = pe;
        this.jwtUtil = ju;
        this.refreshTokenExpireSeconds = 1209600L; // default 14 days
    }

    @Transactional
    public User register(String username, String password){
        if (userRepo.existsByUsername(username)){
            throw new IllegalArgumentException("username exists");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.getRoles().add("ROLE_USER");
        return userRepo.save(u);
    }

    @Transactional
    public Map<String,String> login(String username, String password){
        User user = userRepo.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new IllegalArgumentException("invalid credentials");
        }
        String access = jwtUtil.generateAccessToken(user.getUsername(), user.getRoles());
        // create refresh token
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiryDate(Instant.now().plusSeconds(refreshTokenExpireSeconds));
        refreshRepo.save(rt);
        Map<String,String> m = new HashMap<>();
        m.put("access_token", access);
        m.put("refresh_token", rt.getToken());
        return m;
    }

    @Transactional
    public Map<String,String> refreshToken(String oldRefreshToken){
        RefreshToken rt = refreshRepo.findByToken(oldRefreshToken).orElseThrow(() -> new IllegalArgumentException("invalid refresh token"));
        if (rt.getExpiryDate().isBefore(Instant.now())){
            refreshRepo.delete(rt);
            throw new IllegalArgumentException("refresh token expired");
        }
        // rotation: delete old token and issue a new one
        User user = rt.getUser();
        refreshRepo.delete(rt);

        RefreshToken newRt = new RefreshToken();
        newRt.setToken(UUID.randomUUID().toString());
        newRt.setUser(user);
        newRt.setExpiryDate(Instant.now().plusSeconds(refreshTokenExpireSeconds));
        refreshRepo.save(newRt);

        String newAccess = jwtUtil.generateAccessToken(user.getUsername(), user.getRoles());
        Map<String,String> res = new HashMap<>();
        res.put("access_token", newAccess);
        res.put("refresh_token", newRt.getToken());
        return res;
    }

    @Transactional
    public void revokeTokensForUser(String username){
        User user = userRepo.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("user not found"));
        refreshRepo.deleteByUser(user);
    }
}
