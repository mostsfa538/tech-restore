package com.techRestore.tech.restore.shop.service;

import com.techRestore.tech.restore.common.dto.auth.ShopRegistrationRequest;
import com.techRestore.tech.restore.common.interfaces.RegistrationStrategy;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.ShopAddress;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopRegistrationStrategy implements RegistrationStrategy<Shop, ShopRegistrationRequest> {

    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Shop createEntity(ShopRegistrationRequest registrationData) {
        Shop shop = new Shop();
        shop.setEmail(registrationData.email());
        shop.setPassword(passwordEncoder.encode(registrationData.password()));
        shop.setName(registrationData.name());
        shop.setPhone(registrationData.phone());
        shop.setDescription(registrationData.description());
        shop.setVerified(registrationData.verified());
        shop.setShopType(registrationData.shopType());
        shop.setSubscriptionMonths(0);
        shop.setActivate(false);
        ShopAddress address = new ShopAddress();
        address.setState(registrationData.shopAddress().state());
        address.setCity(registrationData.shopAddress().city());
        address.setStreet(registrationData.shopAddress().street());
        address.setBuilding(registrationData.shopAddress().building());
        address.setNotes(registrationData.shopAddress().notes());
        address.setDefault(registrationData.shopAddress().isDefault());
        address.setShop(shop);

        return shop;
    }

    @Override
    public Shop saveEntity(Shop entity) {
        return shopRepository.save(entity);
    }

    @Override
    public String getEmail(ShopRegistrationRequest registrationData) {
        return registrationData.email();
    }

    @Override
    public String getSuccessMessage(Shop entity) {
        return "Registration successfully, wait for acceptance";
    }
}