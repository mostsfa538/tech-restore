package com.techRestore.tech.restore.security.config;

import com.techRestore.tech.restore.security.filter.JWTAuthenticationFilter;
import com.techRestore.tech.restore.security.userdetails.DeliveryDetailsServiceImpl;
import com.techRestore.tech.restore.security.userdetails.ShopDetailsServiceImpl;
import com.techRestore.tech.restore.security.userdetails.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh",
                                "/api/auth/shops/**")
                        .permitAll()
                        .requestMatchers(
                            "/api/auth/delivery/register",
                            "/api/auth/delivery/login",      
                            "/api/auth/delivery/refresh"     
                        ).permitAll()
                        .requestMatchers("/api/delivery/**").permitAll()
                        .requestMatchers("/api/shops/orders/control/**").hasAnyRole("SELLER", "BOTH")
                        .requestMatchers("/api/cart/**").hasAnyRole("GUEST")
                        .requestMatchers("/api/auth/home", "/api/auth/logout").authenticated()
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/shops/products").hasAnyRole("SELLER")
                        .requestMatchers("/api/shops/repair-request/**").hasAnyRole("REPAIRER")
                        .requestMatchers("/api/users/**", "/api/products/**").hasAnyRole("GUEST")
                        .requestMatchers("/api/reviews/**").hasAnyRole("GUEST", "SHOP_OWNER"))

                .authenticationManager(customAuthenticationManager())

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
}