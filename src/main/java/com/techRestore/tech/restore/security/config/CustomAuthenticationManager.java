package com.techRestore.tech.restore.security.config;

import com.techRestore.tech.restore.security.userdetails.ShopDetailsServiceImpl;
import com.techRestore.tech.restore.security.userdetails.UserDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationManager implements AuthenticationManager {

    private final UserDetailsServiceImpl userDetailsService;
    private final ShopDetailsServiceImpl shopDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationManager(
            UserDetailsServiceImpl userDetailsService,
            ShopDetailsServiceImpl shopDetailsService,
            PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.shopDetailsService = shopDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (userDetails != null && passwordEncoder.matches(password, userDetails.getPassword())) {
                return new UsernamePasswordAuthenticationToken(
                        userDetails,
                        password,
                        userDetails.getAuthorities()
                );
            }
        } catch (UsernameNotFoundException e) {
            // User not found, continue to shop authentication
        }

        try {
            UserDetails shopDetails = shopDetailsService.loadUserByUsername(username);
            if (shopDetails != null && passwordEncoder.matches(password, shopDetails.getPassword())) {
                return new UsernamePasswordAuthenticationToken(
                        shopDetails,
                        password,
                        shopDetails.getAuthorities()
                );
            }
        } catch (UsernameNotFoundException e) {
            // Shop not found either
        }

        throw new BadCredentialsException("Invalid username or password");
    }
}