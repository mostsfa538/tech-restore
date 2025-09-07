package com.techRestore.tech.restore.common.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.PaymentMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;

@Entity
@Table(name = "repair_payment")
@Data
public class RepairPayment {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, unique = true)
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "repair_request_id")
  private UUID repairRequestId;

  @Column(precision = 10, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method")
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status")
  private PaymentStatus paymentStatus;

  @Column(name = "payment_reference")
  private String paymentReference;

  @Column(name = "payment_id", nullable = true, unique = true)
  private String paymentId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "repair_request_id", insertable = false, updatable = false)
  private RepairRequest repairRequest;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public void setTransactionId(String id2) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setTransactionId'");
  }

  public String getTransactionId() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getTransactionId'");
  }
}
