package com.techRestore.tech.restore.delivery.dto;

import com.techRestore.tech.restore.common.model.enums.RepairStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairDeliveryStateUpdate {
    private RepairStatus status;
}
