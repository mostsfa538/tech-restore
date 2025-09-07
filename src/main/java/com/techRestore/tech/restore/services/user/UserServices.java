package com.techRestore.tech.restore.services.user;

import com.techRestore.tech.restore.dto.common.address.AddressResponse;
import com.techRestore.tech.restore.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.dto.user.UserProfileDTO;
import com.techRestore.tech.restore.dto.user.UserProfileUpdateDTO;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Address;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.*;
import com.techRestore.tech.restore.utils.DTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServices {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepairRequestRepository repairRequestRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Page<ProductResponseDTO> getProductsByCategory(UUID shopId, UUID categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not Found"));

        return productRepository.findProductByCategoryId(shopId, categoryId, pageable)
                .map(DTOConverter::convertToProductDTO);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null || !user.isActivate()) {
            throw new NotFoundException("User account is deactivated or not found");
        }
        return user;
    }

    private AddressResponse convertToAddressDTO(Address address) {
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

    public UserProfileDTO getCurrentUserProfile() {
        UUID currentUserId = getCurrentUser().getId();

        User user = userRepository.findByIdWithAddresses(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        List<AddressResponse> addressDTOs = user.getAddresses().stream()
                .map(this::convertToAddressDTO)
                .collect(Collectors.toList());
        profileDTO.setAddresses(addressDTOs);

        return profileDTO;
    }

    @Transactional
    public UserProfileDTO updateUserProfile(UserProfileUpdateDTO updateDTO) {
        User user = getCurrentUser();

        if (updateDTO.getFirst_name() != null) {
            user.setFirst_name(updateDTO.getFirst_name());
        }
        if (updateDTO.getLast_name() != null) {
            user.setLast_name(updateDTO.getLast_name());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        userRepository.save(user);
        return getCurrentUserProfile();
    }

    @Transactional
    public void deleteUserAccount() {
        User user = getCurrentUser();
        user.setActivate(false);
        userRepository.save(user);
    }

    public Page<RepairRequestDto> getUserRepairRequests(Pageable pageable) {
        User user = getCurrentUser();
        return repairRequestRepository.getAllRepairRequestByUserId(user.getId(), pageable)
                .map(DTOConverter::convertToRepairRequestDto);
    }

    public Page<OrderResponseDTO> getUserOrders(Pageable pageable) {
        User user = getCurrentUser();
        return orderRepository.findByUserId(user.getId(), pageable)
                .map(DTOConverter::convertToOrderResponseDTO);
    }

}