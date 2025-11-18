package com.techRestore.tech.restore.delivery.dto;

import com.techRestore.tech.restore.common.model.enums.RepairStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairDeliveryDto {
    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private UUID shopId;
    private UUID deliveryId;
    private RepairStatus status;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private AddressDto userAddress;
    private AddressDto shopAddress;
    private AddressDto deliveryAddress;

    @Getter
    @Setter
    public static class AddressDto {
        private UUID id;
        private String street;
        private String city;
        private String state;
    }
}
