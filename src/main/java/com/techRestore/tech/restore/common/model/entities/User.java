package com.techRestore.tech.restore.common.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.techRestore.tech.restore.common.interfaces.OtpVerifiable;
import com.techRestore.tech.restore.common.model.enums.Role;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User implements OtpVerifiable {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(name = "password", nullable = false, columnDefinition = "TEXT")
    private String password;

    @Column(name = "first_name", nullable = false, length = 100)
    private String first_name;

    @Column(name = "last_name", nullable = false, length = 100)
    private String last_name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "activate")
    private boolean activate = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Chat> chats;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Message> sentMessages;

    @Enumerated(EnumType.STRING)
    private Role role = Role.GUEST;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String notificationHistory = "[]";

    @Column(name = "opt_code", length = 6)
    private String optCode = "";

    @Column(name = "opt_code_expiry")
    private LocalDateTime OtpExpiry;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public User orElseThrow(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
    }

    @Override
    public String getDisplayName() {
        return first_name + " " + (last_name != null ? last_name : "");
    }

    @Override
    public String getEntityType() {
        return "User";
    }

    @Override
    public void setActivate(boolean activate) {
        this.activate = activate;
    }

}