package com.techRestore.tech.restore.common.security.config;

import lombok.RequiredArgsConstructor;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
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
import com.techRestore.tech.restore.common.security.jwt.RefreshTokenService;
import com.techRestore.tech.restore.common.security.userdetails.AssignerDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.DeliveryDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.ShopDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.UserDetailsServiceImpl;
import com.techRestore.tech.restore.common.utils.CookieUtil;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableCaching
public class AppConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final ShopDetailsServiceImpl shopDetailsService;
    private final DeliveryDetailsServiceImpl deliveryDetailsService;
    private final AssignerDetailsServiceImpl assignerDetailsService;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()
                        .requestMatchers("/login/oauth2/code/**", "/oauth2/authorization/**").permitAll()
                        .requestMatchers("/api/auth/register/user", "/api/auth/login",
                                "/api/auth/verify-email", "/api/auth/resend-otp", "api/auth/forgot-password",
                                "/api/auth/test-cookie",
                                "/api/auth/register/shop",
                                "/api/auth/register/delivery",
                                "/api/auth/register/assigner",
                                "api/auth/reset-password",
                                "/api/categories")
                        .permitAll()
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/subscriptions/renew/**").permitAll()
                        .requestMatchers("/api/payments/subscription/cash/confirm/{paymentId}/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/subscriptions/**").hasAnyRole("SELLER", "BOTH", "REPAIRER")
                        .requestMatchers("/api/notifications/delivery").hasAnyRole("DELIVERY")
                        .requestMatchers("/api/notifications/assigner").hasAnyRole("ASSIGNER")
                        .requestMatchers("/api/notifications/shops").hasAnyRole("SELLER", "BOTH", "REPAIRER")
                        .requestMatchers("/api/notifications/users").hasAnyRole("GUEST")
                        .requestMatchers("/api/assigner/**").hasAnyRole("ASSIGNER")
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/payments/**").hasAnyRole("GUEST")
                        .requestMatchers("/shops/connected").hasRole("GUEST")
                        .requestMatchers("/api/chats/**").hasAnyRole("GUEST", "SHOP_OWNER")
                        .requestMatchers("/api/webhook/**").permitAll()
                        .requestMatchers("/api/AllShops").authenticated()
                        .requestMatchers("/api/delivery/**").hasAnyRole("DELIVERY")
                        .requestMatchers("/api/shops/orders/control/**").hasAnyRole("SELLER", "BOTH")
                        .requestMatchers("/api/cart/**").hasAnyRole("GUEST")
                        .requestMatchers("/api/auth/home", "/api/auth/logout", "/api/auth/logout-all",
                                "/api/auth/refresh-token")
                        .authenticated()
                        .requestMatchers("api/shops/dashboard/**").hasAnyRole("SELLER", "BOTH", "REPAIRER")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/shops/products/**", "/api/shop/offers/**").hasAnyRole("SELLER", "BOTH")
                        .requestMatchers("/api/shops/repair-request/**").hasAnyRole("REPAIRER")
                        .requestMatchers("/api/shop/inventory/**").hasAnyRole("SELLER", "BOTH")
                        .requestMatchers("/api/users/**", "/api/products/**").hasAnyRole("GUEST")
                        .requestMatchers("/api/reviews/**").hasAnyRole("GUEST", "SHOP_OWNER")
                        .anyRequest().authenticated())

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/google")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler())
                        .failureHandler(oAuth2AuthenticationFailureHandler()))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\": \"Forbidden\"}");
                        }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(
                "https://tech-restore.net",
                "https://www.tech-restore.net"
        ));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
        corsConfiguration.setExposedHeaders(List.of("Authorization"));
        corsConfiguration.setMaxAge(3600L);
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
                assignerDetailsService,
                passwordEncoder());
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(20_000)
                .recordStats());

        cacheManager.setCacheNames(Arrays.asList(
                // products caches
                "products",
                "productPages",
                "shopProductPages",
                "categoryProductPages",
                "shopCategoryProductPages"));

        return cacheManager;
    }

    private AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            try {
                String accessToken = jwtService.generateAccessToken(authentication);
                String refreshToken = jwtService.generateRefreshToken(authentication);

                refreshTokenService.saveRefreshToken(authentication.getName(), refreshToken, request);

                cookieUtil.addAccessTokenCookie(response, accessToken);
                cookieUtil.addRefreshTokenCookie(response, refreshToken);

                cookieUtil.addClientAccessTokenCookie(response, accessToken);
                response.sendRedirect("https://api.tech-restore.net/oauth2/success");

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        String.format("{\"error\":\"%s\"}", e.getMessage()));
            }
        };
    }

    private AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String body = String.format(
                    "{\"message\": \"OAuth2 login fail\", \"error\": \"%s\"}",
                    exception.getMessage());
            response.getWriter().write(body);
        };
    }
}
