package com.rmh.auth.repository;

import com.rmh.auth.model.RefreshToken;
import com.rmh.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // Spring Data JPA method naming - finds active token
    Optional<RefreshToken> findByTokenAndActiveTrue(String token);
    
    Optional<RefreshToken> findByToken(String token);
    
    // Spring Data JPA method naming - finds all active tokens for a user
    List<RefreshToken> findAllByUserAndActiveTrue(User user);
    
    // Bulk update for security-critical deactivation
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.active = false, rt.deletedAt = CURRENT_TIMESTAMP WHERE rt.user.id = :userId AND rt.active = true")
    int deactivateAllByUserId(@Param("userId") Long userId);
}
