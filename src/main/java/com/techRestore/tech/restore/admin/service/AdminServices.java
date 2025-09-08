package com.techRestore.tech.restore.admin.service;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Offer;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.Role;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.offers.OfferResponseDTO;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.shop.repository.OffersRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.dto.user.ResponseUsersDto;
import com.techRestore.tech.restore.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AdminServices extends BaseService<User, UUID> {
    @Autowired
    private ShopRepository shopRepository;

    private OffersRepository offersRepository;

    public AdminServices(UserRepository userRepository) {
        super(userRepository);
    }

    public Page<ResponseUsersDto> getAllUsers(Pageable pageable) {
        return repository.findAll(pageable).map(this::convertDto);
    }

    public ResponseUsersDto getUserDetailsById(UUID id) {
        User user = findByIdOrThrow(id, "User");
        return convertDto(user);
    }

    public void suspendUser(UUID id) {
        User user = findByIdOrThrow(id, "User");
        user.setActivate(false);
        repository.save(user);
    }

    public void approveUser(UUID id) {
        User user = findByIdOrThrow(id, "User");
        user.setActivate(true);
        repository.save(user);
    }

    public Page<ShopResponseDto> getShops(Pageable pageable) {
        return shopRepository.findAll(pageable)
                .map(DTOConverter::convertToShopyDTO);
    }

    public Page<ShopResponseDto> getApprovedShops(Pageable pageable) {
        return shopRepository.findAllApprovedShops(pageable)
                .map(DTOConverter::convertToShopyDTO);
    }

    public Page<ShopResponseDto> getSuspendedShops(Pageable pageable) {
        return shopRepository.findAllSuspendedShops(pageable)
                .map(DTOConverter::convertToShopyDTO);
    }

    public ShopResponseDto getShopById(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not Found"));

        return DTOConverter.convertToShopyDTO(shop);
    }

    public void deleteShop(UUID id) {
        Optional<Shop> findShop = shopRepository.findById(id);
        if (findShop.isEmpty()) {
            throw new NotFoundException("Shop Not Found");
        }
        shopRepository.deleteById(id);
    }

    public Page<Shop> search(String name, Pageable pageable) {
        return shopRepository.findByName(name, pageable);
    }

    public void updateRole(UUID id, Role role) {
        User user = findByIdOrThrow(id, "User");
        user.setRole(role);

        repository.save(user);
    }

    public void approveShop(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));
        shop.setVerified(true);
        shopRepository.save(shop);
    }

    public void suspendShop(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        shop.setVerified(false);
        shopRepository.save(shop);
    }

    public Page<OfferResponseDTO> getAllOffers(Pageable pageable) {
        return offersRepository.findAll(pageable).map(DTOConverter::convertToOfferResponseDTO);
    }

    public void deleteOffer(UUID offerId) {
        Optional<Offer> offer = offersRepository.findById(offerId);
        if (offer.isEmpty()) {
            throw new NotFoundException("Offer Not Found");
        }
        offersRepository.deleteById(offerId);
    }

    public ResponseUsersDto convertDto(User user) {
        return new ResponseUsersDto(
                user.getId(),
                user.getFirst_name(),
                user.getFirst_name(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActivate());
    }
}
