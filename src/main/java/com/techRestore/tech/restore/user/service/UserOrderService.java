package com.techRestore.tech.restore.user.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.*;
import com.techRestore.tech.restore.common.model.enums.OrderStatus;
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
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        UUID shopId = cartItems.get(0).getShopId();
        boolean allSameShop = cartItems.stream().allMatch(ci -> ci.getShopId().equals(shopId));
        if (!allSameShop) {
            throw new IllegalArgumentException("All items in the order must be from the same shop");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setDeliveryAddressId(request.getDeliveryAddressId());
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setShopId(shopId);
        orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtCheckout(product.getPrice());
            orderItem.setShopId(cartItem.getShopId());
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setShop(shop);
        payment.setOrderId(order.getId());
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentType(PaymentType.ORDER_PAYMENT);
        payment.setPaymentReference(UUID.randomUUID().toString());

        orderPaymentRepository.save(payment);

        notificationService.sendToShop(shopId, "New order received: Order ID " + order.getId());

        cartItemRepository.deleteAll(cartItems);

        return DTOConverter.convertToOrderResponseDTO(order, orderItems, shop.getName());
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getUserOrders(Pageable pageable) {
        UUID userId = getCurrentUserId();
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);
        return ordersPage.map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            String shoName = null;
            Shop shopId = shopRepository.findById(order.getShopId())
                    .orElseThrow(() -> new NotFoundException("Shop not found"));
            if (shopId != null) {
                shoName = shopId.getName();
            }
            return DTOConverter.convertToOrderResponseDTO(order, items, shoName);
        });
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetails(UUID orderId) {
        UUID userId = getCurrentUserId();
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        String shopName = null;
        Shop shopId = shopRepository.findById(order.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found"));
        if (shopId != null) {
            shopName = shopId.getName();
        }
        return DTOConverter.convertToOrderResponseDTO(order, shopName);
    }

    public String updateRefundStatus(UUID orderId) {
        Payment payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        if (payment.getPaymentStatus() != PaymentStatus.NEEDREFUND) {
            throw new IllegalArgumentException("Payment is not in NEEDREFUND status");
        }
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        orderPaymentRepository.save(payment);
        return "Refund status updated successfully.";
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        UUID userId = getCurrentUserId();
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order cannot be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        Payment payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            payment.setPaymentStatus(PaymentStatus.NEEDREFUND);
        } else {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        orderPaymentRepository.save(payment);
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