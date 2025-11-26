package com.rmh.auth.service;

import com.rmh.auth.dto.UpdateUserProfileRequest;
import com.rmh.auth.dto.UserProfileResponse;
import com.rmh.auth.exception.NotFoundException;
import com.rmh.auth.model.Address;
import com.rmh.auth.model.User;
import com.rmh.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String email) {
        User user = findActiveUser(email);
        return toProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfileById(Long id) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> searchUsersByNameRegex(String regexPattern) {
        List<User> users = userRepository.findActiveUsersByNameRegex(regexPattern);
        return users.stream()
                .map(this::toProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(String email, UpdateUserProfileRequest request) {
        User user = findActiveUser(email);
        user.setName(request.name());
        
        // Handle address - create if doesn't exist, update if exists
        Address address = user.getAddress();
        if (address == null) {
            address = new Address();
            address.setUser(user);
            user.setAddress(address);
        }
        
        address.setPhoneNumber(request.phoneNumber());
        address.setAddressLine(request.addressLine());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        
        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    private User findActiveUser(String email) {
        return userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserProfileResponse toProfileResponse(User user) {
        Address address = user.getAddress();
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                address != null ? address.getPhoneNumber() : null,
                address != null ? address.getAddressLine() : null,
                address != null ? address.getCity() : null,
                address != null ? address.getState() : null,
                address != null ? address.getPostalCode() : null,
                address != null ? address.getCountry() : null,
                user.getRoles()
        );
    }
}

