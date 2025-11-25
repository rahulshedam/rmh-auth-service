package com.rmh.auth.repository;

import com.rmh.auth.model.Address;
import com.rmh.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByUser(User user);
}

