package com.techRestore.tech.restore.shop.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.shop.dto.shop.SalesStatsDto;
import com.techRestore.tech.restore.shop.repository.ShopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ShopRepository shopRepository;

    private UUID getCurrentShop() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Shop not found"));
        return shop.getId();
    }

    public BigDecimal getTotalSales(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        UUID shopId = getCurrentShop();

        BigDecimal orderSales = shopRepository.calculateTotalOrderSales(shopId, startOfDay, endOfDay);
        BigDecimal repairSales = shopRepository.calculateTotalRepairSales(shopId, startOfDay, endOfDay);

        return orderSales.add(repairSales);
    }

    public SalesStatsDto getSalesStats() {
        UUID shopId = getCurrentShop();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(LocalTime.MAX);

        BigDecimal todayOrderSales = shopRepository.calculateTotalOrderSales(shopId, todayStart, todayEnd);
        BigDecimal todayRepairSales = shopRepository.calculateTotalRepairSales(shopId, todayStart, todayEnd);
        BigDecimal todayTotal = todayOrderSales.add(todayRepairSales);

        BigDecimal yesterdayOrderSales = shopRepository.calculateTotalOrderSales(shopId, yesterdayStart, yesterdayEnd);
        BigDecimal yesterdayRepairSales = shopRepository.calculateTotalRepairSales(shopId, yesterdayStart,
                yesterdayEnd);
        BigDecimal yesterdayTotal = yesterdayOrderSales.add(yesterdayRepairSales);

        BigDecimal difference = todayTotal.subtract(yesterdayTotal);
        boolean increased = difference.compareTo(BigDecimal.ZERO) > 0;

        return new SalesStatsDto(todayTotal, yesterdayTotal, difference, increased);
    }

    public Long getTotalOrders(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        UUID shopId = getCurrentShop();
        return shopRepository.countOrdersByShopId(shopId, startOfDay, endOfDay);
    }

    public SalesStatsDto getRepairStats() {
        UUID shopId = getCurrentShop();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        BigDecimal todaySales = shopRepository.calculateTotalRepairSales(shopId, today.atStartOfDay(),
                today.atTime(LocalTime.MAX));
        BigDecimal yesterdaySales = shopRepository.calculateTotalRepairSales(shopId, yesterday.atStartOfDay(),
                yesterday.atTime(LocalTime.MAX));

        BigDecimal difference = todaySales.subtract(yesterdaySales);
        boolean increased = difference.compareTo(BigDecimal.ZERO) > 0;

        return new SalesStatsDto(todaySales, yesterdaySales, difference, increased);
    }

    public Long getTotalRepairs() {
        UUID shopId = getCurrentShop();
        LocalDate today = LocalDate.now();
        return shopRepository.countRepairsByShopId(shopId, today);
    }
}
