package com.techRestore.tech.restore.dto.payment;

import java.util.UUID;

import lombok.Data;
@Data
public class RefundRequestDTO {
  private UUID paymentId;
}
