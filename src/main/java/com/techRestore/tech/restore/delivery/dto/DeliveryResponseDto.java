package com.techRestore.tech.restore.delivery.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.ApprovalStatus;

@Data
public class DeliveryResponseDto {
    private UUID id;
    private String email;
    private String name;
    private String address;
    private String phone;
    private boolean activate;
    private boolean verified;
    private ApprovalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notificationHistory;
    private int activeOrderDeliveries;
    private int activeRepairDeliveries;
    private int totalCompletedDeliveries;
}
