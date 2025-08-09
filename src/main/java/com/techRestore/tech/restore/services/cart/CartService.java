package com.techRestore.tech.restore.services.cart;

import com.techRestore.tech.restore.dto.cart.AddToCartRequestDTO;
import com.techRestore.tech.restore.dto.cart.CartItemResponseDTO;
import com.techRestore.tech.restore.dto.cart.CartResponseDTO;
import com.techRestore.tech.restore.dto.cart.UpdateCartItemRequestDTO;
import com.techRestore.tech.restore.model.entities.CartItem;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.repository.CartItemRepository;
import com.techRestore.tech.restore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public CartResponseDTO getCart(UUID userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return mapToCartResponseDTO(userId, items);
    }

    @Transactional
    public CartResponseDTO addItemToCart(UUID userId, AddToCartRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem existingItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
        }

        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return mapToCartResponseDTO(userId, items);
    }

    @Transactional
    public CartResponseDTO updateCartItem(UUID userId, UUID itemId, UpdateCartItemRequestDTO request) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        }

        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return mapToCartResponseDTO(userId, items);
    }

    @Transactional
    public void removeCartItem(UUID userId, UUID itemId) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(UUID userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        cartItemRepository.deleteAll(items);
    }

    private CartResponseDTO mapToCartResponseDTO(UUID userId, List<CartItem> cartItems) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setUserId(userId);

        List<CartItemResponseDTO> itemDTOs = cartItems.stream()
                .map(this::mapToCartItemResponseDTO)
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);
        dto.setTotalItems(itemDTOs.stream().mapToInt(CartItemResponseDTO::getQuantity).sum());
        dto.setTotalPrice(itemDTOs.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return dto;
    }

    private CartItemResponseDTO mapToCartItemResponseDTO(CartItem cartItem) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProductId());
        dto.setQuantity(cartItem.getQuantity());

        if (cartItem.getProduct() != null) {
            dto.setProductName(cartItem.getProduct().getName());
            dto.setProductPrice(cartItem.getProduct().getPrice());
            dto.setSubtotal(cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        return dto;
    }
}

