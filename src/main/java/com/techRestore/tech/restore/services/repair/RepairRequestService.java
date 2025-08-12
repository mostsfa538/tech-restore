package com.techRestore.tech.restore.services.repair;

import com.techRestore.tech.restore.dto.repair.RepairRequestCreateDto;
import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.dto.repair.RepairStatusDto;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.AddressRepository;
import com.techRestore.tech.restore.repository.RepairRequestRepository;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RepairRequestService {
    @Autowired
    private RepairRequestRepository repairRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private AddressRepository addressRepository;

    private UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null)
            throw new NotFoundException("User not fount");

        return user.getId();
    }

    public List<RepairRequest> getAllRepairRequest() {
        return repairRequestRepository.findAll();
    }

    public RepairRequestDto createRepairRequest(RepairRequestCreateDto requestCreateDto) {
        UUID userId = getUserId();

        shopRepository.findById(requestCreateDto.shopId())
                .orElseThrow(() -> new NotFoundException("Shop not found with id: " + requestCreateDto.shopId()));

        addressRepository.findById(requestCreateDto.deliveryAddress())
                .orElseThrow(() -> new NotFoundException("Address not found with id: " + requestCreateDto.deliveryAddress()));

        RepairRequest repairRequest = new RepairRequest();

        repairRequest.setUserId(userId);
        repairRequest.setShopId(requestCreateDto.shopId());
        repairRequest.setDeliveryAddress(requestCreateDto.deliveryAddress());
        repairRequest.setDescription(requestCreateDto.description());
        repairRequest.setDeliveryMethod(requestCreateDto.deliveryMethod());
        repairRequest.setCategoryId(requestCreateDto.deviceCategory());
        repairRequest.setPaymentMethod(requestCreateDto.paymentMethod());
        repairRequest.setConfirmed(false);

        RepairRequest savedRepairRequest = repairRequestRepository.save(repairRequest);

        return convertToDto(savedRepairRequest);
    }


    public RepairRequestDto getRepairRequestById(UUID id) {
        RepairRequest repairRequest = repairRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Repair request not found with id: " + id));

        return convertToDto(repairRequest);
    }

    public RepairRequestDto updateRepairRequest(UUID id, RepairRequestCreateDto requestCreateDto) {
        UUID userId = getUserId();

        RepairRequest repairRequest = repairRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Repair request not found with id: " + id));


        shopRepository.findById(requestCreateDto.shopId())
                .orElseThrow(() -> new NotFoundException("Shop not found with id: " + requestCreateDto.shopId()));

        addressRepository.findById(requestCreateDto.deliveryAddress())
                .orElseThrow(() -> new NotFoundException("Address not found with id: " + requestCreateDto.deliveryAddress()));

        repairRequest.setUserId(userId);
        repairRequest.setShopId(requestCreateDto.shopId());
        repairRequest.setDeliveryAddress(requestCreateDto.deliveryAddress());
        repairRequest.setDescription(requestCreateDto.description());
        repairRequest.setDeliveryMethod(requestCreateDto.deliveryMethod());
        repairRequest.setCategoryId(requestCreateDto.deviceCategory());
        repairRequest.setPaymentMethod(requestCreateDto.paymentMethod());

        RepairRequest updatedRepairRequest = repairRequestRepository.save(repairRequest);

        return convertToDto(updatedRepairRequest);
    }


    @PreAuthorize("hasRole('GUEST')")
    public void deleteRepairRequest(UUID id) {
        RepairRequest repairRequest = repairRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Repair request not found with id: " + id));

        repairRequestRepository.delete(repairRequest);
    }

    @PreAuthorize("hasRole('USER')")
    public void setStatus(UUID id, RepairStatusDto repairStatusDto) {
        RepairRequest repairRequest = repairRequestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Repair request not found with id: " + id));

            if (repairStatusDto.status() == null) {
                throw new IllegalArgumentException("Repair status cannot be null");
            }
            
        repairRequest.setStatus(repairStatusDto.status());
        repairRequestRepository.save(repairRequest);  
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