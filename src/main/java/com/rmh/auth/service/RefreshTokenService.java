package com.rmh.auth.service;

import com.rmh.auth.model.RefreshToken;
import com.rmh.auth.model.User;
import com.rmh.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenAndActiveTrue(String token) {
        return refreshTokenRepository.findByTokenAndActiveTrue(token);
    }

    @Transactional
    public RefreshToken save(RefreshToken token) {
        return refreshTokenRepository.save(token);
    }

    @Transactional
    public void deactivate(RefreshToken token) {
        token.deactivate();
        refreshTokenRepository.save(token);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deactivateAllTokensForUser(User user) {
        refreshTokenRepository.deactivateAllByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public List<RefreshToken> findAllActiveByUser(User user) {
        return refreshTokenRepository.findAllByUserAndActiveTrue(user);
    }
}

