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
import com.techRestore.tech.restore.services.BaseService;
import com.techRestore.tech.restore.utils.DTOConverter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ShopServices extends BaseService<Shop, UUID> {

    private final ShopAddressRepository shopAddressRepository;
    private final ShopRepository Shoprepository;

    public ShopServices(ShopRepository shopRepository, ShopAddressRepository shopAddressRepository) {
        super(shopRepository);
        this.shopAddressRepository = shopAddressRepository;
        this.Shoprepository = shopRepository;
    }

    /**
     * Get current authenticated shop
     */
    private Shop getCurrentShop() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((ShopRepository) repository).findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Shop not found"));
    }

    public Page<ShopResponseDto> getAllShops(Pageable pageable) {
        Page<Shop> shops = Shoprepository.findAll(pageable);
        return shops.map(DTOConverter::convertToShopyDTO);
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

        return DTOConverter.convertToShopyDTO(shop);
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
}