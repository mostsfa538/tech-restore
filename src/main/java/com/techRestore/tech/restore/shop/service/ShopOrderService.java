package com.techRestore.tech.restore.shop.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Order;
import com.techRestore.tech.restore.common.model.entities.OrderItem;
import com.techRestore.tech.restore.common.model.entities.Product;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.enums.OrderStatus;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.services.notification.NotificationService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.repository.ProductRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.user.dto.order.OrderStatusUpdateDTO;
import com.techRestore.tech.restore.user.repository.OrderItemRepository;
import com.techRestore.tech.restore.user.repository.OrderRepository;

@Service
public class ShopOrderService extends BaseService<Order, UUID> {

    private final ShopRepository shopRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public ShopOrderService(OrderRepository orderRepository, ShopRepository shopRepository,
            OrderItemRepository orderItemRepository, ProductRepository productRepository,
            NotificationService notificationService) {
        super(orderRepository);
        this.shopRepository = shopRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    private UUID getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Shop not found with email: " + email));
        return shop.getId();
    }

    @Transactional
    public Page<OrderResponseDTO> getAllShopOrders(Pageable pageable) {
        UUID shopId = getCurrentShopId();
        Page<Order> orders = ((OrderRepository) repository).findByShopIdOrderByCreatedAtDesc(shopId, pageable);
        return orders.map(this::mapToOrderResponseDTO);
    }

    public OrderResponseDTO getOrderById(UUID orderId) {
        UUID shopId = getCurrentShopId();
        Order order = ((OrderRepository) repository).findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new NotFoundException("Order not found for shop"));
        return mapToOrderResponseDTO(order);
    }

    public void acceptOrder(UUID orderId) {
        UUID shopId = getCurrentShopId();
        Order order = ((OrderRepository) repository).findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new NotFoundException("Order not found for shop"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be accepted");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("No items found for order");
        }

        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + orderItem.getProductId()));

            int currentStock = product.getStock();
            int orderedQuantity = orderItem.getQuantity();
            if (currentStock < orderedQuantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + currentStock + ", Requested: " + orderedQuantity);
            }

            product.setStock(currentStock - orderedQuantity);
            order.setShopId(shopId);
            order.setStatus(OrderStatus.CONFIRMED);
            repository.save(order);
            productRepository.save(product);
            notificationService.sendToUser(order.getUserId(),
                    "Your order " + orderId + " has been accepted by the shop");
        }
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    public void rejectOrder(UUID orderId) {
        UUID shopId = getCurrentShopId();
        Order order = ((OrderRepository) repository).findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new NotFoundException("Order not found for shop"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be rejected");
        }

        order.setShopId(shopId);
        order.setStatus(OrderStatus.CANCELLED);
        repository.save(order);
        notificationService.sendToUser(order.getUserId(), "Your order " + orderId + " has been rejected by the shop");
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @Transactional
    public void setStatus(UUID orderId, OrderStatusUpdateDTO statusDto) {

        UUID shopId = getCurrentShopId();
        Order order = ((OrderRepository) repository).findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new NotFoundException("Order not found for shop"));

        if (order.getStatus() == OrderStatus.PENDING && statusDto.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("PENDING orders can only transition to CONFIRMED");
        }
        if (order.getStatus() == OrderStatus.CONFIRMED && statusDto.getStatus() != OrderStatus.PROCESSING) {
            throw new IllegalStateException("CONFIRMED orders can only transition to PROCESSING");
        }
        if (order.getStatus() == OrderStatus.PROCESSING && statusDto.getStatus() != OrderStatus.FINISHPROCESSING) {
            throw new IllegalStateException("PROCESSING orders can only transition to FINISHPROCESSING");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("CANCELLED orders cannot be updated");
        }
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot update order after it has been shipped or Delivered");
        }

        order.setShopId(shopId);
        order.setStatus(statusDto.getStatus());
        repository.save(order);

        if (statusDto.getStatus() == OrderStatus.PROCESSING) {
            notificationService.sendToUser(order.getUserId(),
                    "Your order " + orderId + " is now being processed by the shop");
        } else if (statusDto.getStatus() == OrderStatus.FINISHPROCESSING) {
            notificationService.sendToAllDelivery("Order " + orderId + " has finished processing and is ready for delivery");

            notificationService.sendToAssigners("Order " + orderId + " has finished processing and is ready for assignment");
        }
    }

    public Page<OrderResponseDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        UUID shopId = getCurrentShopId();
        Page<Order> orders = ((OrderRepository) repository).findByStatusAndShopId(status, shopId, pageable);
        return orders.map(this::mapToOrderResponseDTO);
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        String shopName =null;
        Shop shop = shopRepository.findById(order.getShopId()).orElseThrow(() -> new NotFoundException("Shop not found"));;
        if (shop != null) {
            shopName = shop.getName();
        }
        return DTOConverter.convertToOrderResponseDTO(order, orderItems,shopName);
    }
}