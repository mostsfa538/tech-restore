package com.techRestore.tech.restore.common.security.config;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.Role;
import com.techRestore.tech.restore.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);

            String email = oAuth2User.getAttribute("email");
            if (email == null || email.trim().isEmpty()) {
                throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
            }

            String givenName = oAuth2User.getAttribute("given_name");
            String familyName = oAuth2User.getAttribute("family_name");
            String fullName = oAuth2User.getAttribute("name");

            User user = userRepository.findByEmail(email);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setFirst_name(givenName != null ? givenName : extractFirstName(fullName));
                user.setLast_name(familyName != null ? familyName : extractLastName(fullName));
                user.setPassword("{noop}" + UUID.randomUUID().toString());
                user.setRole(Role.GUEST);
                user.setActivate(true);
                user = userRepository.save(user);
            }

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                    oAuth2User.getAttributes(),
                    "email");

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("OAuth2 login failed: " + e.getMessage());
        }
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : "";
    }
}
