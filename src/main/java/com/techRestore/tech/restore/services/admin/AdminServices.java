package com.techRestore.tech.restore.services.admin;

import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.user.ResponseUsersDto;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.model.enums.Role;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminServices {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    public List<ResponseUsersDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(this::convertDto).toList();
    }

    public ResponseUsersDto getUserDetailsById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user not found"));

        return convertDto(user);
    }

    public void suspendUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user not found"));
        user.setActivate(false);

        userRepository.save(user);
    }

    public void approveUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user not found"));
        user.setActivate(true);

        userRepository.save(user);
    }

    public List<ShopResponseDto> getShops() {
        return shopRepository.findAll().stream()
                .map(this::toShopDto)
                .toList();
    }

    public List<ShopResponseDto> getApprovedShops() {
        return shopRepository.findAllApprovedShops().stream()
                .map(this::toShopDto)
                .toList();
    }

    public List<ShopResponseDto> getSuspendedShops() {
        return shopRepository.findAllSuspendedShops().stream()
                .map(this::toShopDto)
                .toList();
    }

    public ShopResponseDto getShopById(UUID shopId){
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not Found"));

        return toShopDto(shop);
    }

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

    public void updateRole(UUID id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user not found"));
        user.setRole(role);

        userRepository.save(user);
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

    public ResponseUsersDto convertDto(User user) {
        return new ResponseUsersDto(
                user.getFirst_name(),
                user.getFirst_name(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }
}
