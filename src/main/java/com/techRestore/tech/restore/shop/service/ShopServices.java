package com.techRestore.tech.restore.shop.service;

import com.techRestore.tech.restore.common.dto.address.AddressRequest;
import com.techRestore.tech.restore.common.dto.address.AddressResponse;
import com.techRestore.tech.restore.common.dto.address.AddressUpdate;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.ShopAddress;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.shop.dto.shop.ShopUpdateRequest;
import com.techRestore.tech.restore.shop.repository.ShopAddressRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.dto.reviews.ReviewResponseDTO;
import com.techRestore.tech.restore.user.repository.ReviewRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ShopServices extends BaseService<Shop, UUID> {

    private final ShopAddressRepository shopAddressRepository;
    private final ReviewRepository reviewRepository;

    public ShopServices(ShopRepository shopRepository, ShopAddressRepository shopAddressRepository) {
        super(shopRepository);
        this.shopAddressRepository = shopAddressRepository;
        this.reviewRepository = null;
    }

    /**
     * Get current authenticated shop
     */
    private Shop getCurrentShop() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((ShopRepository) repository).findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Shop not found"));
    }

    public Page<AddressResponse> getAllAddresses(Pageable pageable) {
        Shop shop = getCurrentShop();
        Page<ShopAddress> addresses = shopAddressRepository.findShopAddressByShopId(shop.getId(), pageable);
        return addresses.map(DTOConverter::convertToAddressDTO);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ShopResponseDto updateShop(ShopUpdateRequest shopUpdateRequest) {
        Shop shop = getCurrentShop();

        if (shopUpdateRequest.description() != null) {
            shop.setDescription(shopUpdateRequest.description());
        }
        if (shopUpdateRequest.name() != null) {
            shop.setName(shopUpdateRequest.name());
        }
        if (shopUpdateRequest.phone() != null) {
            shop.setPhone(shopUpdateRequest.phone());
        }
        repository.save(shop);

        return DTOConverter.convertToShopDTO(shop);
    }

    public void addAddress(AddressRequest addressRequest) {
        Shop shop = getCurrentShop();
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
        getCurrentShop();

        ShopAddress shopAddress = findByIdOrThrow(shopAddressRepository, id, "Address");

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
        getCurrentShop();
        deleteByIdOrThrow(shopAddressRepository, id, "Address");
    }

    public Page<ReviewResponseDTO> getReviewsByShopId(Pageable pageable) {
        UUID shopId = getCurrentShop().getId();
        return reviewRepository.findAllByShopId(shopId, pageable)
                .map(DTOConverter::toReviewResponseDTO);
    }

    @Transactional(readOnly = true)
    public ShopResponseDto getShopById(UUID shopId) {
        Shop shop = findByIdOrThrow(repository, shopId, "Shop");
        return DTOConverter.convertToShopDTO(shop);
    }

}