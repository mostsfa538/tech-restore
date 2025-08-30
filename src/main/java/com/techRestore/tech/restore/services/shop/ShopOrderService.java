package com.techRestore.tech.restore.services.shop;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.dto.order.OrderStatusUpdateDTO;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Order;
import com.techRestore.tech.restore.model.entities.OrderItem;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.enums.OrderStatus;
import com.techRestore.tech.restore.repository.OrderItemRepository;
import com.techRestore.tech.restore.repository.OrderRepository;
import com.techRestore.tech.restore.repository.ProductRepository;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.services.BaseService;
import com.techRestore.tech.restore.utils.DTOConverter;


@Service
public class ShopOrderService extends BaseService<Order, UUID> {

    private final ShopRepository shopRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ShopOrderService(OrderRepository orderRepository, ShopRepository shopRepository, 
                           OrderItemRepository orderItemRepository,ProductRepository productRepository) {
        super(orderRepository);
        this.shopRepository = shopRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    private UUID getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Shop not found with email: " + email));
        return shop.getId();
    }

    public Page<OrderResponseDTO> getAllShopOrders(Pageable pageable) {
        UUID shopId = getCurrentShopId();
        Page<Order> orders = ((OrderRepository) repository).findByShopId(shopId, pageable);
        return orders.map(this::mapToOrderResponseDTO);
    }

    public OrderResponseDTO getOrderById(UUID orderId) {
        UUID shopId = getCurrentShopId();
        Order order = ((OrderRepository) repository).findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new NotFoundException("Order not found for shop"));
        return mapToOrderResponseDTO(order);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
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
                throw new RuntimeException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + currentStock + ", Requested: " + orderedQuantity);
            }

            product.setStock(currentStock - orderedQuantity);
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CONFIRMED);
        repository.save(order);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    public void rejectOrder(UUID orderId) {
        UUID shopId = getCurrentShopId();
        Order order = ((OrderRepository) repository).findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new NotFoundException("Order not found for shop"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be rejected");
        }
        order.setStatus(OrderStatus.CANCELLED);
        repository.save(order);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
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
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("CANCELLED orders cannot be updated");
        }

        order.setStatus(statusDto.getStatus());
        repository.save(order);
    }

    public Page<OrderResponseDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        UUID shopId = getCurrentShopId();
        Page<Order> orders = ((OrderRepository) repository).findByStatusAndShopId(status, shopId, pageable);
        return orders.map(this::mapToOrderResponseDTO);
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        return DTOConverter.convertToOrderResponseDTO(order, orderItems);
    }
}