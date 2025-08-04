package com.techRestore.tech.restore.security.userdetails;

import com.techRestore.tech.restore.model.entities.Shop;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class ShopPrincipal implements UserDetails {
    private final Shop shop;

    public ShopPrincipal(Shop shop) {
        this.shop = shop;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("SHOP_OWNER"));
    }

    @Override
    public String getPassword() {
        return shop.getPassword();
    }

    @Override
    public String getUsername() {
        return shop.getEmail();
    }
}
