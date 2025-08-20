package com.techRestore.tech.restore.utils;

import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.category.CategoryDTO;
import com.techRestore.tech.restore.dto.common.address.AddressResponse;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.entities.ShopAddress;
import com.techRestore.tech.restore.model.entities.Category;
import org.hibernate.LazyInitializationException;

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
} 