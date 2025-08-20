package com.techRestore.tech.restore.services.shop;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.dto.repair.RepairPriceUpdateDto;
import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.dto.repair.RepairStatusDto;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.enums.RepairStatus;
import com.techRestore.tech.restore.repository.RepairRequestRepository;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.services.BaseService;
import com.techRestore.tech.restore.utils.DTOConverter;

@Service
public class ShopRepairService extends BaseService<RepairRequest, UUID> {
    
    private final ShopRepository shopRepository;

    public ShopRepairService(RepairRequestRepository repairRequestRepository, ShopRepository shopRepository) {
        super(repairRequestRepository);
        this.shopRepository = shopRepository;
    }

    /**
     * Get current authenticated shop ID
     */
    private UUID getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Shop shop = shopRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Shop not found with email: " + email));
        return shop.getId();
    }

    public Page<RepairRequestDto> getAllRepairRequest(Pageable pageable) {
        UUID shopId = getCurrentShopId();
        return ((RepairRequestRepository) repository).findAllByShopId(shopId, pageable)
                .map(DTOConverter::convertToRepairRequestDTO);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    public void setPrice(UUID id, RepairPriceUpdateDto repairPriceUpdateDto) {
        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        repairRequest.setPrice(repairPriceUpdateDto.price());
        repository.save(repairRequest);
    }

    public void setStatus(UUID id, RepairStatusDto repairStatusDto) {
        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        repairRequest.setStatus(repairStatusDto.status());
        repository.save(repairRequest);
    }

    public RepairRequestDto getRepairRequestById(UUID id) {
        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        return DTOConverter.convertToRepairRequestDTO(repairRequest);
    }

    public Page<RepairRequestDto> getRepairsByStatus(RepairStatus status, Pageable pageable) {
        Page<RepairRequest> repairRequests = ((RepairRequestRepository) repository).findByStatus(status, pageable);
        return repairRequests.map(DTOConverter::convertToRepairRequestDTO);
    }
}