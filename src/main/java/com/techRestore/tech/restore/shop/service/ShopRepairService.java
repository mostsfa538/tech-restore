package com.techRestore.tech.restore.shop.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.RepairRequest;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.enums.RepairStatus;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.services.notification.NotificationService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.dto.repair.RepairPriceUpdateDto;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.user.dto.repair.RepairStatusDto;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;

@Service
public class ShopRepairService extends BaseService<RepairRequest, UUID> {

    private final ShopRepository shopRepository;
    private final NotificationService notificationService;

    public ShopRepairService(RepairRequestRepository repairRequestRepository, ShopRepository shopRepository,
                             NotificationService notificationService) {
        super(repairRequestRepository);
        this.shopRepository = shopRepository;
        this.notificationService=notificationService;
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
        Shop shop = shopRepository.findById(shopId).orElse(null);
        return ((RepairRequestRepository) repository)
                .findAllByShopId(shopId, pageable)
                .map(repairRequest -> DTOConverter.convertToRepairRequestDTO(repairRequest, shop));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    public void setPrice(UUID id, RepairPriceUpdateDto repairPriceUpdateDto) {
        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        repairRequest.setPrice(repairPriceUpdateDto.price());
        repository.save(repairRequest);
    }

    public void setStatus(UUID id, RepairStatusDto repairStatusDto) {
        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        RepairStatus newStatus = repairStatusDto.status();
        repairRequest.setStatus(newStatus);
        repository.save(repairRequest);

        if (newStatus == RepairStatus.REPAIR_COMPLETED && repairRequest.getDeliveryAddress() != null) {
            notificationService.sendToAllDelivery(
                    "Repair request " + id + " is now REPAIR_COMPLETED and ready for delivery."
            );
        }
    }

    public RepairRequestDto getRepairRequestById(UUID id) {
        UUID shopId = getCurrentShopId();
        RepairRequest repairRequest = ((RepairRequestRepository) repository)
                .findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new NotFoundException("Repair request not found for this shop"));
        Shop shop = shopRepository.findById(shopId).orElse(null);
        return DTOConverter.convertToRepairRequestDTO(repairRequest, shop);
    }

    public Page<RepairRequestDto> getRepairsByStatus(RepairStatus status, Pageable pageable) {
        UUID shopId = getCurrentShopId();
        Shop shop = shopRepository.findById(shopId).orElse(null);
        Page<RepairRequest> repairRequests = ((RepairRequestRepository) repository)
                .findByShopIdAndStatus(shopId, status, pageable);
        return repairRequests.map(repairRequest -> DTOConverter.convertToRepairRequestDTO(repairRequest, shop));
    }

}