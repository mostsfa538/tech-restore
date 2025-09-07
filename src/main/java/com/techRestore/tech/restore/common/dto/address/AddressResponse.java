package com.techRestore.tech.restore.common.dto.address;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class AddressResponse {
        private UUID id;
        private String state;
        private String city;
        private String street;
        private String building;
        private String notes;
        private boolean isDefault;
        private UUID userId;
        private LocalDateTime createdAt;
}