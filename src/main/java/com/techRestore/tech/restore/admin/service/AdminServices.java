package com.techRestore.tech.restore.admin.service;

import com.techRestore.tech.restore.assigners.dto.AssignerResponseDto;
import com.techRestore.tech.restore.assigners.repository.AssignerRepository;
import com.techRestore.tech.restore.admin.dto.AdminStatsDto;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Assigner;
import com.techRestore.tech.restore.common.model.entities.Delivery;
import com.techRestore.tech.restore.common.model.entities.Offer;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.ApprovalStatus;
import com.techRestore.tech.restore.common.model.enums.OrderStatus;
import com.techRestore.tech.restore.common.model.enums.RepairStatus;
import com.techRestore.tech.restore.common.model.enums.Role;
import com.techRestore.tech.restore.common.repository.AssignmentLogRepository;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.services.notification.NotificationService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.delivery.dto.DeliveryResponseDto;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;
import com.techRestore.tech.restore.shop.dto.offers.OfferResponseDTO;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.shop.repository.OffersRepository;
import com.techRestore.tech.restore.shop.repository.ProductRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.dto.user.ResponseUsersDto;
import com.techRestore.tech.restore.user.repository.OrderRepository;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminServices extends BaseService<User, UUID> {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private OffersRepository offersRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AssignerRepository assignerRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RepairRequestRepository repairRequestRepository;

    @Autowired
    private AssignmentLogRepository assignmentLogRepository;

    @Autowired
    private NotificationService notificationService;

    public AdminServices(UserRepository userRepository) {
        super(userRepository);
    }

    public Page<ResponseUsersDto> getAllUsers(Pageable pageable) {
        return repository.findAll(pageable).map(this::convertDto);
    }

    public ResponseUsersDto getUserDetailsById(UUID id) {
        User user = findByIdOrThrow(id, "User");
        return convertDto(user);
    }

    public void suspendUser(UUID id) {
        User user = findByIdOrThrow(id, "User");
        user.setActivate(false);
        repository.save(user);
    }

    public void approveUser(UUID id) {
        User user = findByIdOrThrow(id, "User");
        user.setActivate(true);
        repository.save(user);
    }

    @Transactional
    public Page<ShopResponseDto> getShops(Pageable pageable) {
        return shopRepository.findAll(pageable)
                .map(DTOConverter::convertToShopDTO);
    }

    public Page<ShopResponseDto> getApprovedShops(Pageable pageable) {
        return shopRepository.findAllApprovedShops(pageable)
                .map(DTOConverter::convertToShopDTO);
    }

    public Page<ShopResponseDto> getSuspendedShops(Pageable pageable) {
        return shopRepository.findAllSuspendedShops(pageable)
                .map(DTOConverter::convertToShopDTO);
    }

    public ShopResponseDto getShopById(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not Found"));

        return DTOConverter.convertToShopDTO(shop);
    }

    public void deleteShop(UUID id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shop Not Found"));

        Shop unknownShop = shopRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .orElseThrow(() -> new IllegalStateException("Unknown shop must exist"));

        productRepository.updateShopToUnknown(id, unknownShop.getId());

        shopRepository.delete(shop);
    }

    public Page<Shop> search(String name, Pageable pageable) {
        return shopRepository.findByName(name, pageable);
    }

    public void updateRole(UUID id, Role role) {
        User user = findByIdOrThrow(id, "User");
        user.setRole(role);

        repository.save(user);
    }

    public void approveShop(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));
        shop.setVerified(true);
        shopRepository.save(shop);
    }

    public void suspendShop(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        shop.setVerified(false);
        shopRepository.save(shop);
    }

    public Page<OfferResponseDTO> getAllOffers(Pageable pageable) {
        return offersRepository.findAll(pageable).map(DTOConverter::convertToOfferResponseDTO);
    }

    public void deleteOffer(UUID offerId) {
        Optional<Offer> offer = offersRepository.findById(offerId);
        if (offer.isEmpty()) {
            throw new NotFoundException("Offer Not Found");
        }
        offersRepository.deleteById(offerId);
    }

    public Page<DeliveryResponseDto> getAllDeliveries(Pageable pageable) {
        Page<Delivery> deliveries = deliveryRepository.findAll(pageable);
        return deliveries.map(this::convertToDeliveryResponseDto);
    }

    public DeliveryResponseDto getDeliveryById(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found with ID: " + deliveryId));
        return convertToDeliveryResponseDto(delivery);
    }

    public Page<DeliveryResponseDto> getPendingDeliveries(Pageable pageable) {
        Page<Delivery> deliveries = deliveryRepository.findByStatus(ApprovalStatus.PENDING, pageable);
        return deliveries.map(this::convertToDeliveryResponseDto);
    }

    public Page<DeliveryResponseDto> getApprovedDeliveries(Pageable pageable) {
        Page<Delivery> deliveries = deliveryRepository.findByStatus(ApprovalStatus.APPROVED, pageable);
        return deliveries.map(this::convertToDeliveryResponseDto);
    }

    public Page<DeliveryResponseDto> getSuspendedDeliveries(Pageable pageable) {
        Page<Delivery> deliveries = deliveryRepository.findByStatus(ApprovalStatus.SUSPENDED, pageable);
        return deliveries.map(this::convertToDeliveryResponseDto);
    }

    @Transactional
    public void approveDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found with ID: " + deliveryId));

        delivery.setStatus(ApprovalStatus.APPROVED);
        delivery.setActivate(true);
        delivery.setVerified(true);
        deliveryRepository.save(delivery);

        notificationService.sendToDelivery(deliveryId,
                "Congratulations! Your delivery account has been approved by admin. You can now start accepting deliveries.");
    }

    @Transactional
    public void suspendDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found with ID: " + deliveryId));

        delivery.setStatus(ApprovalStatus.SUSPENDED);
        delivery.setActivate(false);
        deliveryRepository.save(delivery);

        notificationService.sendToDelivery(deliveryId,
                "Your delivery account has been suspended by admin. Please contact support for more information.");
    }

    @Transactional
    public void deleteDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found with ID: " + deliveryId));

        long activeAssignments = orderRepository.countByDeliveryIdAndStatusIn(
                deliveryId, List.of(OrderStatus.SHIPPED)) +
                repairRequestRepository.countByDeliveryIdAndStatusIn(
                        deliveryId, List.of(RepairStatus.DEVICE_COLLECTED, RepairStatus.DEVICE_DELIVERED));

        if (activeAssignments > 0) {
            throw new IllegalStateException("Cannot delete delivery person with active assignments");
        }

        deliveryRepository.delete(delivery);
    }

    public Page<AssignerResponseDto> getAllAssigners(Pageable pageable) {
        Page<Assigner> assigners = assignerRepository.findAll(pageable);
        return assigners.map(this::convertToAssignerResponseDto);
    }

    public AssignerResponseDto getAssignerById(UUID assignerId) {
        Assigner assigner = assignerRepository.findById(assignerId)
                .orElseThrow(() -> new NotFoundException("Assigner not found with ID: " + assignerId));
        return convertToAssignerResponseDto(assigner);
    }

    public Page<AssignerResponseDto> getPendingAssigners(Pageable pageable) {
        Page<Assigner> assigners = assignerRepository.findByStatus(ApprovalStatus.PENDING, pageable);
        return assigners.map(this::convertToAssignerResponseDto);
    }

    public Page<AssignerResponseDto> getApprovedAssigners(Pageable pageable) {
        Page<Assigner> assigners = assignerRepository.findByStatus(ApprovalStatus.APPROVED, pageable);
        return assigners.map(this::convertToAssignerResponseDto);
    }

    public Page<AssignerResponseDto> getSuspendedAssigners(Pageable pageable) {
        Page<Assigner> assigners = assignerRepository.findByStatus(ApprovalStatus.SUSPENDED, pageable);
        return assigners.map(this::convertToAssignerResponseDto);
    }

    @Transactional
    public void approveAssigner(UUID assignerId) {
        Assigner assigner = assignerRepository.findById(assignerId)
                .orElseThrow(() -> new NotFoundException("Assigner not found with ID: " + assignerId));

        assigner.setStatus(ApprovalStatus.APPROVED);
        assigner.setActivate(true);
        assigner.setVerified(true);
        assignerRepository.save(assigner);

        notificationService.sendToAssigner(assignerId,
                "Congratulations! Your assigner account has been approved by admin. You can now start managing delivery assignments.");
    }

    @Transactional
    public void suspendAssigner(UUID assignerId) {
        Assigner assigner = assignerRepository.findById(assignerId)
                .orElseThrow(() -> new NotFoundException("Assigner not found with ID: " + assignerId));

        assigner.setStatus(ApprovalStatus.SUSPENDED);
        assigner.setActivate(false);
        assignerRepository.save(assigner);

        notificationService.sendToAssigner(assignerId,
                "Your assigner account has been suspended by admin. Please contact support for more information.");
    }

    @Transactional
    public void deleteAssigner(UUID assignerId) {
        Assigner assigner = assignerRepository.findById(assignerId)
                .orElseThrow(() -> new NotFoundException("Assigner not found with ID: " + assignerId));

        long activeAssignments = assignmentLogRepository.countByAssignerId(assignerId);
        if (activeAssignments > 0) {
            throw new IllegalStateException("Cannot delete assigner with active assignments");
        }

        assignerRepository.delete(assigner);
    }

    private DeliveryResponseDto convertToDeliveryResponseDto(Delivery delivery) {
        DeliveryResponseDto dto = new DeliveryResponseDto();
        dto.setId(delivery.getId());
        dto.setEmail(delivery.getEmail());
        dto.setName(delivery.getName());
        dto.setAddress(delivery.getAddress());
        dto.setPhone(delivery.getPhone());
        dto.setStatus(delivery.getStatus());
        dto.setActivate(delivery.isActivate());
        dto.setVerified(delivery.getVerified());
        dto.setCreatedAt(delivery.getCreatedAt());
        dto.setNotificationHistory(delivery.getNotificationHistory());

        long activeOrderDeliveries = orderRepository.countByDeliveryIdAndStatusIn(
                delivery.getId(), List.of(OrderStatus.SHIPPED));
        long activeRepairDeliveries = repairRequestRepository.countByDeliveryIdAndStatusIn(
                delivery.getId(), List.of(RepairStatus.DEVICE_COLLECTED, RepairStatus.DEVICE_DELIVERED));
        long totalCompleted = orderRepository.countByDeliveryIdAndStatusIn(
                delivery.getId(), List.of(OrderStatus.DELIVERED)) +
                repairRequestRepository.countByDeliveryIdAndStatusIn(
                        delivery.getId(), List.of(RepairStatus.REPAIR_COMPLETED));
        dto.setActiveOrderDeliveries((int) activeOrderDeliveries);
        dto.setActiveRepairDeliveries((int) activeRepairDeliveries);
        dto.setTotalCompletedDeliveries((int) totalCompleted);

        return dto;
    }

    private AssignerResponseDto convertToAssignerResponseDto(Assigner assigner) {
        AssignerResponseDto dto = new AssignerResponseDto();
        dto.setId(assigner.getId());
        dto.setEmail(assigner.getEmail());
        dto.setName(assigner.getName());
        dto.setDepartment(assigner.getDepartment());
        dto.setPhone(assigner.getPhone());
        dto.setStatus(assigner.getStatus());
        dto.setActivate(assigner.isActivate());
        dto.setVerified(assigner.getVerified());
        dto.setCreatedAt(assigner.getCreatedAt());
        dto.setNotificationHistory(assigner.getNotificationHistory());
        dto.setTotalAssignmentsHandled(0);
        dto.setPendingAssignments(0);

        return dto;
    }

    public ResponseUsersDto convertDto(User user) {
        return new ResponseUsersDto(
                user.getId(),
                user.getFirst_name(),
                user.getFirst_name(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActivate());
    }

    public AdminStatsDto getAdminStats() {
        return new AdminStatsDto(
                repository.count(),
                shopRepository.count(),
                repairRequestRepository.count(),
                orderRepository.count());
    }
}
