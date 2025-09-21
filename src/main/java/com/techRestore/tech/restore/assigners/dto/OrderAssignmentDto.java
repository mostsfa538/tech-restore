package com.techRestore.tech.restore.assigners.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class OrderAssignmentDto {
    private UUID orderId;
    private UUID deliveryId;
    private String notes;
}