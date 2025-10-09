package com.techRestore.tech.restore.common.security.config;

import com.techRestore.tech.restore.common.exception.AccountNotApprovedException;
import com.techRestore.tech.restore.common.exception.ActivationException;
import com.techRestore.tech.restore.common.model.enums.ApprovalStatus;
import com.techRestore.tech.restore.common.security.userdetails.AssignerDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.AssignerPrincipal;
import com.techRestore.tech.restore.common.security.userdetails.DeliveryDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.DeliveryPrincipal;
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
    private final AssignerDetailsServiceImpl assignerDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationManager(
            UserDetailsServiceImpl userDetailsService,
            ShopDetailsServiceImpl shopDetailsService,
            DeliveryDetailsServiceImpl deliveryDetailsService,
            AssignerDetailsServiceImpl assignerDetailsService,
            PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.shopDetailsService = shopDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.deliveryDetailsService = deliveryDetailsService;
        this.assignerDetailsService = assignerDetailsService;
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

                if (deliveryDetails instanceof DeliveryPrincipal deliveryPrincipal) {
                    var delivery = deliveryPrincipal.getDelivery();

                    if (delivery.getStatus() != ApprovalStatus.APPROVED) {
                        String message = switch (delivery.getStatus()) {
                            case PENDING -> "Your account is pending approval by admin. Please wait for approval.";
                            case SUSPENDED -> "Your account has been suspended. Please contact support.";
                            default -> "Account status invalid. Please contact support.";
                        };
                        throw new AccountNotApprovedException(message);
                    }

                    if (!delivery.isActivate()) {
                        throw new ActivationException(
                                "Account is not activated. Please check your email for activation instructions");
                    }
                }

                return new UsernamePasswordAuthenticationToken(
                        deliveryDetails,
                        password,
                        deliveryDetails.getAuthorities());
            }
        } catch (UsernameNotFoundException e) {
        }

        try {
            UserDetails assignerDetails = assignerDetailsService.loadUserByUsername(username);
            if (assignerDetails != null) {
                if (!passwordEncoder.matches(password, assignerDetails.getPassword())) {
                    throw new BadCredentialsException("Invalid username or password");
                }
                if (!assignerDetails.isEnabled()) {
                    throw new ActivationException(
                            "Account is not activated. Please check your email for activation instructions");
                }

                if (assignerDetails instanceof AssignerPrincipal assignerPrincipal) {
                    var assigner = assignerPrincipal.getAssigner();

                    if (assigner.getStatus() != ApprovalStatus.APPROVED) {
                        String message = switch (assigner.getStatus()) {
                            case PENDING -> "Your account is pending approval by admin. Please wait for approval.";
                            case SUSPENDED -> "Your account has been suspended. Please contact support.";
                            default -> "Account status invalid. Please contact support.";
                        };
                        throw new AccountNotApprovedException(message);
                    }

                    if (!assigner.isActivate()) {
                        throw new ActivationException(
                                "Account is not activated. Please check your email for activation instructions");
                    }
                }

                return new UsernamePasswordAuthenticationToken(
                        assignerDetails,
                        password,
                        assignerDetails.getAuthorities());
            }
        } catch (UsernameNotFoundException e) {
        }

        throw new BadCredentialsException("Invalid username or password");
    }
}