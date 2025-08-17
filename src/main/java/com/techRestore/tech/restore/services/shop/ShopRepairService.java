package com.techRestore.tech.restore.services.shop;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

@Service
public class ShopRepairService {
    @Autowired
    private RepairRequestRepository repairRequestRepository;
    
    @Autowired
    private ShopRepository shopRepository;

    private UUID getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String email = authentication.getName();
      Shop shop = shopRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Shop not found"));

      if (shop == null) {
          throw new RuntimeException("User not found with email: " + email);
      }
      return shop.getId();
    }

    public List<RepairRequestDto> getAllRepairRequest() {
        UUID shopId = getCurrentShopId();
        return repairRequestRepository.findAllByShopId(shopId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    public void setPrice(UUID id, RepairPriceUpdateDto repairPriceUpdateDto) {
        RepairRequest repairRequest = repairRequestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Repair request not found with id: " + id));

        repairRequest.setPrice(repairPriceUpdateDto.price());
        repairRequestRepository.save(repairRequest);         
    }


    public void setStatus(UUID id, RepairStatusDto repairStatusDto) {
        RepairRequest repairRequest = repairRequestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Repair request not found with id: " + id));

        repairRequest.setStatus(repairStatusDto.status());
        repairRequestRepository.save(repairRequest);  
    }

    public RepairRequestDto getRepairRequestById(UUID id) {
        RepairRequest repairRequest = repairRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Repair request not found with id: " + id));
        return convertToDto(repairRequest);
    }


    public List<RepairRequestDto> getRepairsByStatus(RepairStatus status) {
        List<RepairRequest> repairRequests = repairRequestRepository.findByStatus(status);
        return repairRequests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private RepairRequestDto convertToDto(RepairRequest repairRequest) {
        return new RepairRequestDto(
                repairRequest.getId(),
                null,
                repairRequest.getUserId(),
                repairRequest.getShopId(),
                repairRequest.getDeliveryAddress(),
                repairRequest.getPaymentId(),
                repairRequest.getDescription(),
                repairRequest.getDeliveryMethod().name(),
                repairRequest.getCategoryId(),
                repairRequest.getPaymentMethod().name(),
                repairRequest.isConfirmed(),
                repairRequest.getStatus()
        );
    }

}
