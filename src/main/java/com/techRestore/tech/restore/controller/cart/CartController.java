package com.techRestore.tech.restore.controller.cart;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.dto.cart.AddToCartRequestDTO;
import com.techRestore.tech.restore.dto.cart.CartResponseDTO;
import com.techRestore.tech.restore.dto.cart.UpdateCartItemRequestDTO;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.UserRepository;
import com.techRestore.tech.restore.services.cart.CartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null || !user.isActivate()) {
            throw new RuntimeException("User account is deactivated or not found: " + email);
        }

        return user.getId();
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
            Pageable pageable) {

        UUID userId = getCurrentUserId();
        // Pageable pageable = PageRequest.of(page, size);
        CartResponseDTO cart = cartService.getCart(userId, pageable);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(
            @RequestBody AddToCartRequestDTO request,
            Pageable pageable) {

        UUID userId = getCurrentUserId();
        // Pageable pageable = PageRequest.of(page, size);
        CartResponseDTO cart = cartService.addItemToCart(userId, request, pageable);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable UUID itemId,
            @RequestBody UpdateCartItemRequestDTO request,
            Pageable pageable) {

        UUID userId = getCurrentUserId();
        // Pageable pageable = PageRequest.of(page, size);
        CartResponseDTO cart = cartService.updateCartItem(userId, itemId, request, pageable);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable UUID itemId) {
        UUID userId = getCurrentUserId();
        cartService.removeCartItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        UUID userId = getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
