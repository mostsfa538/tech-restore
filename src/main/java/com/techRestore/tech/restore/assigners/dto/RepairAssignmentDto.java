package com.techRestore.tech.restore.assigners.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class RepairAssignmentDto {
    private UUID repairRequestId;
    private UUID deliveryId;
    private String notes;
}