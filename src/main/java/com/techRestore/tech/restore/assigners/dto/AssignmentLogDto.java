package com.techRestore.tech.restore.assigners.dto;

import com.techRestore.tech.restore.common.model.entities.AssignmentLog.AssignmentType;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AssignmentLogDto {
    private UUID id;
    private UUID assignerId;
    private String assignerName;
    private UUID shopId;
    private String shopName;
    private ShopAddressDto shopAddress;  
    private UUID deliveryId;
    private UUID userId;
    private String userName;
    private UserAdressDto userAddress;
    private UUID orderId;
    private UUID repairRequestId;
    private AssignmentType assignmentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

