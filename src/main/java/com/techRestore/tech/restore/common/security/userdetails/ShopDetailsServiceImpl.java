package com.techRestore.tech.restore.common.security.userdetails;

import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.shop.repository.ShopRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ShopDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private ShopRepository shopRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Shop not found"));
        return new ShopPrincipal(shop);
    }
}