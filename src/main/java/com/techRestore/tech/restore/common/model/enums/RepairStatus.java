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
    FAILED; // Repair could not be completed

    public boolean canTransitionTo(RepairStatus next) {
        return switch (this) {
            case SUBMITTED -> next == QUOTE_PENDING || next == CANCELLED;
            case QUOTE_PENDING -> next == QUOTE_SENT || next == CANCELLED;
            case QUOTE_SENT -> next == QUOTE_APPROVED || next == QUOTE_REJECTED || next == CANCELLED;
            case QUOTE_APPROVED -> next == DEVICE_COLLECTED || next == CANCELLED;
            case QUOTE_REJECTED -> next == CANCELLED; // NO GOING BACK. DEAD END.
            case DEVICE_COLLECTED -> next == REPAIRING || next == CANCELLED;
            case REPAIRING -> next == REPAIR_COMPLETED || next == FAILED || next == CANCELLED;
            case REPAIR_COMPLETED -> next == DEVICE_DELIVERED;
            case DEVICE_DELIVERED -> null; // FINAL STATE
            case CANCELLED, FAILED -> null; // FINAL STATE
        } != null;
    }
}
