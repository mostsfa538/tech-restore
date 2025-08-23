package com.techRestore.tech.restore.services.cart;

import com.techRestore.tech.restore.dto.cart.AddToCartRequestDTO;
import com.techRestore.tech.restore.dto.cart.CartItemResponseDTO;
import com.techRestore.tech.restore.dto.cart.CartResponseDTO;
import com.techRestore.tech.restore.dto.cart.UpdateCartItemRequestDTO;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.CartItem;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.repository.CartItemRepository;
import com.techRestore.tech.restore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public CartResponseDTO getCart(UUID userId, Pageable pageable) {
        Page<CartItem> items = cartItemRepository.findByUserId(userId,pageable);
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
    public CartResponseDTO addItemToCart(UUID userId, AddToCartRequestDTO request,Pageable pageable) {
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
            CartItem savedItem = cartItemRepository.save(newItem);
        }

        return getCart(userId, pageable);
    }

    @Transactional
    public CartResponseDTO updateCartItem(UUID userId, UUID itemId, UpdateCartItemRequestDTO request,Pageable pageable) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        }

        return getCart(userId, pageable);
    }

    @Transactional
    public void removeCartItem(UUID userId, UUID itemId) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));
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

        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));
        dto.setProductName(product.getName());
        dto.setProductPrice(product.getPrice());
        dto.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        dto.setShopId(cartItem.getShopId());

        return dto;
    }
}

