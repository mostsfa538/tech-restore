package com.techRestore.tech.restore.delivery.service;

import com.techRestore.tech.restore.common.interfaces.RegistrationStrategy;
import com.techRestore.tech.restore.common.model.entities.Delivery;
import com.techRestore.tech.restore.common.model.enums.Role;
import com.techRestore.tech.restore.delivery.dto.DeliveryRegistration;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DeliveryRegistrationStrategy implements RegistrationStrategy<Delivery, DeliveryRegistration> {

    private final DeliveryRepository deliveryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Delivery createEntity(DeliveryRegistration registrationData) {
        Delivery delivery = new Delivery();
        delivery.setEmail(registrationData.getEmail());
        delivery.setPassword(passwordEncoder.encode(registrationData.getPassword()));
        delivery.setName(registrationData.getName());
        delivery.setAddress(registrationData.getAddress());
        delivery.setPhone(registrationData.getPhone());
        delivery.setRole(Role.DELIVERY);
        delivery.setCreatedAt(LocalDateTime.now());
        return delivery;
    }

    @Override
    public Delivery saveEntity(Delivery entity) {
        return deliveryRepository.save(entity);
    }

    @Override
    public String getEmail(DeliveryRegistration registrationData) {
        return registrationData.getEmail();
    }

    @Override
    public String getSuccessMessage(Delivery entity) {
        return entity.getId().toString();
    }
}
