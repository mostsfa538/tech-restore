package com.techRestore.tech.restore.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.techRestore.tech.restore.model.enums.Role;

@Entity
@Table(name = "users")
@Data
public class User {

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

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.GUEST;

//    @Column(name = "role", nullable = false)
//    private String role = "USER";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;
}
