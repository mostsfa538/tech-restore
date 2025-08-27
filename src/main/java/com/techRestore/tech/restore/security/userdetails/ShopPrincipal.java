package com.techRestore.tech.restore.security.userdetails;

import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.enums.ShopType;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShopPrincipal implements UserDetails {
    private final Shop shop;
    private final ShopType shop_type;

    public ShopPrincipal(Shop shop) {
        this.shop = shop;
        this.shop_type = shop.getShopType();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authoritie = new ArrayList<>();
        if (shop_type.equals("REPAIRER")) {
            authoritie.add(new SimpleGrantedAuthority("ROLE_REPAIRER"));
        } else if (shop_type.equals("SELLER")) {
            authoritie.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        } else if (shop_type.equals("BOTH")) {
            authoritie.add(new SimpleGrantedAuthority("ROLE_REPAIRER"));
            authoritie.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        }
        authoritie.add(new SimpleGrantedAuthority("ROLE_SHOP_OWNER"));
        return authoritie;
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
