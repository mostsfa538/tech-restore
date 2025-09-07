package com.techRestore.tech.restore.common.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.techRestore.tech.restore.common.model.enums.ContractType;
import com.techRestore.tech.restore.common.model.enums.ShopType;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "shop")
public class Shop {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 512)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 250)
    private String description;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(precision = 10, scale = 2)
    private BigDecimal rating;

    @Column(name = "contract_type")
    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @Column(name = "shop_type")
    @Enumerated(EnumType.STRING)
    private ShopType shopType;

    @Column(columnDefinition = "TEXT")
    private String notificationHistory = "[]";

    @Column(name = "repair_revenue_percentage", precision = 10, scale = 2)
    private BigDecimal repairRevenuePercentage;

    @Column(name = "product_revenue_percentage", precision = 10, scale = 2)
    private BigDecimal productRevenuePercentage;

    // NEW FIELDS FOR FINANCIAL TRACKING
    @Column(name = "total_sales_revenue", precision = 15, scale = 2)
    private BigDecimal totalSalesRevenue = BigDecimal.ZERO;

    @Column(name = "total_repair_revenue", precision = 15, scale = 2)
    private BigDecimal totalRepairRevenue = BigDecimal.ZERO;

    @Column(name = "total_profit", precision = 15, scale = 2)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    // INVENTORY TRACKING
    @Column(name = "total_products_count")
    private Integer totalProductsCount = 0;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 5;

    @Column(name = "low_stock_products_count")
    private Integer lowStockProductsCount = 0;

    @Column(name = "total_inventory_value", precision = 15, scale = 2)
    private BigDecimal totalInventoryValue = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "opt_code", length = 6)
    private String optCode = "";

    @Column(name = "opt_code_expiry")
    private LocalDateTime OtpExpiry;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepairRequest> repairRequests;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Chat> chats;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Message> sentMessages;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopAddress> addresses = new ArrayList<>();

    // NEW RELATIONSHIPS
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Offer> offers = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SupportTicket> supportTickets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
