package com.techRestore.tech.restore.common.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.uuid.UuidCreator;
import com.techRestore.tech.restore.common.interfaces.OtpVerifiable;
import com.techRestore.tech.restore.common.model.enums.ApprovalStatus;
import com.techRestore.tech.restore.common.model.enums.Role;

@Entity
@Data
@Table(name = "assigner")
public class Assigner implements OtpVerifiable {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @JsonIgnore
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String department;
    
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
    private Role role = Role.ASSIGNER;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "TEXT")
    private String notificationHistory = "[]";

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.PENDING;
    
    @Column(name = "verified")
    private Boolean verified = false;
    
    @PrePersist
    protected void onCreate() {
        id = UuidCreator.getTimeOrderedEpoch();
        createdAt = LocalDateTime.now();
    }
    
    @Override
    public String getDisplayName() {
        return name;
    }
    
    @Override
    public String getEntityType() {
        return "Assigner";
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
