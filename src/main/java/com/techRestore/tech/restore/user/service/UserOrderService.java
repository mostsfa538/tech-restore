package com.techRestore.tech.restore.user.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.*;
import com.techRestore.tech.restore.common.model.enums.OrderStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentType;
import com.techRestore.tech.restore.common.repository.*;
import com.techRestore.tech.restore.common.services.notification.NotificationService;
import com.techRestore.tech.restore.common.utils.DTOConverter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.shop.repository.ProductRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.dto.order.OrderRequestDTO;
import com.techRestore.tech.restore.user.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.user.dto.order.TrackingResponseDTO;
import com.techRestore.tech.restore.user.repository.CartItemRepository;
import com.techRestore.tech.restore.user.repository.OrderItemRepository;
import com.techRestore.tech.restore.user.repository.OrderRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserOrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository orderPaymentRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final AuthUtil authUtil;

    private UUID getCurrentUserId() {
        return authUtil.getCurrentUser().getId();
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        UUID userId = getCurrentUserId();
        
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        validateCartItems(cartItems);
        
        UUID shopId = cartItems.get(0).getShopId();
        
        Map<UUID, Product> productMap = fetchAndValidateProducts(cartItems);
        BigDecimal totalPrice = calculateTotalPrice(cartItems, productMap);

        Order order = createOrderEntity(userId, request, shopId, totalPrice);
        orderRepository.save(order);
        log.info("Order created: {} for user: {} with total: {}", order.getId(), userId, totalPrice);

        List<OrderItem> orderItems = createOrderItems(order.getId(), cartItems, productMap);
        orderItemRepository.saveAll(orderItems);
        log.debug("Created {} order items for order: {}", orderItems.size(), order.getId());

        Payment payment = createPayment(userId, shopId, order.getId(), totalPrice, request.getPaymentMethod());
        orderPaymentRepository.save(payment);
        log.debug("Payment record created for order: {} with reference: {}", order.getId(), payment.getPaymentReference());

        notificationService.sendToShop(shopId, buildOrderNotificationMessage(order.getId(), totalPrice));

        cartItemRepository.deleteAll(cartItems);
        log.debug("Cart cleared for user: {}", userId);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));
        
        return DTOConverter.convertToOrderResponseDTO(order, orderItems, shop.getName(),user);
    }

    private void validateCartItems(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        UUID shopId = cartItems.get(0).getShopId();
        boolean allSameShop = cartItems.stream().allMatch(ci -> ci.getShopId().equals(shopId));
        
        if (!allSameShop) {
            throw new IllegalArgumentException("All items in the order must be from the same shop");
        }
    }

    private Map<UUID, Product> fetchAndValidateProducts(List<CartItem> cartItems) {
        Map<UUID, Product> productMap = new HashMap<>();
        
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + cartItem.getProductId()));
            productMap.put(product.getId(), product);
        }
        
        return productMap;
    }

    private BigDecimal calculateTotalPrice(List<CartItem> cartItems, Map<UUID, Product> productMap) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        
        for (CartItem cartItem : cartItems) {
            Product product = productMap.get(cartItem.getProductId());
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }
        
        return totalPrice;
    }

    private Order createOrderEntity(UUID userId, OrderRequestDTO request, UUID shopId, BigDecimal totalPrice) {
        Order order = new Order();
        order.setUserId(userId);
        order.setDeliveryAddressId(request.getDeliveryAddressId());
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setShopId(shopId);
        return order;
    }

    private List<OrderItem> createOrderItems(UUID orderId, List<CartItem> cartItems, Map<UUID, Product> productMap) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CartItem cartItem : cartItems) {
            Product product = productMap.get(cartItem.getProductId());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtCheckout(product.getPrice());
            orderItem.setShopId(cartItem.getShopId());
            orderItems.add(orderItem);
        }
        
        return orderItems;
    }

    private Payment createPayment(UUID userId, UUID shopId, UUID orderId, BigDecimal amount, PaymentMethod paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setShop(shop);
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentType(PaymentType.ORDER_PAYMENT);
        payment.setPaymentReference(UUID.randomUUID().toString());
        
        return payment;
    }

    private String buildOrderNotificationMessage(UUID orderId, BigDecimal totalPrice) {
        return String.format("New order received: Order ID %s with total amount %.2f", orderId, totalPrice);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getUserOrders(Pageable pageable) {
        UUID userId = getCurrentUserId();

        User user=userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Page<Order> ordersPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return ordersPage.map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            Shop shop = shopRepository.findById(order.getShopId())
                    .orElseThrow(() -> new NotFoundException("Shop not found"));
            return DTOConverter.convertToOrderResponseDTO(order, items, shop.getName(),user);
        });
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetails(UUID orderId) {
        UUID userId = getCurrentUserId();

        User user=userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        
        Shop shop = shopRepository.findById(order.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found"));
        
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        
        return DTOConverter.convertToOrderResponseDTO(order, orderItems, shop.getName(),user);
    }

    @Transactional
    public String updateRefundStatus(UUID orderId) {
        Payment payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for order: " + orderId));
        
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            log.warn("Attempted to refund already refunded payment for order: {}", orderId);
            throw new IllegalArgumentException("Payment has already been refunded");
        }
        
        if (payment.getPaymentStatus() != PaymentStatus.NEEDREFUND) {
            log.warn("Attempted to refund payment not in NEEDREFUND status. Order: {}, Status: {}", 
                    orderId, payment.getPaymentStatus());
            throw new IllegalArgumentException("Payment is not in NEEDREFUND status. Current status: " 
                    + payment.getPaymentStatus());
        }
        
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        orderPaymentRepository.save(payment);
        
        log.info("Refund processed successfully for order: {}, payment: {}", orderId, payment.getId());
        return "Refund status updated successfully.";
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        UUID userId = getCurrentUserId();
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("User {} attempted to cancel order {} with status: {}", userId, orderId, order.getStatus());
            throw new IllegalArgumentException("Order cannot be cancelled. Current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order cancelled: {} by user: {}", orderId, userId);

        Payment payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for order: " + orderId));
        
        PaymentStatus newPaymentStatus = payment.getPaymentStatus() == PaymentStatus.COMPLETED 
                ? PaymentStatus.NEEDREFUND 
                : PaymentStatus.REFUNDED;
        
        payment.setPaymentStatus(newPaymentStatus);
        orderPaymentRepository.save(payment);
        
        log.info("Payment status updated to {} for cancelled order: {}", newPaymentStatus, orderId);
        
        notificationService.sendToShop(order.getShopId(), 
                "Order cancelled by customer: Order ID " + orderId);
    }

    @Transactional(readOnly = true)
    public TrackingResponseDTO trackOrder(UUID orderId) {
        UUID userId = getCurrentUserId();
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        TrackingResponseDTO dto = new TrackingResponseDTO();
        dto.setStatus(order.getStatus());
        dto.setOrderId(order.getId());
        return dto;
    }
}