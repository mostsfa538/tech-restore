package com.techRestore.tech.restore.common.model.enums;

public enum RepairStatus {
    SUBMITTED, // Customer submitted repair request
    QUOTE_PENDING, // Shop reviewing damage and preparing quote
    QUOTE_SENT, // Quote sent to customer for approval
    QUOTE_APPROVED, // Customer approved the quote
    QUOTE_REJECTED, // Customer rejected the quote
    DEVICE_COLLECTED, // Device picked up from customer
    REPAIRING, // Actual repair work in progress
    REPAIR_COMPLETED, // Repair finished, ready for return
    DEVICE_DELIVERED, // Device returned to customer
    CANCELLED, // Repair cancelled
    FAILED // Repair could not be completed
}
