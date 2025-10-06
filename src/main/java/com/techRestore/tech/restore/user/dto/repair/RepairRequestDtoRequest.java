package com.techRestore.tech.restore.user.dto.repair;

import java.util.UUID;

public record RepairRequestDtoRequest(
        String description,
        UUID deviceCategory) {
}