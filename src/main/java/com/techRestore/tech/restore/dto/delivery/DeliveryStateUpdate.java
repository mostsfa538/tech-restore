package com.techRestore.tech.restore.dto.delivery;

import com.techRestore.tech.restore.model.enums.OrderStatus;

import lombok.Data;

@Data
public class DeliveryStateUpdate {

    private OrderStatus status;
    private String notes;
    public boolean isValidDeliveryStatus() {
        return status == OrderStatus.DELIVERED;
    }  
}
