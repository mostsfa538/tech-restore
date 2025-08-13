package com.techRestore.tech.restore.services.shop;

import com.techRestore.tech.restore.dto.common.address.AddressRequest;
import com.techRestore.tech.restore.dto.common.address.AddressResponse;
import com.techRestore.tech.restore.dto.common.address.AddressUpdate;
import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.shop.ShopUpdateRequest;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.entities.ShopAddress;
import com.techRestore.tech.restore.repository.ShopAddressRepository;
import com.techRestore.tech.restore.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShopServices {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopAddressRepository shopAddressRepository;

    private Shop getShop() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Shop> shop = shopRepository.findByEmail(authentication.getName());

        if (shop.isEmpty()) {
            throw new NotFoundException("Shop Not Found");
        }
        return shop.get();
    }

    public List<AddressResponse> getAllAddresses() {
        Shop shop = getShop();
        List<ShopAddress> addresses = shopAddressRepository.findShopAddressByShopId(shop.getId());
        return addresses.stream()
                .map(this::convertDto)
                .toList();
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ShopResponseDto updateShop(ShopUpdateRequest shopUpdateRequest) {
        Shop shop = getShop();

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

        return toShopDto(shop);
    }

    public void addAddress(AddressRequest addressRequest) {
        Shop shop = getShop();
        ShopAddress shopAddress = new ShopAddress();

        shopAddress.setShop(shop);
        shopAddress.setState(addressRequest.state());
        shopAddress.setCity(addressRequest.city());
        shopAddress.setBuilding(addressRequest.building());
        shopAddress.setStreet(addressRequest.street());
        shopAddress.setNotes(addressRequest.notes());
        shopAddress.setDefault(addressRequest.isDefault());

        shopAddressRepository.save(shopAddress);
    }

    public void updateAddress(UUID id, AddressUpdate addressUpdate) {
        getShop();

        Optional<ShopAddress> address = shopAddressRepository.findById(id);
        if (address.isEmpty()) {
            throw new NotFoundException("Address Not found");
        }
        ShopAddress shopAddress = address.get();
        if (addressUpdate.state() != null) {
            shopAddress.setState(addressUpdate.state());
        }
        if (addressUpdate.city() != null) {
            shopAddress.setCity(addressUpdate.city());
        }
        if (addressUpdate.building() != null) {
            shopAddress.setBuilding(addressUpdate.building());
        }
        if (addressUpdate.street() != null) {
            shopAddress.setStreet(addressUpdate.street());
        }
        if (addressUpdate.notes() != null) {
            shopAddress.setNotes(addressUpdate.notes());
        }
        shopAddress.setDefault(addressUpdate.isDefault());

        shopAddressRepository.save(shopAddress);
    }

    public void deleteAddress(UUID id) {
        getShop();
        shopAddressRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Address not found"));

        shopAddressRepository.deleteById(id);

    }

    private AddressResponse convertDto(ShopAddress address) {
        return new AddressResponse(
                address.getState(),
                address.getCity(),
                address.getStreet(),
                address.getBuilding(),
                address.getNotes(),
                address.isDefault()
        );
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
