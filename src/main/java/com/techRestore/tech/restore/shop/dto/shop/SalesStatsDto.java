package com.techRestore.tech.restore.shop.dto.shop;

import java.math.BigDecimal;

public record SalesStatsDto(
        BigDecimal totalSales,
        BigDecimal previousDaySales,
        BigDecimal difference,
        boolean increased) {
}
