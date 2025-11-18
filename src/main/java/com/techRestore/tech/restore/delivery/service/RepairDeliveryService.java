package com.techRestore.tech.restore.delivery.service;

import com.techRestore.tech.restore.common.exception.AccountNotApprovedException;
import com.techRestore.tech.restore.common.exception.ActivationException;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Address;
import com.techRestore.tech.restore.common.model.entities.Delivery;
import com.techRestore.tech.restore.common.model.entities.Payment;
import com.techRestore.tech.restore.common.model.entities.RepairRequest;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.ShopAddress;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.ApprovalStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.model.enums.RepairStatus;
import com.techRestore.tech.restore.common.repository.PaymentRepository;
import com.techRestore.tech.restore.common.services.notification.NotificationService;
import com.techRestore.tech.restore.delivery.dto.DeliveryProfileUpdateDto;
import com.techRestore.tech.restore.delivery.dto.RepairDeliveryDto;
import com.techRestore.tech.restore.delivery.dto.RepairDeliveryStateUpdate;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepairDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    private UUID getCurrentDeliveryId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Delivery delivery = deliveryRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Delivery not found with email: " + email));
        
        if (delivery.getStatus() != ApprovalStatus.APPROVED) {
            throw new AccountNotApprovedException("Your account is not approved. Please wait for admin approval.");
        }
        if (!delivery.isActivate()) {
            throw new ActivationException("Account is not activated. Please check your email for activation instructions");
        }
        return delivery.getId();
    }

    public Delivery getProfile() {
        UUID deliveryId = getCurrentDeliveryId();
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
    }

    @Transactional
    public void updateProfile(DeliveryProfileUpdateDto updateDto) {
        UUID deliveryId = getCurrentDeliveryId();
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
        delivery.setName(updateDto.getName());
        delivery.setAddress(updateDto.getAddress());
        deliveryRepository.save(delivery);
    }

    @Transactional(readOnly = true)
    public Page<RepairDeliveryDto> getAvailableRepairRequests(Pageable pageable) {
        Page<RepairRequest> repairRequests = repairRequestRepository.findByStatusInAndDeliveryIdIsNull(
                List.of(RepairStatus.REPAIR_COMPLETED), pageable);
        return repairRequests.map(this::convertToRepairDeliveryDTO);
    }

    @Transactional(readOnly = true)
    public Page<RepairDeliveryDto> getMyRepairDeliveries(Pageable pageable) {
        UUID deliveryId = getCurrentDeliveryId();
        Page<RepairRequest> repairRequests = repairRequestRepository.findByDeliveryId(deliveryId, pageable);
        return repairRequests.map(this::convertToRepairDeliveryDTO);
    }

    @Transactional
    public void acceptRepairDelivery(UUID repairRequestId) {
        UUID deliveryId = getCurrentDeliveryId();
        RepairRequest repairRequest = repairRequestRepository.findById(repairRequestId)
                .orElseThrow(() -> new NotFoundException("Repair request not found"));
        
        if (repairRequest.getStatus() != RepairStatus.REPAIR_COMPLETED || repairRequest.getDeliveryId() != null) {
            throw new IllegalStateException("Repair request is not available for delivery");
        }
        repairRequest.setDeliveryId(deliveryId);
        repairRequest.setStatus(RepairStatus.DEVICE_DELIVERED);
        repairRequestRepository.save(repairRequest);
        notificationService.sendToUser(repairRequest.getUserId(),
                "Your repair request " + repairRequestId + " is being Shipped");
    }

    @Transactional
    public void rejectRepairDelivery(UUID repairRequestId) {
        getCurrentDeliveryId();
        RepairRequest repairRequest = repairRequestRepository.findById(repairRequestId)
                .orElseThrow(() -> new NotFoundException("Repair request not found"));
        
        if (repairRequest.getStatus() != RepairStatus.REPAIR_COMPLETED || repairRequest.getDeliveryId() != null) {
            throw new IllegalStateException("Repair request is not available for delivery");
        }
        notificationService.sendToShop(repairRequest.getShopId(),
                "Delivery rejected for repair request " + repairRequestId);
    }

    @Transactional
    public void updateRepairRequestStatus(UUID repairRequestId, RepairDeliveryStateUpdate stateUpdate) {
        UUID deliveryId = getCurrentDeliveryId();
        RepairRequest repairRequest = repairRequestRepository.findById(repairRequestId)
                .orElseThrow(() -> new NotFoundException("Repair request not found"));
        if (!deliveryId.equals(repairRequest.getDeliveryId())) {
            throw new IllegalStateException("You are not assigned to this repair request");
        }
        if (stateUpdate.getStatus() != RepairStatus.DEVICE_COLLECTED && stateUpdate.getStatus() != RepairStatus.DEVICE_DELIVERED) {
            throw new IllegalStateException("Invalid status update for repair delivery");
        }
        repairRequest.setStatus(stateUpdate.getStatus());
        repairRequestRepository.save(repairRequest);

        if (stateUpdate.getStatus() == RepairStatus.DEVICE_DELIVERED) {
            Payment payment = paymentRepository.findByRepairRequestId(repairRequestId)
                    .orElseThrow(() -> new NotFoundException("Payment not found for repair request: " + repairRequestId));

            if (payment.getPaymentMethod() == PaymentMethod.CASH &&
                    payment.getPaymentStatus() == PaymentStatus.PENDING) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
            notificationService.sendToUser(repairRequest.getUserId(),
                    "Your repair request " + repairRequestId + " has been delivered");
            notificationService.sendToShop(repairRequest.getShopId(),
                    "Repair request " + repairRequestId + " has been delivered to the customer");
        } else if (stateUpdate.getStatus() == RepairStatus.DEVICE_COLLECTED) {
            notificationService.sendToShop(repairRequest.getShopId(),
                    "Repair request " + repairRequestId + " has been collected and is en route to your shop");
        }
    }

    private RepairDeliveryDto convertToRepairDeliveryDTO(RepairRequest repairRequest) {
        RepairDeliveryDto dto = new RepairDeliveryDto();
        dto.setId(repairRequest.getId());
        dto.setUserId(repairRequest.getUserId());
        dto.setFirstName(repairRequest.getUser().getFirst_name());
        dto.setLastName(repairRequest.getUser().getLast_name());    
        dto.setPhone(repairRequest.getUser().getPhone());
        dto.setShopId(repairRequest.getShopId());
        dto.setDeliveryId(repairRequest.getDeliveryId());
        dto.setStatus(repairRequest.getStatus());
        dto.setPrice(repairRequest.getPrice());
        dto.setCreatedAt(repairRequest.getCreatedAt());

        User user = userRepository.findById(repairRequest.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + repairRequest.getUserId()));
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            Address userAddress = user.getAddresses().get(0); // Select first address
            RepairDeliveryDto.AddressDto userAddressDto = new RepairDeliveryDto.AddressDto();
            userAddressDto.setId(userAddress.getId());
            userAddressDto.setStreet(userAddress.getStreet());
            userAddressDto.setCity(userAddress.getCity());
            userAddressDto.setState(userAddress.getState());
            dto.setUserAddress(userAddressDto);
        }

        Shop shop = shopRepository.findById(repairRequest.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found with ID: " + repairRequest.getShopId()));
        if (shop.getAddresses() != null && !shop.getAddresses().isEmpty()) {
            ShopAddress shopAddress = shop.getAddresses().get(0);
            RepairDeliveryDto.AddressDto shopAddressDto = new RepairDeliveryDto.AddressDto();
            shopAddressDto.setId(shopAddress.getId());
            shopAddressDto.setStreet(shopAddress.getStreet());
            shopAddressDto.setCity(shopAddress.getCity());
            shopAddressDto.setState(shopAddress.getState());
            dto.setShopAddress(shopAddressDto);
        }
        return dto;
    }
}