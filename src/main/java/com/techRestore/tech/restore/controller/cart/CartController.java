package com.techRestore.tech.restore.controller.cart;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.dto.cart.AddToCartRequestDTO;
import com.techRestore.tech.restore.dto.cart.CartResponseDTO;
import com.techRestore.tech.restore.dto.cart.UpdateCartItemRequestDTO;
import com.techRestore.tech.restore.services.cart.CartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
            Pageable pageable) {

        CartResponseDTO cart = cartService.getCart(pageable);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(
            @RequestBody @Valid AddToCartRequestDTO request,
            Pageable pageable) {
        CartResponseDTO cart = cartService.addItemToCart(request, pageable);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable UUID itemId,
            @RequestBody @Valid UpdateCartItemRequestDTO request,
            Pageable pageable) {

        CartResponseDTO cart = cartService.updateCartItem(itemId, request, pageable);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable UUID itemId) {
        cartService.removeCartItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
