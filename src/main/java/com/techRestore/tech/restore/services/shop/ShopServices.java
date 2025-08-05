package com.techRestore.tech.restore.services.shop;

import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.shop.ShopUpdateRequest;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShopServices {
    @Autowired
    private ShopRepository shopRepository;

    public List<Shop> getShops() {
        return shopRepository.findAll();
    }

    public ShopResponseDto getShopById(UUID id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shop not found with id: " + id));
        return toShopDto(shop);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    public void updateShop(UUID id, ShopUpdateRequest shopUpdateRequest) {
        Optional<Shop> findShop = shopRepository.findById(id);
        if (findShop.isEmpty()) {
            throw new NotFoundException("Shop Not Found");
        }
        Shop shop = findShop.get();
        if (shopUpdateRequest.description() != null) {
            shop.setDescription(shopUpdateRequest.description());
        }
        if (shopUpdateRequest.name() != null) {
            shop.setName(shopUpdateRequest.name());
        }
        if (shopUpdateRequest.phone() != null) {
            shop.setPhone(shopUpdateRequest.phone());
        }
        shopRepository.save(shop);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteShop(UUID id) {
        Optional<Shop> findShop = shopRepository.findById(id);
        if (findShop.isEmpty()) {
            throw new NotFoundException("Shop Not Found");
        }
        shopRepository.deleteById(id);
    }

    public List<Shop> search(String name) {
        return shopRepository.findByName(name);
    }

    public ShopResponseDto toShopDto(Shop shop) {
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
}
