package com.techRestore.tech.restore.user.service;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    private final UserRepository userRepository;

    public AuthUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new NotFoundException("No authenticated user found");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email);

        if (user == null || !user.isActivate()) {
            throw new NotFoundException("User account is deactivated or not found");
        }

        return user;
    }
}
