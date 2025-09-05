package com.techRestore.tech.restore.services.payment;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.OrderPayment;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.repository.OrderPaymentRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessingService {

  private final OrderPaymentRepository orderPaymentRepository;

  @Transactional
  public void processPaymentCallback(Map<String,Object> payload){
    String success = (String) payload.get("success");
    String paymobOrderId = (String) payload.get("order_id");
    OrderPayment payment = orderPaymentRepository.findByPaymentId(paymobOrderId)
        .orElseThrow(()-> new NotFoundException("Payment not found"));
    if("true".equals(success)){
      payment.setPaymentStatus(PaymentStatus.COMPLETED);
      orderPaymentRepository.save(payment);
    }
    else{
      payment.setPaymentStatus(PaymentStatus.FAILED);
      orderPaymentRepository.save(payment);
    }
  }
  
}
