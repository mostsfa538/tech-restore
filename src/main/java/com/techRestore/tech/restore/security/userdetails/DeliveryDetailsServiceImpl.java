package com.techRestore.tech.restore.security.userdetails;

import com.techRestore.tech.restore.model.entities.Delivery;
import com.techRestore.tech.restore.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryDetailsServiceImpl implements UserDetailsService {

    private final DeliveryRepository deliveryRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Delivery delivery = deliveryRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Delivery not found with email: " + email));
        return User.builder()
                .username(delivery.getEmail())
                .password(delivery.getPassword())
                .authorities("ROLE_" + delivery.getRole().name())
                .build();
    }
}