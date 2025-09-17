package com.techRestore.tech.restore.common.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.common.interfaces.OtpVerifiable;
import com.techRestore.tech.restore.common.model.enums.Role;

@Entity
@Data
@Table(name = "delivery")
public class Delivery implements OtpVerifiable {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column
    private String phone;

    @Column(name = "opt_code", length = 6)
    private String optCode = "";

    @Column(name = "opt_code_expiry")
    private LocalDateTime otpExpiry;

    @Column(name = "activate")
    private boolean activate = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.DELIVERY;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "JSONB")
    private String notificationHistory = "[]";

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getEntityType() {
        return "Delivery";
    }

    @Override
    public String getOptCode() {
        return optCode;
    }

    @Override
    public void setOptCode(String optCode) {
        this.optCode = optCode;
    }

    @Override
    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    @Override
    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    @Override
    public void setActivate(boolean activate) {
        this.activate = activate;
    }
}
