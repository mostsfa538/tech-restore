package com.techRestore.tech.restore.services.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.dto.order.OrderRequestDTO;
import com.techRestore.tech.restore.dto.order.OrderItemResponseDTO;
import com.techRestore.tech.restore.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.dto.order.TrackingResponseDTO;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.CartItem;
import com.techRestore.tech.restore.model.entities.Order;
import com.techRestore.tech.restore.model.entities.OrderItem;
import com.techRestore.tech.restore.model.entities.OrderPayment;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.model.enums.OrderStatus;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.repository.CartItemRepository;
import com.techRestore.tech.restore.repository.OrderItemRepository;
import com.techRestore.tech.restore.repository.OrderPaymentRepository;
import com.techRestore.tech.restore.repository.OrderRepository;
import com.techRestore.tech.restore.repository.ProductRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponseDTO createOrder(UUID userId, OrderRequestDTO request) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
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

        OrderPayment payment = new OrderPayment();
        payment.setUserId(userId);
        payment.setOrderId(order.getId());
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentReference(UUID.randomUUID().toString());
        orderPaymentRepository.save(payment);

        order.setPaymentId(payment.getId());
        orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);

        return mapToOrderResponseDTO(order, orderItems);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getUserOrders(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);
        return ordersPage.map(this::mapToOrderResponseDTO);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetails(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return mapToOrderResponseDTO(order);
    }

    @Transactional
    public void cancelOrder(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        OrderPayment payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        orderPaymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public TrackingResponseDTO trackOrder(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        TrackingResponseDTO dto = new TrackingResponseDTO();
        dto.setStatus(order.getStatus());
        dto.setOrderId(order.getId());
        return dto;
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        return mapToOrderResponseDTO(order, orderItems);
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order, List<OrderItem> orderItems) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setDeliveryAddressId(order.getDeliveryAddressId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaymentId(order.getPaymentId());

        List<OrderItemResponseDTO> itemDTOs = orderItems.stream().map(this::mapToOrderItemResponseDTO)
                .collect(Collectors.toList());
        dto.setOrderItems(itemDTOs);

        return dto;
    }

    private OrderItemResponseDTO mapToOrderItemResponseDTO(OrderItem orderItem) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProductId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtCheckout(orderItem.getPriceAtCheckout());
        dto.setShopId(orderItem.getShopId());
        dto.setSubtotal(orderItem.getPriceAtCheckout().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return dto;
    }

}