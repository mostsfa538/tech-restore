package com.techRestore.tech.restore.common.security.userdetails;

import com.techRestore.tech.restore.common.model.entities.Delivery;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;

import lombok.RequiredArgsConstructor;
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
                .orElseThrow(() -> new UsernameNotFoundException("Delivery not found"));
        return new DeliveryPrincipal(delivery);
    }
}