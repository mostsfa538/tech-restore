package com.techRestore.tech.restore.common.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.techRestore.tech.restore.common.security.filter.JWTAuthenticationFilter;
import com.techRestore.tech.restore.common.security.jwt.JwtService;
import com.techRestore.tech.restore.common.security.userdetails.DeliveryDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.ShopDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.UserDetailsServiceImpl;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class AppConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final ShopDetailsServiceImpl shopDetailsService;
    private final DeliveryDetailsServiceImpl deliveryDetailsService;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/login/oauth2/code/**", "/oauth2/authorization/**").permitAll()
                        .requestMatchers("/api/auth/register/user", "/api/auth/login",
                                "/api/auth/verify-email", "/api/auth/resend-otp", "api/auth/forgot-password",
                                "/api/auth/test-cookie",
                                "/api/auth/register/shop",
                                "/api/auth/register/delivery",
                                "api/auth/reset-password")
                        .permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/payments/**").hasAnyRole("GUEST")
                        .requestMatchers("/shops/connected").hasRole("GUEST")
                        .requestMatchers("/messages/**").hasAnyRole("GUEST", "SHOP_OWNER")
                        .requestMatchers("/api/webhook/**").permitAll()
                        .requestMatchers("/api/AllShops").authenticated()
                        .requestMatchers("/api/delivery/**").hasAnyRole("DELIVERY")
                        .requestMatchers("/api/shops/orders/control/**").hasAnyRole("SELLER", "BOTH")
                        .requestMatchers("/api/cart/**").hasAnyRole("GUEST")
                        .requestMatchers("/api/auth/home", "/api/auth/logout", "/api/auth/logout-all",
                                "/api/auth/refresh-token").authenticated()
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/shops/products/**", "/api/shop/offers/**").hasAnyRole("SELLER", "BOTH")
                        .requestMatchers("/api/shops/repair-request/**").hasAnyRole("REPAIRER")
                        .requestMatchers("/api/users/**", "/api/products/**").hasAnyRole("GUEST")
                        .requestMatchers("/api/reviews/**").hasAnyRole("GUEST", "SHOP_OWNER"))

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/google")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler())
                        .failureHandler(oAuth2AuthenticationFailureHandler()))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:4200")); // Front-End
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
        corsConfiguration.setExposedHeaders(List.of("Authorization"));
        corsConfiguration.setMaxAge(3600L); // Duration (in seconds) that the browser can cache the CORS preflight
        // response (here: 1 hour)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public CustomAuthenticationManager customAuthenticationManager() {
        return new CustomAuthenticationManager(
                userDetailsService,
                shopDetailsService,
                deliveryDetailsService,
                passwordEncoder());
    }

    private AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            try {
                jwtService.generateAccessToken(authentication);

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String body = String.format("{\"message\": \"OAuth2 login successful\", \"email\": \"%s\"}",
                        authentication.getName());
                response.getWriter().write(body);
            } catch (Exception e) {

            }
        };
    }

    private AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String body = String.format("{\"message\": \"OAuth2 login fail\", \"email\": \"%s\"}");
            response.getWriter().write(body);
        };
    }
}
