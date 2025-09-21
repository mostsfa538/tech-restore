package com.techRestore.tech.restore.assigners.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DeliveryPersonDto {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private boolean isAvailable;
    private int activeAssignments;
    private LocalDateTime createdAt;
}
