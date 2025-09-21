package com.techRestore.tech.restore.assigners.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ReassignmentDto {
    private UUID newDeliveryId;
    private String notes;
}
