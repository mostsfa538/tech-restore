package com.techRestore.tech.restore.services;

import com.techRestore.tech.restore.dto.LoginDto;
import com.techRestore.tech.restore.dto.UserDto;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServices {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String register(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirst_name(userDto.name());
        user.setEmail(userDto.email());
        user.setPassword(passwordEncoder.encode(userDto.password()));
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return savedUser.getId().toString();
    }

    public Authentication login(LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.email(),
                            loginDto.password())
            );
            return authentication;
        } catch (Exception e) {
            throw new RuntimeException("User no found");
        }
    }
}