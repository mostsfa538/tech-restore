package com.techRestore.tech.restore.shop.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        this.notificationService = notificationService;
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

    @Transactional
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
        if (repairRequest.getStatus() == RepairStatus.SUBMITTED
                || repairRequest.getStatus() == RepairStatus.QUOTE_REJECTED
                || repairRequest.getStatus() == RepairStatus.QUOTE_SENT
                || repairRequest.getPrice() == null) {
            repairRequest.setPrice(repairPriceUpdateDto.price());
            repairRequest.setStatus(RepairStatus.QUOTE_SENT);
            repository.save(repairRequest);
        } else {
            throw new IllegalStateException("Repair request is not available for price update");
        }
        notificationService.sendToUser(repairRequest.getUserId(),
                "Price for repair request " + id + " has been updated to " + repairPriceUpdateDto.price());
    }

    public void setStatus(UUID id, RepairStatusDto repairStatusDto) {
        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        RepairStatus current = repairRequest.getStatus();
        RepairStatus next = repairStatusDto.status();

        if (!current.canTransitionTo(next)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + current + " → " + next);
        }

        repairRequest.setStatus(next);
        repository.save(repairRequest);

        handleNotifications(repairRequest, next);
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

    private void handleNotifications(RepairRequest request, RepairStatus newStatus) {
        if (newStatus == RepairStatus.REPAIR_COMPLETED) {
            notificationService.sendToUser(request.getUserId(),
                    "Repair request " + request.getId() + " is now REPAIR_COMPLETED");

            if (request.getDeliveryAddress() != null) {
                notificationService.sendToAllDelivery(
                        "Repair request " + request.getId() + " is ready for delivery");
                notificationService.sendToAssigners(
                        "Repair request " + request.getId() + " is ready to be assigned");
            }
        }
    }

}