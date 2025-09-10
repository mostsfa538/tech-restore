package com.techRestore.tech.restore.common.security.config;

import com.techRestore.tech.restore.common.exception.ActivationException;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.security.userdetails.DeliveryDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.ShopDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.UserDetailsServiceImpl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomAuthenticationManager implements AuthenticationManager {

    private final UserDetailsServiceImpl userDetailsService;
    private final ShopDetailsServiceImpl shopDetailsService;
    private final DeliveryDetailsServiceImpl deliveryDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationManager(
            UserDetailsServiceImpl userDetailsService,
            ShopDetailsServiceImpl shopDetailsService,
            DeliveryDetailsServiceImpl deliveryDetailsService,
            PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.shopDetailsService = shopDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.deliveryDetailsService = deliveryDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication instanceof OAuth2LoginAuthenticationToken) {
            return authentication;
        }
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails != null) {
                if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                    throw new BadCredentialsException("Invalid username or password");
                }

                if (!userDetails.isEnabled()) {
                    throw new ActivationException(
                            "Account is not activated. Please check your email for activation instructions");
                }

                return new UsernamePasswordAuthenticationToken(
                        userDetails,
                        password,
                        userDetails.getAuthorities());
            }

        } catch (UsernameNotFoundException e) {
            // User not found, continue to shop authentication
        }

        try {
            UserDetails shopDetails = shopDetailsService.loadUserByUsername(username);
            if (shopDetails != null) {
                if (!passwordEncoder.matches(password, shopDetails.getPassword())) {
                    throw new BadCredentialsException("Invalid username or password");
                }
                if (!shopDetails.isEnabled()) {
                    throw new ActivationException(
                            "Account is not activated. Please check your email for activation instructions");
                }

                return new UsernamePasswordAuthenticationToken(
                        shopDetails,
                        password,
                        shopDetails.getAuthorities());
            }
        } catch (UsernameNotFoundException e) {
            // Shop not found either
        }
        try {
            UserDetails deliveryDetails = deliveryDetailsService.loadUserByUsername(username);
            if (deliveryDetails != null) {
                if (!passwordEncoder.matches(password, deliveryDetails.getPassword())) {
                    throw new BadCredentialsException("Invalid username or password");
                }
                if (!deliveryDetails.isEnabled()) {
                    throw new ActivationException(
                            "Account is not activated. Please check your email for activation instructions");
                }
                return new UsernamePasswordAuthenticationToken(
                        deliveryDetails,
                        password,
                        deliveryDetails.getAuthorities());
            }
        } catch (UsernameNotFoundException e) {
        }
        throw new BadCredentialsException("Invalid username or password");
    }
}