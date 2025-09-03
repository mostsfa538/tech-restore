package com.techRestore.tech.restore.security.userdetails;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.techRestore.tech.restore.model.entities.Delivery;

public class DeliveryPrincipal implements UserDetails {

    private final Delivery delivery;

    public DeliveryPrincipal(Delivery delivery) {
        this.delivery = delivery;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + delivery.getRole().name()));
    }

    @Override
    public String getPassword() {
        return delivery.getPassword();
    }

    @Override
    public String getUsername() {
        return delivery.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}