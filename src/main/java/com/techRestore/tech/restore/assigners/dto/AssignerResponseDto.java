package com.techRestore.tech.restore.assigners.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.ApprovalStatus;

@Data
public class AssignerResponseDto {
    private UUID id;
    private String email;
    private String name;
    private String department;
    private String phone;
    private ApprovalStatus status;
    private boolean activate;
    private boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notificationHistory;
    private int totalAssignmentsHandled;
    private int pendingAssignments;
    private LocalDateTime lastActivity;
}
