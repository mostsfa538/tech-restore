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

            ShopAddress address = new ShopAddress();
            address.setState(shopRegistrationRequest.shopAddress().state());
            address.setCity(shopRegistrationRequest.shopAddress().city());
            address.setStreet(shopRegistrationRequest.shopAddress().street());
            address.setBuilding(shopRegistrationRequest.shopAddress().building());
            address.setNotes(shopRegistrationRequest.shopAddress().notes());
            address.setDefault(shopRegistrationRequest.shopAddress().isDefault());

            address.setShop(shop);
            shopRepository.save(shop);

            return "Registration successfully, wait for acceptance";

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TokenResponse login(LoginDto loginDto) {
        try {
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
}
