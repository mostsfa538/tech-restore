package com.techRestore.tech.restore.common.utils;

import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.exception.EmailAlreadyExistsException;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailValidatorService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final DeliveryRepository deliveryRepository;

    public void validateUniqueEmail(String email) {
        boolean exists = userRepository.existsByEmail(email) ||
                shopRepository.existsByEmail(email) ||
                deliveryRepository.existsByEmail(email);

        if (exists) {
            throw new EmailAlreadyExistsException("Email is already registered: " + email);
        }
    }
}