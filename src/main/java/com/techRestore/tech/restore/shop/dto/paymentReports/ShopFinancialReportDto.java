package com.techRestore.tech.restore.shop.dto.paymentReports;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.PaymentMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentType;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
public class ShopFinancialReportDto {
    private BigDecimal totalSalesRevenue;
    private BigDecimal totalRepairRevenue;
    private BigDecimal totalProfit;
    private List<TransactionDto> transactions;

    public ShopFinancialReportDto(BigDecimal totalSalesRevenue, BigDecimal totalRepairRevenue, BigDecimal totalProfit, List<TransactionDto> transactions) {
        this.totalSalesRevenue = totalSalesRevenue;
        this.totalRepairRevenue = totalRepairRevenue;
        this.totalProfit = totalProfit;
        this.transactions = transactions;
    }

    // Getters and setters
    public BigDecimal getTotalSalesRevenue() {
        return totalSalesRevenue;
    }

    public void setTotalSalesRevenue(BigDecimal totalSalesRevenue) {
        this.totalSalesRevenue = totalSalesRevenue;
    }

    public BigDecimal getTotalRepairRevenue() {
        return totalRepairRevenue;
    }

    public void setTotalRepairRevenue(BigDecimal totalRepairRevenue) {
        this.totalRepairRevenue = totalRepairRevenue;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }

    public static class TransactionDto {
        private UUID id;
        private BigDecimal amount;
        private PaymentType paymentType;
        private PaymentMethod paymentMethod;
        private PaymentStatus paymentStatus;
        private LocalDateTime paidAt;
        private String paymentReference;
        private String transactionId;
        private String details;
        private UUID orderId;
        private UUID repairRequestId;

        public TransactionDto(UUID id, BigDecimal amount, PaymentType paymentType, PaymentMethod paymentMethod,
                              PaymentStatus paymentStatus, LocalDateTime paidAt, String paymentReference,
                              String transactionId, String details, UUID orderId, UUID repairRequestId) {
            this.id = id;
            this.amount = amount;
            this.paymentType = paymentType;
            this.paymentMethod = paymentMethod;
            this.paymentStatus = paymentStatus;
            this.paidAt = paidAt;
            this.paymentReference = paymentReference;
            this.transactionId = transactionId;
            this.details = details;
            this.orderId = orderId;
            this.repairRequestId = repairRequestId;
        }

        // Getters and setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public PaymentType getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(PaymentType paymentType) {
            this.paymentType = paymentType;
        }

        public PaymentMethod getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public PaymentStatus getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public LocalDateTime getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
        }

        public String getPaymentReference() {
            return paymentReference;
        }

        public void setPaymentReference(String paymentReference) {
            this.paymentReference = paymentReference;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public UUID getOrderId() {
            return orderId;
        }

        public void setOrderId(UUID orderId) {
            this.orderId = orderId;
        }

        public UUID getRepairRequestId() {
            return repairRequestId;
        }

        public void setRepairRequestId(UUID repairRequestId) {
            this.repairRequestId = repairRequestId;
        }
    }
}