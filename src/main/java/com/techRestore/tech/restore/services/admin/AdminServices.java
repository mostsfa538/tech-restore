package com.techRestore.tech.restore.services.admin;

import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.user.ResponseUsersDto;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.model.enums.Role;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.repository.UserRepository;
import com.techRestore.tech.restore.services.BaseService;
import com.techRestore.tech.restore.utils.DTOConverter;

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

    public ResponseUsersDto convertDto(User user) {
        return new ResponseUsersDto(
                user.getFirst_name(),
                user.getFirst_name(),
                user.getEmail(),
                user.getPhone(),
                user.getRole());
    }
}
