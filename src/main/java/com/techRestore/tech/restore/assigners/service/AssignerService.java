package com.techRestore.tech.restore.assigners.service;

import com.techRestore.tech.restore.assigners.dto.AssignerProfileUpdateDto;
import com.techRestore.tech.restore.assigners.dto.AssignmentLogDto;
import com.techRestore.tech.restore.assigners.dto.DeliveryPersonDto;
import com.techRestore.tech.restore.assigners.dto.OrderAssignmentDto;
import com.techRestore.tech.restore.assigners.dto.RepairAssignmentDto;
import com.techRestore.tech.restore.assigners.dto.ShopAddressDto;
import com.techRestore.tech.restore.assigners.dto.UserAdressDto;
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
import com.techRestore.tech.restore.delivery.dto.RepairDeliveryDto.AddressDto;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.OrderRepository;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    private Assigner getCurrentAssigner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof AssignerPrincipal assignerPrincipal) {
            Assigner assigner = assignerPrincipal.getAssigner();
            
            if (assigner.getStatus() != ApprovalStatus.APPROVED) {
                throw new AccountNotApprovedException("Your account is not approved. Please wait for admin approval.");
            }
            
            if (!assigner.isActivate()) {
                throw new ActivationException("Account is not activated. Please check your email for activation instructions");
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

    public Page<DeliveryPersonDto> getAvailableDeliveryPersons(Pageable pageable) {
        Page<Delivery> deliveries = deliveryRepository.findAll(pageable);
        return deliveries.map(this::convertToDeliveryPersonDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDeliveryDto> getOrdersForAssignment(Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusAndDeliveryIdIsNull(OrderStatus.FINISHPROCESSING, pageable);
        return orders.map(this::convertToOrderDeliveryDto);
    }

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
        
        Delivery delivery = deliveryRepository.findById(assignmentDto.getDeliveryId())
                .orElseThrow(() -> new NotFoundException("Delivery person not found"));
        
        if (order.getStatus() != OrderStatus.FINISHPROCESSING || order.getDeliveryId() != null) {
            throw new IllegalStateException("Order is not available for assignment");
        }
        
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(order.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found"));
        
        order.setDeliveryId(assignmentDto.getDeliveryId());
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
        
        AssignmentLog assignmentLog = new AssignmentLog();
        assignmentLog.setAssigner(assigner);
        assignmentLog.setShop(shop);
        assignmentLog.setUser(user);
        assignmentLog.setDelivery(delivery);
        assignmentLog.setOrderId(order.getId());
        assignmentLog.setAssignmentType(AssignmentLog.AssignmentType.ORDER);
        assignmentLogRepository.save(assignmentLog);
        
        notificationService.sendToUser(order.getUserId(),
                "Your order " + order.getId() + " has been assigned to " + delivery.getName() + " for delivery");
        
        String deliveryMessage = "You have been assigned to deliver order " + order.getId();
        if (assignmentDto.getNotes() != null && !assignmentDto.getNotes().trim().isEmpty()) {
            deliveryMessage += ". Notes: " + assignmentDto.getNotes();
        }
        notificationService.sendToDelivery(delivery.getId(), deliveryMessage);
        
        notificationService.sendToShop(order.getShopId(), 
                "Order " + order.getId() + " has been assigned to delivery person " + delivery.getName());
    }

    @Transactional
    public void assignRepairToDelivery(RepairAssignmentDto assignmentDto) {
        Assigner assigner = getCurrentAssigner();
        
        RepairRequest repairRequest = repairRequestRepository.findById(assignmentDto.getRepairRequestId())
                .orElseThrow(() -> new NotFoundException("Repair request not found"));
        
        Delivery delivery = deliveryRepository.findById(assignmentDto.getDeliveryId())
                .orElseThrow(() -> new NotFoundException("Delivery person not found"));
        
        if (repairRequest.getStatus() != RepairStatus.REPAIR_COMPLETED || repairRequest.getDeliveryId() != null) {
            throw new IllegalStateException("Repair request is not available for assignment");
        }
        
        User user = userRepository.findById(repairRequest.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(repairRequest.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found"));
        
        repairRequest.setDeliveryId(assignmentDto.getDeliveryId());
        repairRequest.setStatus(RepairStatus.DEVICE_DELIVERED);
        repairRequestRepository.save(repairRequest);
        
        AssignmentLog assignmentLog = new AssignmentLog();
        assignmentLog.setAssigner(assigner);
        assignmentLog.setShop(shop);
        assignmentLog.setUser(user);
        assignmentLog.setRepairRequestId(repairRequest.getId());
        assignmentLog.setAssignmentType(AssignmentLog.AssignmentType.REPAIR);
        assignmentLogRepository.save(assignmentLog);
        
        notificationService.sendToUser(repairRequest.getUserId(),
                "Your repair request " + repairRequest.getId() + " has been assigned to " + delivery.getName() + " for delivery");
        
        String deliveryMessage = "You have been assigned to deliver repair request " + repairRequest.getId();
        if (assignmentDto.getNotes() != null && !assignmentDto.getNotes().trim().isEmpty()) {
            deliveryMessage += ". Notes: " + assignmentDto.getNotes();
        }
        notificationService.sendToDelivery(delivery.getId(), deliveryMessage);
        
        notificationService.sendToShop(repairRequest.getShopId(), 
                "Repair request " + repairRequest.getId() + " has been assigned to delivery person " + delivery.getName());
    }

    @Transactional(readOnly = true)
    public Page<OrderDeliveryDto> getAssignedOrdersByDelivery(UUID deliveryId, Pageable pageable) {
        getCurrentAssigner(); 
        
        Page<Order> orders = orderRepository.findByDeliveryId(deliveryId, pageable);
        return orders.map(this::convertToOrderDeliveryDto);
    }

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
        
        AssignmentLog assignmentLog = new AssignmentLog();
        assignmentLog.setAssigner(assigner);
        assignmentLog.setShop(shop);
        assignmentLog.setUser(user);
        assignmentLog.setOrderId(order.getId());
        assignmentLog.setAssignmentType(AssignmentLog.AssignmentType.ORDER);
        assignmentLogRepository.save(assignmentLog);
        
        if (oldDeliveryId != null) {
            notificationService.sendToDelivery(oldDeliveryId, 
                    "Order " + orderId + " has been reassigned to another delivery person");
        }
        
        String newDeliveryMessage = "Order " + orderId + " has been reassigned to you";
        if (notes != null && !notes.trim().isEmpty()) {
            newDeliveryMessage += ". Notes: " + notes;
        }
        notificationService.sendToDelivery(newDeliveryId, newDeliveryMessage);
        
        notificationService.sendToUser(order.getUserId(),
                "Your order " + orderId + " has been reassigned to " + newDelivery.getName());
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
        
        AssignmentLog assignmentLog = new AssignmentLog();
        assignmentLog.setAssigner(assigner);
        assignmentLog.setShop(shop);
        assignmentLog.setUser(user);
        assignmentLog.setRepairRequestId(repairRequest.getId());
        assignmentLog.setAssignmentType(AssignmentLog.AssignmentType.REPAIR);
        assignmentLogRepository.save(assignmentLog);
        
        if (oldDeliveryId != null) {
            notificationService.sendToDelivery(oldDeliveryId, 
                    "Repair request " + repairRequestId + " has been reassigned to another delivery person");
        }
        
        String newDeliveryMessage = "Repair request " + repairRequestId + " has been reassigned to you";
        if (notes != null && !notes.trim().isEmpty()) {
            newDeliveryMessage += ". Notes: " + notes;
        }
        notificationService.sendToDelivery(newDeliveryId, newDeliveryMessage);
        
        notificationService.sendToUser(repairRequest.getUserId(),
                "Your repair request " + repairRequestId + " has been reassigned to " + newDelivery.getName());
    }

    public Page<AssignmentLogDto> getAllAssignmentLogs(Pageable pageable) {
        return assignmentLogRepository.findAll(pageable).map(this::convertToAssignmentLogDto);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentLogDto> getAssignerAssignmentLogs(Pageable pageable) {
        Assigner assigner = getCurrentAssigner();
        Page<AssignmentLog> logs = assignmentLogRepository.findByAssignerId(assigner.getId(), pageable);

        logs.forEach(log -> {
            if (log.getAssigner() != null) log.getAssigner().getName();
            if (log.getShop() != null) log.getShop().getName();
            if (log.getUser() != null) log.getUser().getDisplayName();
            if (log.getDelivery() != null) log.getDelivery().getName();
        });
        

        return logs.map(this::convertToAssignmentLogDto);
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

    private OrderDeliveryDto.AddressDto toOrderAddressDto(ShopAddress address) {
    if (address == null) return null;
    OrderDeliveryDto.AddressDto dto = new OrderDeliveryDto.AddressDto();
    dto.setId(address.getId());
    dto.setStreet(address.getStreet());
    dto.setCity(address.getCity());
    dto.setState(address.getState());
    return dto;
}

    private OrderDeliveryDto.AddressDto toOrderAddressDto(Address address) {
        if (address == null) return null;
        OrderDeliveryDto.AddressDto dto = new OrderDeliveryDto.AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        return dto;
    }


    private RepairDeliveryDto.AddressDto toRepairAddressDto(ShopAddress address) {
        if (address == null) return null;
        RepairDeliveryDto.AddressDto dto = new RepairDeliveryDto.AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        return dto;
    }

    private RepairDeliveryDto.AddressDto toRepairAddressDto(Address address) {
        if (address == null) return null;
        RepairDeliveryDto.AddressDto dto = new RepairDeliveryDto.AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        return dto;
    }


    private OrderDeliveryDto convertToOrderDeliveryDto(Order order) {
        OrderDeliveryDto dto = new OrderDeliveryDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        // dto.setDeliveryId(order.getDeliveryId());
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
                dto.setShopAddress(toOrderAddressDto(shopAddress));
            });
        }

        if (order.getUserId() != null) {
            userRepository.findById(order.getUserId()).ifPresent(user -> {
                Address userAddress = user.getAddresses()
                                        .stream()
                                        .filter(Address::isDefault)
                                        .findFirst()
                                        .orElse(null);
                dto.setUserAddress(toOrderAddressDto(userAddress));
            });
        }

        // if (order.getDeliveryId() != null) {
        //     deliveryRepository.findById(order.getDeliveryId()).ifPresent(delivery -> {
        //         OrderDeliveryDto.AddressDto deliveryAdr = new OrderDeliveryDto.AddressDto();
        //         deliveryAdr.setId(delivery.getId());
        //         deliveryAdr.setStreet(delivery.getAddress());
        //         dto.setDeliveryAddress(deliveryAdr);
        //     });
        // }

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
                dto.setShopAddress(toRepairAddressDto(shopAddress));
            });
        }

        if (repairRequest.getUserId() != null) {
            userRepository.findById(repairRequest.getUserId()).ifPresent(user -> {
                Address userAddress = user.getAddresses()
                                        .stream()
                                        .filter(Address::isDefault)
                                        .findFirst()
                                        .orElse(null);
                dto.setUserAddress(toRepairAddressDto(userAddress));
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
        dto.setAssignerId(assignmentLog.getAssigner().getId());
        dto.setAssignerName(assignmentLog.getAssigner().getName());
        dto.setShopId(assignmentLog.getShop().getId());
        dto.setShopName(assignmentLog.getShop().getName());

        ShopAddress shopAddress = assignmentLog.getShop()
                                            .getAddresses()
                                            .stream()
                                            .filter(ShopAddress::isDefault)
                                            .findFirst()
                                            .orElse(null);
        if (shopAddress != null) {
            ShopAddressDto shopAddressDto = new ShopAddressDto();
            shopAddressDto.setId(shopAddress.getId());
            shopAddressDto.setState(shopAddress.getState());
            shopAddressDto.setCity(shopAddress.getCity());
            shopAddressDto.setStreet(shopAddress.getStreet());
            shopAddressDto.setBuilding(shopAddress.getBuilding());
            shopAddressDto.setNotes(shopAddress.getNotes());
            shopAddressDto.setDefault(shopAddress.isDefault());
            dto.setShopAddress(shopAddressDto);
        }

        dto.setUserId(assignmentLog.getUser().getId());
        dto.setUserName(assignmentLog.getUser().getDisplayName());

        Address userAddress = assignmentLog.getUser()
                                        .getAddresses()
                                        .stream()
                                        .filter(Address::isDefault)
                                        .findFirst()
                                        .orElse(null);
        if (userAddress != null) {
            UserAdressDto addressDto = new UserAdressDto();
            addressDto.setId(userAddress.getId());
            addressDto.setState(userAddress.getState());
            addressDto.setCity(userAddress.getCity());
            addressDto.setStreet(userAddress.getStreet());
            addressDto.setBuilding(userAddress.getBuilding());
            addressDto.setNotes(userAddress.getNotes());
            addressDto.setDefault(userAddress.isDefault());
            dto.setUserAddress(addressDto);
        }

        dto.setOrderId(assignmentLog.getOrderId());
        dto.setRepairRequestId(assignmentLog.getRepairRequestId());
        dto.setAssignmentType(assignmentLog.getAssignmentType());
        dto.setCreatedAt(assignmentLog.getCreatedAt());
        dto.setUpdatedAt(assignmentLog.getUpdatedAt());
        return dto;
    }

}