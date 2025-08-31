package com.techRestore.tech.restore.services.auth;

import com.techRestore.tech.restore.dto.auth.LoginDto;
import com.techRestore.tech.restore.dto.auth.ShopRegistrationRequest;
import com.techRestore.tech.restore.dto.auth.TokenResponse;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.entities.ShopAddress;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.repository.UserRepository;
import com.techRestore.tech.restore.security.config.CustomAuthenticationManager;
import com.techRestore.tech.restore.security.jwt.JwtService;
import com.techRestore.tech.restore.security.jwt.RefreshTokenService;
import com.techRestore.tech.restore.services.EmailService;
import com.techRestore.tech.restore.utils.EmailVerificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopAuthService {
    private final ShopRepository shopRepository;

    private final PasswordEncoder passwordEncoder;

    private final CustomAuthenticationManager customAuthenticationManager;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final UserRepository userRepository;
    
    private final EmailService emailService;

    public String register(ShopRegistrationRequest shopRegistrationRequest) {
        if (shopRepository.existsByEmail(shopRegistrationRequest.email())
                && userRepository.existsByEmail(shopRegistrationRequest.email()))
            throw new RuntimeException("Emails is already exists");

        try {
            Shop shop = new Shop();
            shop.setEmail(shopRegistrationRequest.email());
            shop.setPassword(passwordEncoder.encode(shopRegistrationRequest.password()));
            shop.setName(shopRegistrationRequest.name());
            shop.setPhone(shopRegistrationRequest.phone());
            shop.setDescription(shopRegistrationRequest.description());
            shop.setVerified(shopRegistrationRequest.verified());
            shop.setShopType(shopRegistrationRequest.shopType());
            
            // Set email verification fields
            String verificationToken = EmailVerificationUtils.generateVerificationToken();
            shop.setEmailVerificationToken(verificationToken);
            shop.setEmailTokenExpiry(EmailVerificationUtils.getTokenExpiry());
            shop.setEmailVerified(false);

            ShopAddress address = new ShopAddress();
            address.setState(shopRegistrationRequest.shopAddress().state());
            address.setCity(shopRegistrationRequest.shopAddress().city());
            address.setStreet(shopRegistrationRequest.shopAddress().street());
            address.setBuilding(shopRegistrationRequest.shopAddress().building());
            address.setNotes(shopRegistrationRequest.shopAddress().notes());
            address.setDefault(shopRegistrationRequest.shopAddress().isDefault());

            address.setShop(shop);
            shopRepository.save(shop);
            
            // Send verification email
            try {
                emailService.sendVerificationEmail(shop.getEmail(), verificationToken, "shops");
            } catch (Exception emailException) {
                System.err.println("Failed to send verification email: " + emailException.getMessage());
                // Continue registration even if email fails
            }

            return "Shop registered successfully. Please check your email to verify your account before logging in.";

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TokenResponse login(LoginDto loginDto) {
        try {
            // Check if shop exists and email is verified
            Shop shop = shopRepository.findByEmail(loginDto.email()).orElse(null);
            if (shop != null && !shop.isEmailVerified()) {
                throw new RuntimeException("Please verify your email before logging in. Check your inbox for the verification link.");
            }

            Authentication authentication = customAuthenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));

            String accessToken = jwtService.generateAccessToken(authentication);
            String refreshToken = jwtService.generateRefreshToken(authentication);

            refreshTokenService.saveRefreshToken(authentication.getName(), refreshToken);

            return new TokenResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    15 * 60);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        } catch (DisabledException e) {
            throw new DisabledException("Account is disabled");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String verifyEmail(String token) {
        Shop shop = shopRepository.findByEmailVerificationToken(token);
        if (shop == null) {
            throw new RuntimeException("Invalid verification token");
        }

        if (EmailVerificationUtils.isTokenExpired(shop.getEmailTokenExpiry())) {
            throw new RuntimeException("Verification token has expired");
        }

        shop.setEmailVerified(true);
        shop.setEmailVerificationToken(null);
        shop.setEmailTokenExpiry(null);
        shopRepository.save(shop);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(shop.getEmail(), shop.getName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return "Email verified successfully! You can now log in.";
    }

    public String resendVerificationEmail(String email) {
        Shop shop = shopRepository.findByEmail(email).orElse(null);
        if (shop == null) {
            throw new RuntimeException("Shop not found");
        }

        if (shop.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        // Generate new verification token
        String verificationToken = EmailVerificationUtils.generateVerificationToken();
        shop.setEmailVerificationToken(verificationToken);
        shop.setEmailTokenExpiry(EmailVerificationUtils.getTokenExpiry());
        shopRepository.save(shop);

        // Send verification email
        emailService.sendVerificationEmail(shop.getEmail(), verificationToken, "shops");

        return "Verification email sent successfully";
    }
}
