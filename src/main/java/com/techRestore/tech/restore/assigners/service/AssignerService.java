package com.techRestore.tech.restore.assigners.service;

import com.techRestore.tech.restore.assigners.dto.*;
import com.techRestore.tech.restore.assigners.repository.AssignerRepository;
import com.techRestore.tech.restore.common.exception.AccountNotApprovedException;
import com.techRestore.tech.restore.common.exception.ActivationException;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.*;
import com.techRestore.tech.restore.common.model.enums.ApprovalStatus;
import com.techRestore.tech.restore.common.model.enums.OrderStatus;
import com.techRestore.tech.restore.common.model.enums.RepairStatus;
import com.techRestore.tech.restore.common.repository.AssignmentLogRepository;
import com.techRestore.tech.restore.common.security.userdetails.AssignerPrincipal;
import com.techRestore.tech.restore.common.services.notification.NotificationService;
import com.techRestore.tech.restore.delivery.dto.OrderDeliveryDto;
import com.techRestore.tech.restore.delivery.dto.RepairDeliveryDto;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.OrderRepository;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignerService {

    private final AssignerRepository assignerRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final AssignmentLogRepository assignmentLogRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    private Assigner getCurrentAssigner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof AssignerPrincipal assignerPrincipal) {
            Assigner assigner = assignerPrincipal.getAssigner();

            if (assigner.getStatus() != ApprovalStatus.APPROVED) {
                throw new AccountNotApprovedException("Your account is not approved. Please wait for admin approval.");
            }

            if (!assigner.isActivate()) {
                throw new ActivationException(
                        "Account is not activated. Please check your email for activation instructions");
            }

            return assigner;
        }
        throw new NotFoundException("Assigner not found in authentication context");
    }

    public Assigner getProfile() {
        return getCurrentAssigner();
    }

    @Transactional
    public void updateProfile(AssignerProfileUpdateDto updateDto) {
        Assigner assigner = getCurrentAssigner();
        assigner.setName(updateDto.getName());
        assigner.setDepartment(updateDto.getDepartment());
        assignerRepository.save(assigner);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryPersonDto> getAvailableDeliveryPersons(Pageable pageable) {
        Page<Delivery> deliveries = deliveryRepository.findAll(pageable);
        return deliveries.map(this::convertToDeliveryPersonDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDeliveryDto> getOrdersForAssignment(Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusAndDeliveryIdIsNull(OrderStatus.FINISHPROCESSING, pageable);
        return orders.map(this::convertToOrderDeliveryDto);
    }

    @Transactional(readOnly = true)
    public Page<RepairDeliveryDto> getRepairRequestsForAssignment(Pageable pageable) {
        Page<RepairRequest> repairRequests = repairRequestRepository.findByStatusInAndDeliveryIdIsNull(
                List.of(RepairStatus.REPAIR_COMPLETED), pageable);
        return repairRequests.map(this::convertToRepairDeliveryDto);
    }

    @Transactional
    public void assignOrderToDelivery(OrderAssignmentDto assignmentDto) {
        Assigner assigner = getCurrentAssigner();

        Order order = orderRepository.findById(assignmentDto.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.FINISHPROCESSING || order.getDeliveryId() != null) {
            throw new IllegalStateException("Order is not available for assignment");
        }

        Delivery delivery = deliveryRepository.findById(assignmentDto.getDeliveryId())
                .orElseThrow(() -> new NotFoundException("Delivery person not found"));

        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(order.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        int updated = orderRepository.assignOrderIfAvailable(
                order.getId(),
                delivery.getId(),
                OrderStatus.SHIPPED,
                OrderStatus.FINISHPROCESSING
        );
        
        if (updated == 0) {
            throw new IllegalStateException("Order is not available for assignment (concurrent update)");
        }

        order = orderRepository.findById(order.getId()).orElseThrow();
        
        AssignmentLog assignmentLog = createAssignmentLog(assigner, shop, user, delivery, order.getId(), null);
        assignmentLogRepository.save(assignmentLog);

        eventPublisher.publishEvent(new OrderAssignedEvent(
            order.getId(), 
            order.getUserId(), 
            order.getShopId(),
            delivery.getId(), 
            delivery.getName(), 
            assignmentDto.getNotes()
        ));
        
        log.info("Order {} assigned to delivery {} by assigner {}", 
                order.getId(), delivery.getId(), assigner.getId());
    }

    @Transactional
    public void assignRepairToDelivery(RepairAssignmentDto assignmentDto) {
        Assigner assigner = getCurrentAssigner();

        RepairRequest repairRequest = repairRequestRepository.findById(assignmentDto.getRepairRequestId())
                .orElseThrow(() -> new NotFoundException("Repair request not found"));

        if (repairRequest.getStatus() != RepairStatus.REPAIR_COMPLETED || repairRequest.getDeliveryId() != null) {
            throw new IllegalStateException("Repair request is not available for assignment");
        }

        Delivery delivery = deliveryRepository.findById(assignmentDto.getDeliveryId())
                .orElseThrow(() -> new NotFoundException("Delivery person not found"));

        User user = userRepository.findById(repairRequest.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(repairRequest.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        int updated = repairRequestRepository.assignRepairIfAvailable(
                repairRequest.getId(),
                delivery.getId(),
                RepairStatus.DEVICE_DELIVERED,
                RepairStatus.REPAIR_COMPLETED
        );
        
        if (updated == 0) {
            throw new IllegalStateException("Repair request is not available for assignment (concurrent update)");
        }

        repairRequest = repairRequestRepository.findById(repairRequest.getId()).orElseThrow();

        AssignmentLog assignmentLog = createAssignmentLog(assigner, shop, user, delivery, null, repairRequest.getId());
        assignmentLogRepository.save(assignmentLog);

        eventPublisher.publishEvent(new RepairAssignedEvent(
            repairRequest.getId(), 
            repairRequest.getUserId(),
            repairRequest.getShopId(), 
            delivery.getId(), 
            delivery.getName(), 
            assignmentDto.getNotes()
        ));
        
        log.info("Repair request {} assigned to delivery {} by assigner {}", 
                repairRequest.getId(), delivery.getId(), assigner.getId());
    }

    @Transactional(readOnly = true)
    public Page<OrderDeliveryDto> getAssignedOrdersByDelivery(UUID deliveryId, Pageable pageable) {
        getCurrentAssigner();
        Page<Order> orders = orderRepository.findByDeliveryId(deliveryId, pageable);
        return orders.map(this::convertToOrderDeliveryDto);
    }

    @Transactional(readOnly = true)
    public Page<RepairDeliveryDto> getAssignedRepairsByDelivery(UUID deliveryId, Pageable pageable) {
        getCurrentAssigner();
        Page<RepairRequest> repairRequests = repairRequestRepository.findByDeliveryId(deliveryId, pageable);
        return repairRequests.map(this::convertToRepairDeliveryDto);
    }

    @Transactional
    public void reassignOrder(UUID orderId, UUID newDeliveryId, String notes) {
        Assigner assigner = getCurrentAssigner();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        Delivery newDelivery = deliveryRepository.findById(newDeliveryId)
                .orElseThrow(() -> new NotFoundException("New delivery person not found"));

        UUID oldDeliveryId = order.getDeliveryId();
        if (oldDeliveryId == null) {
            throw new IllegalStateException("Order is not currently assigned to any delivery person");
        }

        if (oldDeliveryId.equals(newDeliveryId)) {
            throw new IllegalStateException("Order is already assigned to this delivery person");
        }

        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(order.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        order.setDeliveryId(newDeliveryId);
        orderRepository.save(order);

        AssignmentLog assignmentLog = createAssignmentLog(assigner, shop, user, newDelivery, order.getId(), null);
        assignmentLogRepository.save(assignmentLog);

        notificationService.sendToDelivery(oldDeliveryId,
                "Order " + orderId + " has been reassigned to another delivery person");

        String newDeliveryMessage = "Order " + orderId + " has been reassigned to you";
        if (notes != null && !notes.trim().isEmpty()) {
            newDeliveryMessage += ". Notes: " + notes;
        }
        notificationService.sendToDelivery(newDeliveryId, newDeliveryMessage);

        notificationService.sendToUser(order.getUserId(),
                "Your order " + orderId + " has been reassigned to " + newDelivery.getName());
        
        log.info("Order {} reassigned from delivery {} to {} by assigner {}", 
                orderId, oldDeliveryId, newDeliveryId, assigner.getId());
    }

    @Transactional
    public void reassignRepairRequest(UUID repairRequestId, UUID newDeliveryId, String notes) {
        Assigner assigner = getCurrentAssigner();

        RepairRequest repairRequest = repairRequestRepository.findById(repairRequestId)
                .orElseThrow(() -> new NotFoundException("Repair request not found"));

        Delivery newDelivery = deliveryRepository.findById(newDeliveryId)
                .orElseThrow(() -> new NotFoundException("New delivery person not found"));

        UUID oldDeliveryId = repairRequest.getDeliveryId();
        if (oldDeliveryId == null) {
            throw new IllegalStateException("Repair request is not currently assigned to any delivery person");
        }

        if (oldDeliveryId.equals(newDeliveryId)) {
            throw new IllegalStateException("Repair request is already assigned to this delivery person");
        }

        User user = userRepository.findById(repairRequest.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(repairRequest.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        repairRequest.setDeliveryId(newDeliveryId);
        repairRequestRepository.save(repairRequest);

        AssignmentLog assignmentLog = createAssignmentLog(assigner, shop, user, newDelivery, null, repairRequest.getId());
        assignmentLogRepository.save(assignmentLog);

        notificationService.sendToDelivery(oldDeliveryId,
                "Repair request " + repairRequestId + " has been reassigned to another delivery person");

        String newDeliveryMessage = "Repair request " + repairRequestId + " has been reassigned to you";
        if (notes != null && !notes.trim().isEmpty()) {
            newDeliveryMessage += ". Notes: " + notes;
        }
        notificationService.sendToDelivery(newDeliveryId, newDeliveryMessage);

        notificationService.sendToUser(repairRequest.getUserId(),
                "Your repair request " + repairRequestId + " has been reassigned to " + newDelivery.getName());
        
        log.info("Repair request {} reassigned from delivery {} to {} by assigner {}", 
                repairRequestId, oldDeliveryId, newDeliveryId, assigner.getId());
    }

    @Transactional(readOnly = true)
    public Page<AssignmentLogDto> getAllAssignmentLogs(Pageable pageable) {
        return assignmentLogRepository.findAll(pageable).map(this::convertToAssignmentLogDto);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentLogDto> getAssignerAssignmentLogs(Pageable pageable) {
        Assigner assigner = getCurrentAssigner();
        Page<AssignmentLog> logs = assignmentLogRepository.findByAssignerId(assigner.getId(), pageable);
        return logs.map(this::convertToAssignmentLogDto);
    }

    private AssignmentLog createAssignmentLog(Assigner assigner, Shop shop, User user, 
                                             Delivery delivery, UUID orderId, UUID repairRequestId) {
        AssignmentLog log = new AssignmentLog();
        log.setAssigner(assigner);
        log.setShop(shop);
        log.setUser(user);
        log.setDelivery(delivery);
        log.setOrderId(orderId);
        log.setRepairRequestId(repairRequestId);
        log.setAssignmentType(orderId != null ? 
            AssignmentLog.AssignmentType.ORDER : AssignmentLog.AssignmentType.REPAIR);
        return log;
    }

    private DeliveryPersonDto convertToDeliveryPersonDto(Delivery delivery) {
        DeliveryPersonDto dto = new DeliveryPersonDto();
        dto.setId(delivery.getId());
        dto.setName(delivery.getName());
        dto.setEmail(delivery.getEmail());
        dto.setPhone(delivery.getPhone());
        dto.setAddress(delivery.getAddress());
        dto.setCreatedAt(delivery.getCreatedAt());

        long orderCount = orderRepository.countByDeliveryIdAndStatusIn(
                delivery.getId(), List.of(OrderStatus.SHIPPED));
        long repairCount = repairRequestRepository.countByDeliveryIdAndStatusIn(
                delivery.getId(), List.of(RepairStatus.DEVICE_DELIVERED, RepairStatus.DEVICE_COLLECTED));

        dto.setActiveAssignments((int) (orderCount + repairCount));
        dto.setAvailable(dto.getActiveAssignments() < 5);

        return dto;
    }

    private <T> T convertAddressDto(ShopAddress address, Class<T> targetClass) {
        if (address == null) return null;
        try {
            T dto = targetClass.getDeclaredConstructor().newInstance();
            if (dto instanceof OrderDeliveryDto.AddressDto orderDto) {
                orderDto.setId(address.getId());
                orderDto.setStreet(address.getStreet());
                orderDto.setCity(address.getCity());
                orderDto.setState(address.getState());
            } else if (dto instanceof RepairDeliveryDto.AddressDto repairDto) {
                repairDto.setId(address.getId());
                repairDto.setStreet(address.getStreet());
                repairDto.setCity(address.getCity());
                repairDto.setState(address.getState());
            }
            return dto;
        } catch (Exception e) {
            log.error("Error converting shop address", e);
            return null;
        }
    }

    private <T> T convertAddressDto(Address address, Class<T> targetClass) {
        if (address == null) return null;
        try {
            T dto = targetClass.getDeclaredConstructor().newInstance();
            if (dto instanceof OrderDeliveryDto.AddressDto orderDto) {
                orderDto.setId(address.getId());
                orderDto.setStreet(address.getStreet());
                orderDto.setCity(address.getCity());
                orderDto.setState(address.getState());
            } else if (dto instanceof RepairDeliveryDto.AddressDto repairDto) {
                repairDto.setId(address.getId());
                repairDto.setStreet(address.getStreet());
                repairDto.setCity(address.getCity());
                repairDto.setState(address.getState());
            }
            return dto;
        } catch (Exception e) {
            log.error("Error converting user address", e);
            return null;
        }
    }

    private OrderDeliveryDto convertToOrderDeliveryDto(Order order) {
        OrderDeliveryDto dto = new OrderDeliveryDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setShopId(order.getShopId());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCreatedAt(order.getCreatedAt());

        if (order.getShopId() != null) {
            shopRepository.findById(order.getShopId()).ifPresent(shop -> {
                ShopAddress shopAddress = shop.getAddresses()
                        .stream()
                        .filter(ShopAddress::isDefault)
                        .findFirst()
                        .orElse(null);
                dto.setShopAddress(convertAddressDto(shopAddress, OrderDeliveryDto.AddressDto.class));
            });
        }

        if (order.getUserId() != null) {
            userRepository.findById(order.getUserId()).ifPresent(user -> {
                Address userAddress = user.getAddresses()
                        .stream()
                        .filter(Address::isDefault)
                        .findFirst()
                        .orElse(null);
                dto.setUserAddress(convertAddressDto(userAddress, OrderDeliveryDto.AddressDto.class));
            });
        }

        return dto;
    }

    private RepairDeliveryDto convertToRepairDeliveryDto(RepairRequest repairRequest) {
        RepairDeliveryDto dto = new RepairDeliveryDto();
        dto.setId(repairRequest.getId());
        dto.setUserId(repairRequest.getUserId());
        dto.setShopId(repairRequest.getShopId());
        dto.setDeliveryId(repairRequest.getDeliveryId());
        dto.setStatus(repairRequest.getStatus());
        dto.setPrice(repairRequest.getPrice());
        dto.setCreatedAt(repairRequest.getCreatedAt());

        if (repairRequest.getShopId() != null) {
            shopRepository.findById(repairRequest.getShopId()).ifPresent(shop -> {
                ShopAddress shopAddress = shop.getAddresses()
                        .stream()
                        .filter(ShopAddress::isDefault)
                        .findFirst()
                        .orElse(null);
                dto.setShopAddress(convertAddressDto(shopAddress, RepairDeliveryDto.AddressDto.class));
            });
        }

        if (repairRequest.getUserId() != null) {
            userRepository.findById(repairRequest.getUserId()).ifPresent(user -> {
                Address userAddress = user.getAddresses()
                        .stream()
                        .filter(Address::isDefault)
                        .findFirst()
                        .orElse(null);
                dto.setUserAddress(convertAddressDto(userAddress, RepairDeliveryDto.AddressDto.class));
            });
        }

        if (repairRequest.getDeliveryId() != null) {
            deliveryRepository.findById(repairRequest.getDeliveryId()).ifPresent(delivery -> {
                RepairDeliveryDto.AddressDto deliveryAdr = new RepairDeliveryDto.AddressDto();
                deliveryAdr.setId(delivery.getId());
                deliveryAdr.setStreet(delivery.getAddress());
                dto.setDeliveryAddress(deliveryAdr);
            });
        }

        return dto;
    }

    private AssignmentLogDto convertToAssignmentLogDto(AssignmentLog assignmentLog) {
        AssignmentLogDto dto = new AssignmentLogDto();
        dto.setId(assignmentLog.getId());

        if (assignmentLog.getAssigner() != null) {
            dto.setAssignerId(assignmentLog.getAssigner().getId());
            dto.setAssignerName(assignmentLog.getAssigner().getName());
        }

        if (assignmentLog.getShop() != null) {
            dto.setShopId(assignmentLog.getShop().getId());
            dto.setShopName(assignmentLog.getShop().getName());

            ShopAddress shopAddress = assignmentLog.getShop()
                    .getAddresses()
                    .stream()
                    .filter(ShopAddress::isDefault)
                    .findFirst()
                    .orElse(null);
            
            if (shopAddress != null) {
                dto.setShopAddress(convertToShopAddressDto(shopAddress));
            }
        }

        if (assignmentLog.getUser() != null) {
            dto.setUserId(assignmentLog.getUser().getId());
            dto.setUserName(assignmentLog.getUser().getDisplayName());

            Address userAddress = assignmentLog.getUser()
                    .getAddresses()
                    .stream()
                    .filter(Address::isDefault)
                    .findFirst()
                    .orElse(null);
            
            if (userAddress != null) {
                dto.setUserAddress(convertToUserAddressDto(userAddress));
            }
        }

        dto.setOrderId(assignmentLog.getOrderId());
        dto.setRepairRequestId(assignmentLog.getRepairRequestId());
        dto.setAssignmentType(assignmentLog.getAssignmentType());
        dto.setCreatedAt(assignmentLog.getCreatedAt());
        dto.setUpdatedAt(assignmentLog.getUpdatedAt());
        
        return dto;
    }

    private ShopAddressDto convertToShopAddressDto(ShopAddress address) {
        ShopAddressDto dto = new ShopAddressDto();
        dto.setId(address.getId());
        dto.setState(address.getState());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setBuilding(address.getBuilding());
        dto.setNotes(address.getNotes());
        dto.setDefault(address.isDefault());
        return dto;
    }

    private UserAdressDto convertToUserAddressDto(Address address) {
        UserAdressDto dto = new UserAdressDto();
        dto.setId(address.getId());
        dto.setState(address.getState());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setBuilding(address.getBuilding());
        dto.setNotes(address.getNotes());
        dto.setDefault(address.isDefault());
        return dto;
    }

    public static class OrderAssignedEvent {
        public final UUID orderId;
        public final UUID userId;
        public final UUID shopId;
        public final UUID deliveryId;
        public final String deliveryName;
        public final String notes;

        public OrderAssignedEvent(UUID orderId, UUID userId, UUID shopId, UUID deliveryId, 
                                 String deliveryName, String notes) {
            this.orderId = orderId;
            this.userId = userId;
            this.shopId = shopId;
            this.deliveryId = deliveryId;
            this.deliveryName = deliveryName;
            this.notes = notes;
        }
    }

    public static class RepairAssignedEvent {
        public final UUID repairRequestId;
        public final UUID userId;
        public final UUID shopId;
        public final UUID deliveryId;
        public final String deliveryName;
        public final String notes;

        public RepairAssignedEvent(UUID repairRequestId, UUID userId, UUID shopId, UUID deliveryId, 
                                  String deliveryName, String notes) {
            this.repairRequestId = repairRequestId;
            this.userId = userId;
            this.shopId = shopId;
            this.deliveryId = deliveryId;
            this.deliveryName = deliveryName;
            this.notes = notes;
        }
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderAssigned(OrderAssignedEvent event) {
        try {
            notificationService.sendToUser(event.userId,
                    "Your order " + event.orderId + " has been assigned to " + event.deliveryName + " for delivery");

            String deliveryMessage = "You have been assigned to deliver order " + event.orderId;
            if (event.notes != null && !event.notes.trim().isEmpty()) {
                deliveryMessage += ". Notes: " + event.notes;
            }
            notificationService.sendToDelivery(event.deliveryId, deliveryMessage);

            notificationService.sendToShop(event.shopId,
                    "Order " + event.orderId + " has been assigned to delivery person " + event.deliveryName);
            
            log.info("Notifications sent for order assignment: {}", event.orderId);
        } catch (Exception e) {
            log.error("Failed to send notifications for order assignment: {}", event.orderId, e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRepairAssigned(RepairAssignedEvent event) {
        notificationService.sendToUser(event.userId,
                "Your repair request " + event.repairRequestId + " has been assigned to " + event.deliveryName + " for delivery");

        String deliveryMessage = "You have been assigned to deliver repair request " + event.repairRequestId;
        if (event.notes != null && !event.notes.trim().isEmpty()) {
            deliveryMessage += ". Notes: " + event.notes;
        }
        notificationService.sendToDelivery(event.deliveryId, deliveryMessage);

        notificationService.sendToShop(event.shopId,
                "Repair request " + event.repairRequestId + " has been assigned to delivery person " + event.deliveryName);
    }
}
