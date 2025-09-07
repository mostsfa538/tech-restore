package com.techRestore.tech.restore.common.model.enums;

public enum InventoryAction {
    STOCK_IN, // Adding inventory
    STOCK_OUT, // Removing inventory
    ADJUSTMENT, // Manual adjustment
    SALE, // Sold item
    RETURN, // Returned item
    DAMAGED, // Marked as damaged
    EXPIRED // Marked as expired
}