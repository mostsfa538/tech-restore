package com.techRestore.tech.restore.user.service;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Payment;
import com.techRestore.tech.restore.common.model.entities.RepairRequest;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentType;
import com.techRestore.tech.restore.common.repository.AddressRepository;
import com.techRestore.tech.restore.common.repository.PaymentRepository;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.services.notification.NotificationService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestCreateDto;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestUpdateDto;
import com.techRestore.tech.restore.user.dto.repair.RepairStatusDto;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class RepairRequestService extends BaseService<RepairRequest, UUID> {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    public RepairRequestService(RepairRequestRepository repairRequestRepository, PaymentRepository paymentRepository,
            UserRepository userRepository,
            ShopRepository shopRepository,
            AddressRepository addressRepository,
            NotificationService notificationService,
            AuthUtil authUtil) {
        super(repairRequestRepository);
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.addressRepository = addressRepository;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
        this.authUtil = authUtil;
    }

    /**
     * Get current authenticated user ID
     */
    private UUID getCurrentUserId() {
        return authUtil.getCurrentUser().getId();
    }

    public Page<RepairRequestDto> getAllRepairRequestByUserId(Pageable pageable) {
        UUID userId = getCurrentUserId();
        Page<RepairRequest> repairRequests = ((RepairRequestRepository) repository)
                .getAllRepairRequestByUserId(userId, pageable);

        return repairRequests.map(rr -> {
            Shop shop = shopRepository.findById(rr.getShopId()).orElse(null);
            return DTOConverter.convertToRepairRequestDTO(rr, shop);
        });
    }

    public Page<RepairRequestDto> getAllRepairRequest(Pageable pageable) {
        return repository.findAll(pageable)
                .map(rr -> {
                    Shop shop = shopRepository.findById(rr.getShopId()).orElse(null);
                    return DTOConverter.convertToRepairRequestDTO(rr, shop);
                });
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setShop(shop);
        payment.setRepairRequestId(savedRepairRequest.getId());
        payment.setPaymentMethod(requestCreateDto.paymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentType(PaymentType.REPAIR_PAYMENT);
        payment.setAmount(BigDecimal.ZERO);
        payment.setPaymentReference(UUID.randomUUID().toString());

        paymentRepository.save(payment);

        notificationService.sendToShop(shopId, "New repair request received: Request ID " + savedRepairRequest.getId());

        return DTOConverter.convertToRepairRequestDTO(savedRepairRequest, shop);
    }

    public RepairRequestDto getRepairRequestById(UUID id) {
        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        Shop shop = shopRepository.findById(repairRequest.getShopId()).orElse(null);
        return DTOConverter.convertToRepairRequestDTO(repairRequest, shop);
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
        notificationService.sendToShop(shopId, "Repair request updated: Request ID " + requestId);
        Shop shop = shopRepository.findById(repairRequest.getShopId()).orElse(null);
        return DTOConverter.convertToRepairRequestDTO(updatedRepairRequest, shop);
    }

    @PreAuthorize("hasRole('GUEST')")
    public void deleteRepairRequest(UUID id) {
        findByIdOrThrow(id, "Repair request");
        repository.deleteById(id);
        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        UUID shopId = repairRequest.getShopId();
        notificationService.sendToShop(shopId, "Repair request deleted: Request ID " + id);
    }

    @PreAuthorize("hasRole('USER')")
    public void setStatus(UUID id, RepairStatusDto repairStatusDto) {
        if (repairStatusDto.status() == null) {
            throw new IllegalArgumentException("Repair status cannot be null");
        }

        RepairRequest repairRequest = findByIdOrThrow(id, "Repair request");
        repairRequest.setStatus(repairStatusDto.status());
        repository.save(repairRequest);
        notificationService.sendToShop(repairRequest.getShopId(),
                "Repair request status updated: Request ID " + id + " to " + repairStatusDto.status());
    }
}