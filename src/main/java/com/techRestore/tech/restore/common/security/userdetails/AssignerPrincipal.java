package com.techRestore.tech.restore.common.security.userdetails;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.techRestore.tech.restore.common.model.entities.Assigner;

public class AssignerPrincipal implements UserDetails {
    private final Assigner assigner;

    public AssignerPrincipal(Assigner assigner) {
        this.assigner = assigner;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + assigner.getRole().name()));
    }

    @Override
    public String getPassword() {
        return assigner.getPassword();
    }

    @Override
    public String getUsername() {
        return assigner.getEmail();
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
        return assigner.isActivate();
    }

    public Assigner getAssigner() {
        return assigner;
    }
}
