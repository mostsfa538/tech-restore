package com.techRestore.tech.restore.user.service;

import com.techRestore.tech.restore.common.exception.AccessDeniedException;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Payment;
import com.techRestore.tech.restore.common.model.entities.RepairRequest;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentType;
import com.techRestore.tech.restore.common.model.enums.RepairStatus;
import com.techRestore.tech.restore.common.repository.AddressRepository;
import com.techRestore.tech.restore.common.repository.PaymentRepository;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.services.notification.NotificationService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.dto.repair.*;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RepairRequestService extends BaseService<RepairRequest, UUID> {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final RepairRequestRepository repairRequestRepository;
    private final AuthUtil authUtil;

    public RepairRequestService(
            RepairRequestRepository repairRequestRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            ShopRepository shopRepository,
            AddressRepository addressRepository,
            NotificationService notificationService,
            AuthUtil authUtil) {
        super(repairRequestRepository);
        this.repairRequestRepository = repairRequestRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.addressRepository = addressRepository;
        this.notificationService = notificationService;
        this.authUtil = authUtil;
    }

    private UUID getCurrentUserId() {
        return authUtil.getCurrentUser().getId();
    }

    private Shop getShopOrThrow(UUID shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateAddressExists(UUID addressId) {
        findByIdOrThrow(addressRepository, addressId, "Address");
    }

    private Payment createRepairPayment(User user, Shop shop, RepairRequest req) {
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setShop(shop);
        payment.setRepairRequestId(req.getId());
        payment.setPaymentMethod(req.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentType(PaymentType.REPAIR_PAYMENT);
        payment.setAmount(req.getPrice());
        payment.setPaymentReference(UUID.randomUUID().toString());
        return paymentRepository.save(payment);
    }

    private void validateOwnership(RepairRequest req, UUID userId) {
        if (!req.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to modify this repair request");
        }
    }

    @Transactional(readOnly = true)
    public Page<RepairRequestDto> getAllRepairRequestByUserId(Pageable pageable) {
        UUID userId = getCurrentUserId();
        return repairRequestRepository.getAllRepairRequestByUserId(userId, pageable)
                .map(rr -> DTOConverter.convertToRepairRequestDTO(rr,
                        shopRepository.findById(rr.getShopId()).orElse(null)));
    }

    @Transactional(readOnly = true)
    public Page<RepairRequestDto> getAllRepairRequest(Pageable pageable) {
        return repository.findAll(pageable)
                .map(rr -> DTOConverter.convertToRepairRequestDTO(rr,
                        shopRepository.findById(rr.getShopId()).orElse(null)));
    }

    @Transactional(readOnly = true)
    public RepairRequestDto getRepairRequestById(UUID id) {
        RepairRequest req = findByIdOrThrow(id, "Repair request");
        Shop shop = shopRepository.findById(req.getShopId()).orElse(null);
        return DTOConverter.convertToRepairRequestDTO(req, shop);
    }

    @Transactional
    public RepairRequestDto createRepairRequest(UUID shopId, RepairRequestDtoRequest dto) {
        UUID userId = getCurrentUserId();

        Shop shop = getShopOrThrow(shopId);

        RepairRequest req = new RepairRequest();
        req.setUserId(userId);
        req.setShopId(shopId);
        req.setDescription(dto.description());
        req.setCategoryId(dto.deviceCategory());
        req.setConfirmed(false);
        req.setDeliveryAddress(null);
        req.setDeliveryAddressEntity(null);
        RepairRequest savedReq = repository.save(req);

        notificationService.sendToShop(shopId,
                "New repair request received: ID " + savedReq.getId());

        return DTOConverter.convertToRepairRequestDTO(savedReq, shop);
    }

    @Transactional
    public RepairRequestDto updateRepairRequest(UUID shopId, UUID requestId, RepairRequestDtoRequest dto) {
        UUID userId = getCurrentUserId();
        RepairRequest req = findByIdOrThrow(requestId, "Repair request");

        validateOwnership(req, userId);
        getShopOrThrow(shopId);

        if (dto.description() != null)
            req.setDescription(dto.description());
        if (dto.deviceCategory() != null)
            req.setCategoryId(dto.deviceCategory());

        RepairRequest updated = repository.save(req);
        notificationService.sendToShop(shopId, "Repair request updated: ID " + requestId);

        return DTOConverter.convertToRepairRequestDTO(updated, getShopOrThrow(shopId));
    }

    @PreAuthorize("hasRole('GUEST')")
    @Transactional
    public void deleteRepairRequest(UUID id) {
        RepairRequest req = findByIdOrThrow(id, "Repair request");
        UUID shopId = req.getShopId();
        repository.delete(req);
        notificationService.sendToShop(shopId, "Repair request deleted: ID " + id);
    }

    @PreAuthorize("hasRole('GUEST')")
    @Transactional
    public void setStatus(UUID id, RepairStatusDto dto) {
        if (dto.status() == null)
            throw new IllegalArgumentException("Repair status cannot be null");

        RepairRequest req = findByIdOrThrow(id, "Repair request");
        req.setStatus(dto.status());
        repository.save(req);

        notificationService.sendToShop(req.getShopId(),
                "Repair request status updated: ID " + id + " → " + dto.status());
    }

    @PreAuthorize("hasRole('GUEST')")
    @Transactional
    public RepairRequestDto confirmShopOffer(UUID repairId, UserRepairDetailsDto dto) {
        UUID userId = getCurrentUserId();
        RepairRequest req = findByIdOrThrow(repairId, "Repair request");

        validateOwnership(req, userId);
        validateAddressExists(dto.deliveryAddress());

        if (req.isConfirmed())
            throw new IllegalStateException("Offer already confirmed");
        if (req.getStatus() != RepairStatus.QUOTE_APPROVED)
            throw new IllegalStateException("Only approved quotes can be confirmed");

        req.setConfirmed(true);
        req.setDeliveryAddress(dto.deliveryAddress());
        req.setDeliveryMethod(dto.deliveryMethod());
        req.setPaymentMethod(dto.paymentMethod());
        req.setStatus(RepairStatus.QUOTE_APPROVED);

        User user = getUserOrThrow(userId);
        Shop shop = getShopOrThrow(req.getShopId());

        Payment payment = createRepairPayment(user, shop, req);
        req.setPaymentId(payment.getId());

        repository.save(req);

        notificationService.sendToShop(shop.getId(),
                "Customer confirmed offer for Request ID " + repairId +
                        " with price " + req.getPrice());

        return DTOConverter.convertToRepairRequestDTO(req, shop);
    }
    
}
