package com.techRestore.tech.restore.user.service;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import com.techRestore.tech.restore.shop.repository.ShopRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final UserRepository userRepository;
    private final ShopRepository ShopRepository;

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

    public Shop getCurrentShop() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new NotFoundException("No authenticated Shop found");
        }

        String email = auth.getName();
        Shop shop = ShopRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Shop not found"));

        if (shop == null || !shop.isActivate()) {
            throw new NotFoundException("Shop account is deactivated or not found");
        }

        return shop;
    }
}
