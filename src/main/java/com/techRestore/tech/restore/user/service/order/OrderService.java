package com.techRestore.tech.restore.user.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.techRestore.tech.restore.common.exception.ActivationException;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository orderPaymentRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new NotFoundException("User not found: " + email);
        }
        if (!user.isActivate()) {
            throw new ActivationException("User account is deactivated: " + email);
        }

        return user.getId();
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        UUID userId = getCurrentUserId();
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // ✅ Ensure all cart items belong to the same shop
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

        // ✅ Create Payment row with Shop
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setShop(shop); // ✅ now like createRepairRequest
        payment.setOrderId(order.getId());
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentType(PaymentType.ORDER_PAYMENT);
        payment.setPaymentReference(UUID.randomUUID().toString());

        orderPaymentRepository.save(payment);

        // Notify the shop
        notificationService.sendToShop(shopId, "New order received: Order ID " + order.getId());

        cartItemRepository.deleteAll(cartItems);

        return DTOConverter.convertToOrderResponseDTO(order, orderItems);
    }


    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getUserOrders(Pageable pageable) {
        UUID userId = getCurrentUserId();
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);
        return ordersPage.map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            return DTOConverter.convertToOrderResponseDTO(order, items);
        });
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetails(UUID orderId) {
        UUID userId = getCurrentUserId();
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return DTOConverter.convertToOrderResponseDTO(order);
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
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
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