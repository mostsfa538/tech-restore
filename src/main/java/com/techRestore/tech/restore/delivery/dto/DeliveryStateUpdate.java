package com.techRestore.tech.restore.delivery.dto;

import com.techRestore.tech.restore.common.model.enums.OrderStatus;

import lombok.Data;

@Data
public class DeliveryStateUpdate {

    private OrderStatus status;
    private String notes;

    public boolean isValidDeliveryStatus() {
        return status == OrderStatus.DELIVERED;
    }
}
