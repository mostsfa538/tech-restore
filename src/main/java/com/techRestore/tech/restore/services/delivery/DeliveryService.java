package com.techRestore.tech.restore.services.delivery;

import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Delivery;
import com.techRestore.tech.restore.model.entities.Order;
import com.techRestore.tech.restore.model.enums.OrderStatus;
import com.techRestore.tech.restore.repository.DeliveryRepository;
import com.techRestore.tech.restore.repository.OrderRepository;
import com.techRestore.tech.restore.services.notification.NotificationService;
import com.techRestore.tech.restore.dto.delivery.DeliveryProfileUpdateDto;
import com.techRestore.tech.restore.dto.delivery.DeliveryStateUpdate;
import com.techRestore.tech.restore.dto.delivery.OrderDeliveryDto;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    private UUID getCurrentDeliveryId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Delivery delivery = deliveryRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Delivery not found with email: " + email));
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

    public Page<OrderDeliveryDto> getAvailableOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusAndDeliveryIdIsNull(OrderStatus.FINISHPROCESSING, pageable);
        return orders.map(this::convertToDeliveryDTO);
    }

    public Page<OrderDeliveryDto> getMyDeliveries(Pageable pageable) {
        UUID deliveryId = getCurrentDeliveryId();
        Page<Order> orders = orderRepository.findByDeliveryId(deliveryId, pageable);
        return orders.map(this::convertToDeliveryDTO);
    }

    @Transactional
    public void acceptDelivery(UUID orderId) {
        UUID deliveryId = getCurrentDeliveryId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.FINISHPROCESSING || order.getDeliveryId() != null) {
            throw new IllegalStateException("Order is not available for delivery");
        }
        order.setDeliveryId(deliveryId);
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
        notificationService.sendToUser(order.getUserId(), "Your order " + orderId + " has been accepted for delivery and is now shipped");
    }

    @Transactional
    public void rejectDelivery(UUID orderId) {
        UUID deliveryId = getCurrentDeliveryId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.FINISHPROCESSING || order.getDeliveryId() != null) {
            throw new IllegalStateException("Order is not available for delivery");
        }
        notificationService.sendToShop(order.getShopId(), "Delivery rejected for order " + orderId);
    }

    @Transactional
    public void updateOrderStatus(UUID orderId, DeliveryStateUpdate stateUpdate) {
        UUID deliveryId = getCurrentDeliveryId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (!order.getDeliveryId().equals(deliveryId)) {
            throw new IllegalStateException("You are not assigned to this order");
        }
        if (stateUpdate.getStatus() != OrderStatus.SHIPPED && stateUpdate.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Invalid status update for delivery");
        }
        order.setStatus(stateUpdate.getStatus());
        orderRepository.save(order);
        if (stateUpdate.getStatus() == OrderStatus.DELIVERED) {
            notificationService.sendToUser(order.getUserId(), "Your order " + orderId + " status updated to " + stateUpdate.getStatus());
            notificationService.sendToShop(order.getShopId(), "Order " + orderId + " has been delivered");
        }
    }

    private OrderDeliveryDto convertToDeliveryDTO(Order order) {
    OrderDeliveryDto dto = new OrderDeliveryDto();
    dto.setId(order.getId());
    dto.setUserId(order.getUserId());
    dto.setShopId(order.getShopId());
    dto.setDeliveryId(order.getDeliveryId());
    dto.setStatus(order.getStatus());
    dto.setTotalPrice(order.getTotalPrice());
    dto.setCreatedAt(order.getCreatedAt());
    return dto;
}
}