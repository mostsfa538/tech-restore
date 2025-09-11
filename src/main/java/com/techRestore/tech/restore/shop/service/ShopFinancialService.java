package com.techRestore.tech.restore.shop.service;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Payment;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentType;
import com.techRestore.tech.restore.common.repository.PaymentRepository;
import com.techRestore.tech.restore.shop.dto.paymentReports.ShopFinancialReportDto;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopFinancialService {

    private final ShopRepository shopRepository;
    private final PaymentRepository paymentRepository;

    private UUID getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Shop not found with email: " + email));
        return shop.getId();
    }

    @Transactional(readOnly = true)
    public ShopFinancialReportDto getFinancialReport() {
        UUID shopId = getCurrentShopId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found with ID: " + shopId));

        List<Payment> payments = paymentRepository.findByShopIdAndPaymentStatus(shopId, com.techRestore.tech.restore.common.model.enums.PaymentStatus.COMPLETED);

        List<ShopFinancialReportDto.TransactionDto> transactions = payments.stream()
                .map(payment -> new ShopFinancialReportDto.TransactionDto(
                        payment.getId(),
                        payment.getAmount(),
                        payment.getPaymentType(),
                        payment.getPaymentMethod(),
                        payment.getPaymentStatus(),
                        payment.getPaidAt(),
                        payment.getPaymentReference(),
                        payment.getTransactionId(),
                        payment.getDetails(),
                        payment.getOrderId(),
                        payment.getRepairRequestId()
                ))
                .collect(Collectors.toList());

        return new ShopFinancialReportDto(
                shop.getTotalSalesRevenue(),
                shop.getTotalRepairRevenue(),
                shop.getTotalProfit(),
                transactions
        );
    }


       @Transactional(readOnly = true)
    public List<ShopFinancialReportDto.TransactionDto> getRepairPayments() {
        UUID shopId = getCurrentShopId();
        List<Payment> payments = paymentRepository.findByShopIdAndPaymentTypeAndPaymentStatus(
                shopId, PaymentType.REPAIR_PAYMENT, PaymentStatus.COMPLETED);

        return payments.stream()
                .map(payment -> new ShopFinancialReportDto.TransactionDto(
                        payment.getId(),
                        payment.getAmount(),
                        payment.getPaymentType(),
                        payment.getPaymentMethod(),
                        payment.getPaymentStatus(),
                        payment.getPaidAt(),
                        payment.getPaymentReference(),
                        payment.getTransactionId(),
                        payment.getDetails(),
                        payment.getOrderId(),
                        payment.getRepairRequestId()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShopFinancialReportDto.TransactionDto> getOrderPayments() {
        UUID shopId = getCurrentShopId();
        List<Payment> payments = paymentRepository.findByShopIdAndPaymentTypeAndPaymentStatus(
                shopId, PaymentType.ORDER_PAYMENT, PaymentStatus.COMPLETED);

        return payments.stream()
                .map(payment -> new ShopFinancialReportDto.TransactionDto(
                        payment.getId(),
                        payment.getAmount(),
                        payment.getPaymentType(),
                        payment.getPaymentMethod(),
                        payment.getPaymentStatus(),
                        payment.getPaidAt(),
                        payment.getPaymentReference(),
                        payment.getTransactionId(),
                        payment.getDetails(),
                        payment.getOrderId(),
                        payment.getRepairRequestId()
                ))
                .collect(Collectors.toList());
    }
}
