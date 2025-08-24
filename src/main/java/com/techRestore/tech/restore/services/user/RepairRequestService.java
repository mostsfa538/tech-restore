package com.techRestore.tech.restore.services.user;

import com.techRestore.tech.restore.dto.repair.RepairRequestCreateDto;
import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.dto.repair.RepairRequestUpdateDto;
import com.techRestore.tech.restore.dto.repair.RepairStatusDto;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.AddressRepository;
import com.techRestore.tech.restore.repository.RepairRequestRepository;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.repository.UserRepository;
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
public class RepairRequestService extends BaseService<RepairRequest, UUID> {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final AddressRepository addressRepository;

    public RepairRequestService(RepairRequestRepository repairRequestRepository,
            UserRepository userRepository,
            ShopRepository shopRepository,
            AddressRepository addressRepository) {
        super(repairRequestRepository);
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * Get current authenticated user ID
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new NotFoundException("User not found with email: " + email);
        }

        return user.getId();
    }

    public Page<RepairRequestDto> getAllRepairRequestByUserId(Pageable pageable) {
        UUID userId = getCurrentUserId();
        Page<RepairRequest> repairRequests = ((RepairRequestRepository) repository)
                .getAllRepairRequestByUserId(userId, pageable);

        return repairRequests.map(DTOConverter::convertToRepairRequestDTO);
    }

    public Page<RepairRequestDto> getAllRepairRequest(Pageable pageable) {
        return repository.findAll(pageable)
                .map(DTOConverter::convertToRepairRequestDTO);
    }

    public RepairRequestDto createRepairRequest(UUID shopId, RepairRequestCreateDto requestCreateDto) {
        UUID userId = getCurrentUserId();

        findByIdOrThrow(shopRepository, shopId, "Shop");

        findByIdOrThrow(addressRepository, requestCreateDto.deliveryAddress(), "Address");

        RepairRequest repairRequest = new RepairRequest();
        repairRequest.setUserId(userId);
        repairRequest.setShopId(shopId);
        repairRequest.setDeliveryAddress(requestCreateDto.deliveryAddress());
        repairRequest.setDescription(requestCreateDto.description());
        repairRequest.setDeliveryMethod(requestCreateDto.deliveryMethod());
        repairRequest.setCategoryId(requestCreateDto.deviceCategory());
        repairRequest.setPaymentMethod(requestCreateDto.paymentMethod());
        repairRequest.setConfirmed(false);

        RepairRequest savedRepairRequest = repository.save(repairRequest);
        return DTOConverter.convertToRepairRequestDTO(savedRepairRequest);
    }

    public RepairRequestDto getRepairRequestById(UUID id) {
        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        return DTOConverter.convertToRepairRequestDTO(repairRequest);
    }

    public RepairRequestDto updateRepairRequest(UUID shopId, UUID requestId, RepairRequestUpdateDto requestUpdateDto) {
        UUID userId = getCurrentUserId();

        RepairRequest repairRequest = findByIdOrThrow(requestId, "Repair request");

        findByIdOrThrow(shopRepository, shopId, "Shop");

        if (requestUpdateDto.deliveryAddressId() != null) {
            findByIdOrThrow(addressRepository, requestUpdateDto.deliveryAddressId(), "Address");
            repairRequest.setDeliveryAddress(requestUpdateDto.deliveryAddressId());
        }

        repairRequest.setUserId(userId);
        repairRequest.setShopId(shopId);

        if (requestUpdateDto.description() != null) {
            repairRequest.setDescription(requestUpdateDto.description());
        }
        if (requestUpdateDto.deliveryMethod() != null) {
            repairRequest.setDeliveryMethod(requestUpdateDto.deliveryMethod());
        }
        if (requestUpdateDto.paymentMethod() != null) {
            repairRequest.setPaymentMethod(requestUpdateDto.paymentMethod());
        }
        if (requestUpdateDto.categoryId() != null) {
            repairRequest.setCategoryId(requestUpdateDto.categoryId());
        }

        RepairRequest updatedRepairRequest = repository.save(repairRequest);
        return DTOConverter.convertToRepairRequestDTO(updatedRepairRequest);
    }

    @PreAuthorize("hasRole('GUEST')")
    public void deleteRepairRequest(UUID id) {
        findByIdOrThrow(id, "Repair request");
        repository.deleteById(id);
    }

    @PreAuthorize("hasRole('USER')")
    public void setStatus(UUID id, RepairStatusDto repairStatusDto) {
        if (repairStatusDto.status() == null) {
            throw new IllegalArgumentException("Repair status cannot be null");
        }

        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        repairRequest.setStatus(repairStatusDto.status());
        repository.save(repairRequest);
    }
}