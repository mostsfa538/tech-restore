package com.techRestore.tech.restore.services.auth;

import com.techRestore.tech.restore.dto.auth.LoginDto;
import com.techRestore.tech.restore.dto.auth.ShopRegistrationRequest;
import com.techRestore.tech.restore.dto.auth.TokenResponse;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.repository.UserRepository;
import com.techRestore.tech.restore.security.jwt.JwtService;
import com.techRestore.tech.restore.security.jwt.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ShopAuthService {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

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

            shopRepository.save(shop);

            return shop.getId().toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TokenResponse login(LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.email(),
                            loginDto.password()
                    )
            );

            Shop shop = shopRepository.findByEmail(loginDto.email())
                    .orElseThrow(() -> new UsernameNotFoundException("Shop not found"));

            if (!shop.getVerified()) {
                throw new RuntimeException("Shop account is suspended");
            }

            String accessToken = jwtService.generateAccessToken(authentication);
            String refreshToken = jwtService.generateRefreshToken(authentication);

            refreshTokenService.saveRefreshToken(authentication.getName(), refreshToken);

            return new TokenResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    15 * 60
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        } catch (DisabledException e) {
            throw new DisabledException("Account is disabled");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

