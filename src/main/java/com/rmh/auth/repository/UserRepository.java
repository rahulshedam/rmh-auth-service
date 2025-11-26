package com.rmh.auth.repository;

import com.rmh.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndActiveTrue(String email);
    boolean existsByEmailAndActiveTrue(String email);
    Optional<User> findByIdAndActiveTrue(Long id);

    @Query(value = "SELECT * FROM users WHERE active = true AND name ~* :pattern", nativeQuery = true)
    List<User> findActiveUsersByNameRegex(@Param("pattern") String regexPattern);
}
