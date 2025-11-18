package com.techRestore.tech.restore.delivery.service;

import com.techRestore.tech.restore.common.exception.AccountNotApprovedException;
import com.techRestore.tech.restore.common.exception.ActivationException;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Address;
import com.techRestore.tech.restore.common.model.entities.Delivery;
import com.techRestore.tech.restore.common.model.entities.Order;
import com.techRestore.tech.restore.common.model.entities.Payment;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.ShopAddress;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.ApprovalStatus;
import com.techRestore.tech.restore.common.model.enums.OrderStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.repository.PaymentRepository;
import com.techRestore.tech.restore.common.services.notification.NotificationService;
import com.techRestore.tech.restore.delivery.dto.DeliveryProfileUpdateDto;
import com.techRestore.tech.restore.delivery.dto.DeliveryStateUpdate;
import com.techRestore.tech.restore.delivery.dto.OrderDeliveryDto;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.OrderRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final PaymentRepository orderPaymentRepository;
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

    @Cacheable(value = "deliveryProfile", key = "#root.methodName + '_' + getCurrentDeliveryId()")
    public Delivery getProfile() {
        UUID deliveryId = getCurrentDeliveryId();
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
    }

    @Transactional
    @CacheEvict(value = "deliveryProfile", key = "'getProfile_' + getCurrentDeliveryId()")
    public void updateProfile(DeliveryProfileUpdateDto updateDto) {
        UUID deliveryId = getCurrentDeliveryId();
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
        delivery.setName(updateDto.getName());
        delivery.setAddress(updateDto.getAddress());
        deliveryRepository.save(delivery);
    }

    @Cacheable(value = "availableOrders", key = "'FINISHPROCESSING_'+#pageable.pageNumber+'_'+#pageable.pageSize")
    public Page<OrderDeliveryDto> getAvailableOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusAndDeliveryIdIsNull(OrderStatus.FINISHPROCESSING, pageable);
        return orders.map(this::convertToDeliveryDTO);
    }

    @Cacheable(value = "myDeliveries", key = "#root.methodName + '_' + getCurrentDeliveryId() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<OrderDeliveryDto> getMyDeliveries(Pageable pageable) {
        UUID deliveryId = getCurrentDeliveryId();
        Page<Order> orders = orderRepository.findByDeliveryId(deliveryId, pageable);
        return orders.map(this::convertToDeliveryDTO);
    }

    @CacheEvict(value = {"availableOrders", "myDeliveries"}, allEntries = true)
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
        notificationService.sendToUser(order.getUserId(),
                "Your order " + orderId + " has been accepted for delivery and is now shipped");
    }

    @CacheEvict(value = "availableOrders", allEntries = true)
    @Transactional
    public void rejectDelivery(UUID orderId) {
        getCurrentDeliveryId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.FINISHPROCESSING || order.getDeliveryId() != null) {
            throw new IllegalStateException("Order is not available for delivery");
        }
        notificationService.sendToShop(order.getShopId(), "Delivery rejected for order " + orderId);
    }

    @CacheEvict(value = {"availableOrders", "myDeliveries"}, allEntries = true)
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

            Payment payment = orderPaymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new NotFoundException("Payment not found for order: " + orderId));

            if (payment.getPaymentMethod() == PaymentMethod.CASH &&
                    payment.getPaymentStatus() == PaymentStatus.PENDING) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                orderPaymentRepository.save(payment);
            }
            notificationService.sendToUser(order.getUserId(),
                    "Your order " + orderId + " status updated to " + stateUpdate.getStatus());
            notificationService.sendToShop(order.getShopId(), "Order " + orderId + " has been delivered");
        }
    }

    private OrderDeliveryDto convertToDeliveryDTO(Order order) {
        OrderDeliveryDto dto = new OrderDeliveryDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setFirstName(order.getUser().getFirst_name());
        dto.setLastName(order.getUser().getLast_name());
        dto.setPhone(order.getUser().getPhone());
        dto.setShopId(order.getShopId());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCreatedAt(order.getCreatedAt());

        User user = userRepository.findByIdWithAddresses(order.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + order.getUserId()));
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            Address userAddress = user.getAddresses().stream()
                    .filter(addr->addr.getId().equals(order.getDeliveryAddressId()))
                    .findFirst()
                    .orElse(null);
            if(userAddress!=null){        
                OrderDeliveryDto.AddressDto userAddressDto = new OrderDeliveryDto.AddressDto();
                userAddressDto.setId(userAddress.getId());
                userAddressDto.setStreet(userAddress.getStreet());
                userAddressDto.setCity(userAddress.getCity());
                userAddressDto.setState(userAddress.getState());
                dto.setUserAddress(userAddressDto);
            }
        }

        Shop shop = shopRepository.findByIdWithAddresses(order.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found with ID: " + order.getShopId()));
        if (shop.getAddresses() != null && !shop.getAddresses().isEmpty()) {
            ShopAddress shopAddress = shop.getAddresses().get(0);
            OrderDeliveryDto.AddressDto shopAddressDto = new OrderDeliveryDto.AddressDto();
            shopAddressDto.setId(shopAddress.getId());
            shopAddressDto.setStreet(shopAddress.getStreet());
            shopAddressDto.setCity(shopAddress.getCity());
            shopAddressDto.setState(shopAddress.getState());
            dto.setShopAddress(shopAddressDto);
        }
        return dto;
    }


}