package com.techRestore.tech.restore.common.utils;

import com.techRestore.tech.restore.admin.dto.CategoryDTO;
import com.techRestore.tech.restore.common.dto.address.AddressResponse;
import com.techRestore.tech.restore.common.model.entities.*;
import com.techRestore.tech.restore.shop.dto.offers.OfferResponseDTO;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.user.dto.order.OrderItemResponseDTO;
import com.techRestore.tech.restore.user.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.user.dto.reviews.ReviewResponseDTO;
import com.techRestore.tech.restore.user.dto.user.UserProfileDTO;

import org.hibernate.LazyInitializationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

        UUID categoryId = null;
        try {
            if (product.getCategory() != null) {
                categoryId = product.getCategory().getId();
            }
        } catch (LazyInitializationException e) {
            categoryId = null;
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
                categoryId,
                categoryName,
                product.isDeleted());
    }

    public static CategoryDTO convertToCategoryDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName());
    }

    public static ShopResponseDto convertToShopDTO(Shop shop) {
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
        dto.setActivate(shop.isActivate());
        dto.setShopType(shop.getShopType() != null ? shop.getShopType().toString() : null);

        if (shop.getAddresses() != null && !shop.getAddresses().isEmpty()) {
            dto.setShopAddress(shop.getAddresses().get(0));
        }

        return dto;
    }

    public static AddressResponse convertToAddressDTO(ShopAddress address) {
        AddressResponse dto = new AddressResponse();
        dto.setId(address.getId());
        dto.setState(address.getState());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setBuilding(address.getBuilding());
        dto.setNotes(address.getNotes());
        dto.setDefault(address.isDefault());
        dto.setUserId(address.getShop().getId());
        dto.setCreatedAt(address.getCreatedAt());
        return dto;
    }

    public static AddressResponse convertToUserAddressDTO(Address address) {
        AddressResponse dto = new AddressResponse();
        dto.setId(address.getId());
        dto.setState(address.getState());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setBuilding(address.getBuilding());
        dto.setNotes(address.getNotes());
        dto.setDefault(address.isDefault());
        dto.setUserId(address.getUser().getId());
        dto.setCreatedAt(address.getCreatedAt());
        return dto;
    }

    public static RepairRequestDto convertToRepairRequestDTO(RepairRequest repairRequest, Shop shop) {
        return new RepairRequestDto(
                repairRequest.getId(),
                null,
                repairRequest.getUserId(),
                repairRequest.getShopId(),
                repairRequest.getDeliveryAddress(),
                repairRequest.getPaymentId(),
                repairRequest.getDescription(),
                repairRequest.getDeliveryMethod() != null ? repairRequest.getDeliveryMethod().name() : null,
                repairRequest.getCategoryId(),
                repairRequest.getPaymentMethod() != null ? repairRequest.getPaymentMethod().name() : null,
                repairRequest.isConfirmed(),
                repairRequest.getPrice(),
                repairRequest.getStatus(),
                shop != null ? shop.getName() : null);
    }

    public static OrderResponseDTO convertToOrderResponseDTO(Order order, String shopName) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setDeliveryAddressId(order.getDeliveryAddressId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaymentId(order.getPaymentId());
        dto.setShopName(shopName);
        try {
            if (order.getOrderItems() != null) {
                List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream()
                        .map(DTOConverter::convertToOrderItemResponseDTO)
                        .collect(Collectors.toList());
                dto.setOrderItems(itemDTOs);
            }
        } catch (LazyInitializationException e) {
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

        if (item.getQuantity() != null && item.getPriceAtCheckout() != null) {
            dto.setSubtotal(item.getPriceAtCheckout().multiply(new BigDecimal(item.getQuantity())));
        }

        return dto;
    }

    public static OrderResponseDTO convertToOrderResponseDTO(Order order, List<OrderItem> orderItems, String shopName) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setDeliveryAddressId(order.getDeliveryAddressId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaymentId(order.getPaymentId());

        List<OrderItemResponseDTO> itemDTOs = orderItems.stream()
                .map(DTOConverter::convertToOrderItemResponseDTO)
                .collect(Collectors.toList());
        dto.setOrderItems(itemDTOs);
        dto.setShopName(shopName);

        return dto;
    }

    public static OfferResponseDTO convertToOfferResponseDTO(Offer offer) {
        OfferResponseDTO dto = new OfferResponseDTO();
        dto.setId(offer.getId());
        dto.setName(offer.getName());
        dto.setDescription(offer.getDescription());
        dto.setDiscountValue(offer.getDiscountValue());
        dto.setDiscountType(offer.getDiscountType());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        dto.setStatus(offer.getStatus());
        dto.setShopId(offer.getShop().getId());
        dto.setCreatedAt(offer.getCreatedAt());
        dto.setUpdatedAt(offer.getUpdatedAt());
        return dto;
    }

    public static ReviewResponseDTO toReviewResponseDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setShopId(review.getShopId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    public static UserProfileDTO convertToUserProfileDTO(User user) {
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setId(user.getId());
        profileDTO.setFirst_name(user.getFirst_name());
        profileDTO.setLast_name(user.getLast_name());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setPhone(user.getPhone());
        profileDTO.setActivate(user.isActivate());
        profileDTO.setRole(user.getRole());
        profileDTO.setCreatedAt(user.getCreatedAt());
        profileDTO.setUpdatedAt(user.getUpdatedAt());

        if (user.getAddresses() != null) {
            profileDTO.setAddresses(
                    user.getAddresses().stream()
                            .map(DTOConverter::convertToUserAddressDTO)
                            .toList());
        }

        return profileDTO;
    }

}