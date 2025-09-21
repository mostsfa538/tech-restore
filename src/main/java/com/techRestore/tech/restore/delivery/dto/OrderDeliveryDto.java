package com.techRestore.tech.restore.delivery.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.OrderStatus;

@Data
public class OrderDeliveryDto {
    private UUID id;
    private UUID userId;
    private UUID shopId;
    private UUID deliveryId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private AddressDto shopAddress;
    private AddressDto userAddress;
    private AddressDto deliveryAddress;

    @Data
    public static class AddressDto {
        private UUID id;
        private String street;
        private String city;
        private String state;
    }
}
