package com.techRestore.tech.restore.common.services;

import com.techRestore.tech.restore.assigners.repository.AssignerRepository;
import com.techRestore.tech.restore.common.model.entities.Assigner;
import com.techRestore.tech.restore.common.model.entities.Delivery;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.user.repository.UserRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntityFinderService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final DeliveryRepository deliveryRepository;
    private final AssignerRepository assignerRepository;

    public Optional<Authentication> findEntityAndCreateAuthentication(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return Optional.of(new UsernamePasswordAuthenticationToken(
                    email, null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
        }

        Optional<Shop> shop = shopRepository.findByEmail(email);
        if (shop != null) {
            return Optional.of(new UsernamePasswordAuthenticationToken(
                    email, null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_SHOP"))));
        }

        Optional<Delivery> delivery = deliveryRepository.findByEmail(email);
        if (delivery != null) {
            return Optional.of(new UsernamePasswordAuthenticationToken(
                    email, null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_DELIVERY"))));
        }

        Optional<Assigner> assigner = assignerRepository.findByEmail(email);
        if (assigner != null) {
            return Optional.of(new UsernamePasswordAuthenticationToken(
                    email, null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ASSIGNER"))));
        }

        return Optional.empty();
    }

    public boolean entityExists(String email) {
        return userRepository.existsByEmail(email) ||
                shopRepository.existsByEmail(email) ||
                deliveryRepository.existsByEmail(email)||
                assignerRepository.existsByEmail(email);
    }

    public String getEntityType(String email) {
        if (userRepository.existsByEmail(email)) {
            return "USER";
        } else if (shopRepository.existsByEmail(email)) {
            return "SHOP";
        } else if (deliveryRepository.existsByEmail(email)) {
            return "DELIVERY";
        }
        else if (assignerRepository.existsByEmail(email)) {
            return "ASSIGNER";
        }
        return "UNKNOWN";
    }


    public Optional<?> findEntityByEmail(String email) {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmail(email));
        if (user.isPresent()) return Optional.of(user.get());
        
        Optional<Shop> shop = shopRepository.findByEmail(email);
        if (shop.isPresent()) return Optional.of(shop.get());
        
        Optional<Delivery> delivery = deliveryRepository.findByEmail(email);
        if (delivery.isPresent()) return Optional.of(delivery.get());
        
        Optional<Assigner> assigner = assignerRepository.findByEmail(email);
        if (assigner.isPresent()) return Optional.of(assigner.get());
        
        return Optional.empty();
}

}
