package com.techRestore.tech.restore.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.techRestore.tech.restore.model.enums.OrderStatus;
import com.techRestore.tech.restore.model.enums.PaymentMethod;

import lombok.Data;

@Data
public class OrderResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID deliveryAddressId;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private UUID paymentId;
    private List<OrderItemResponseDTO> orderItems;
}
