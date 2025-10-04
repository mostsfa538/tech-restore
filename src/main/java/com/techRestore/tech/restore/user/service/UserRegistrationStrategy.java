package com.techRestore.tech.restore.user.service;

import com.techRestore.tech.restore.common.dto.auth.UserRegistration;
import com.techRestore.tech.restore.common.interfaces.RegistrationStrategy;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.Role;
import com.techRestore.tech.restore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserRegistrationStrategy implements RegistrationStrategy<User, UserRegistration> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createEntity(UserRegistration registrationData) {
        User user = new User();
        user.setFirst_name(registrationData.first_name());
        user.setLast_name(registrationData.last_name());
        user.setEmail(registrationData.email());
        user.setRole(Role.GUEST);
        user.setPassword(passwordEncoder.encode(registrationData.password()));
        user.setPhone(registrationData.phone());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Override
    public User saveEntity(User entity) {
        return userRepository.save(entity);
    }

    @Override
    public String getEmail(UserRegistration registrationData) {
        return registrationData.email();
    }

    @Override
    public String getSuccessMessage(User entity) {
        return "User registered successfully";
    }
}