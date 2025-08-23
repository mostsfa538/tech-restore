package com.techRestore.tech.restore.utils;

import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.category.CategoryDTO;
import com.techRestore.tech.restore.dto.common.address.AddressResponse;
import com.techRestore.tech.restore.dto.order.OrderItemResponseDTO;
import com.techRestore.tech.restore.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.model.entities.*;
import org.hibernate.LazyInitializationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DTOConverter {

    public static ProductResponseDTO convertToProductDTO(Product product) {
        String categoryName = null;
        try {
            if (product.getCategory() != null) {
                categoryName = product.getCategory().getName();
            }
        } catch (LazyInitializationException e) {
            categoryName = null;
        }

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getCondition(),
                product.getCreatedAt(),
                categoryName
        );
    }

    public static CategoryDTO convertToCategoryDTO(Category category) {
        return new CategoryDTO(category.getName());
    }

    public static ShopResponseDto convertToShopyDTO(Shop shop) {
        ShopResponseDto dto = new ShopResponseDto();
        dto.setId(shop.getId());
        dto.setEmail(shop.getEmail());
        dto.setName(shop.getName());
        dto.setDescription(shop.getDescription());
        dto.setVerified(shop.getVerified());
        dto.setPhone(shop.getPhone());
        dto.setRating(shop.getRating());
        dto.setCreatedAt(shop.getCreatedAt());
        dto.setUpdatedAt(shop.getUpdatedAt());
        return dto;
    }

    public static AddressResponse convertToAddressDTO(ShopAddress address) {
        return new AddressResponse(
                address.getState(),
                address.getCity(),
                address.getStreet(),
                address.getBuilding(),
                address.getNotes(),
                address.isDefault()
        );
    }

        public static RepairRequestDto convertToRepairRequestDTO(RepairRequest repairRequest) {
        return new RepairRequestDto(
                repairRequest.getId(),
                null,
                repairRequest.getUserId(),
                repairRequest.getShopId(),
                repairRequest.getDeliveryAddress(),
                repairRequest.getPaymentId(),
                repairRequest.getDescription(),
                repairRequest.getDeliveryMethod().name(),
                repairRequest.getCategoryId(),
                repairRequest.getPaymentMethod().name(),
                repairRequest.isConfirmed(),
                repairRequest.getStatus()
        );
    }

    // Alias method to match the method name used in UserServices
    public static RepairRequestDto convertToRepairRequestDto(RepairRequest repairRequest) {
        return convertToRepairRequestDTO(repairRequest);
    }

    public static OrderResponseDTO convertToOrderResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setDeliveryAddressId(order.getDeliveryAddressId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaymentId(order.getPaymentId());

        // Convert order items if they are loaded
        try {
            if (order.getOrderItems() != null) {
                List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream()
                        .map(DTOConverter::convertToOrderItemResponseDTO)
                        .collect(Collectors.toList());
                dto.setOrderItems(itemDTOs);
            }
        } catch (LazyInitializationException e) {
            // Items were not loaded, set empty list
            dto.setOrderItems(new ArrayList<>());
        }

        return dto;
    }

    public static OrderItemResponseDTO convertToOrderItemResponseDTO(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtCheckout(item.getPriceAtCheckout());
        dto.setShopId(item.getShopId());

        // Calculate subtotal
        if (item.getQuantity() != null && item.getPriceAtCheckout() != null) {
            dto.setSubtotal(item.getPriceAtCheckout().multiply(new BigDecimal(item.getQuantity())));
        }

        return dto;
    }
}