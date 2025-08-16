package com.techRestore.tech.restore.dto.order;

import java.util.UUID;

import com.techRestore.tech.restore.model.enums.PaymentMethod;


import lombok.Data;

@Data
public class OrderRequestDTO {
   private UUID deliveryAddressId;
   private PaymentMethod paymentMethod;
}
