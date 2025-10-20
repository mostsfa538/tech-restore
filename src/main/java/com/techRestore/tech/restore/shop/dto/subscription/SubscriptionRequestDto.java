package com.techRestore.tech.restore.shop.dto.subscription;

import com.techRestore.tech.restore.common.model.enums.SubscriptionType;
import lombok.Data;

@Data
public class SubscriptionRequestDto {
    private Integer months;
    private SubscriptionType type;
}
