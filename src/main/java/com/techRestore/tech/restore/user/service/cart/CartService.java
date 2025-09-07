package com.techRestore.tech.restore.user.service.cart;

import com.techRestore.tech.restore.common.exception.ActivationException;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.CartItem;
import com.techRestore.tech.restore.common.model.entities.Product;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.shop.repository.ProductRepository;
import com.techRestore.tech.restore.user.dto.cart.AddToCartRequestDTO;
import com.techRestore.tech.restore.user.dto.cart.CartItemResponseDTO;
import com.techRestore.tech.restore.user.dto.cart.CartResponseDTO;
import com.techRestore.tech.restore.user.dto.cart.UpdateCartItemRequestDTO;
import com.techRestore.tech.restore.user.repository.CartItemRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new NotFoundException("User not found: " + email);
        }
        if (!user.isActivate()) {
            throw new ActivationException("User account is deactivated: " + email);
        }

        return user.getId();
    }

    @Transactional(readOnly = true)
    public CartResponseDTO getCart(Pageable pageable) {
        UUID userId = getCurrentUserId();
        Page<CartItem> items = cartItemRepository.findByUserId(userId, pageable);
        List<CartItemResponseDTO> itemDTOs = items
                .map(this::mapToCartItemResponseDTO)
                .getContent();

        CartResponseDTO dto = new CartResponseDTO();
        dto.setUserId(userId);
        dto.setItems(itemDTOs);
        dto.setTotalItems(itemDTOs.stream().mapToInt(CartItemResponseDTO::getQuantity).sum());
        dto.setTotalPrice(itemDTOs.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        dto.setPage(items.getNumber());
        dto.setSize(items.getSize());
        dto.setTotalElements(items.getTotalElements());
        dto.setTotalPages(items.getTotalPages());

        return dto;
    }

    @Transactional
    public CartResponseDTO addItemToCart(AddToCartRequestDTO request, Pageable pageable) {
        UUID userId = getCurrentUserId();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        CartItem existingItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProductId(request.getProductId());
            newItem.setShopId(product.getShopId());
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
        }

        return getCart(pageable);
    }

    @Transactional
    public CartResponseDTO updateCartItem(UUID itemId, UpdateCartItemRequestDTO request,
            Pageable pageable) {
        UUID userId = getCurrentUserId();
        CartItem cartItem = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        }

        return getCart(pageable);
    }

    @Transactional
    public void removeCartItem(UUID itemId) {
        UUID userId = getCurrentUserId();

        CartItem cartItem = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart() {
        UUID userId = getCurrentUserId();
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        cartItemRepository.deleteAll(items);
    }

    private CartItemResponseDTO mapToCartItemResponseDTO(CartItem cartItem) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProductId());
        dto.setQuantity(cartItem.getQuantity());

        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));
        dto.setProductName(product.getName());
        dto.setProductPrice(product.getPrice());
        dto.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        dto.setShopId(cartItem.getShopId());

        return dto;
    }
}
