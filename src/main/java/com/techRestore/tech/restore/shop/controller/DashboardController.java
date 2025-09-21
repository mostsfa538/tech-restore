package com.techRestore.tech.restore.shop.controller;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.shop.dto.shop.SalesDtoRequest;
import com.techRestore.tech.restore.shop.dto.shop.SalesStatsDto;
import com.techRestore.tech.restore.shop.service.DashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/shops/dashboard")
@RequiredArgsConstructor
public class DashboardController extends BaseController {

    private final DashboardService dashboardService;

    @PostMapping("/sales/total")
    @PreAuthorize("hasRole('SELLER') or hasRole('BOTH')")
    public ResponseEntity<BigDecimal> getTotalSales(@RequestBody SalesDtoRequest salesDtoRequest) {
        return successResponse(
                dashboardService.getTotalSales(salesDtoRequest.startDate(), salesDtoRequest.endDate()));
    }

    @GetMapping("/sales/stats")
    @PreAuthorize("hasRole('SELLER') or hasRole('BOTH')")
    public ResponseEntity<SalesStatsDto> getSalesStats() {
        return successResponse(dashboardService.getSalesStats());
    }

    @PostMapping("/orders/total")
    @PreAuthorize("hasRole('SELLER') or hasRole('BOTH')")
    public ResponseEntity<Long> getTotalOrders(@RequestBody SalesDtoRequest salesDtoRequest) {
        return successResponse(
                dashboardService.getTotalOrders(salesDtoRequest.startDate(), salesDtoRequest.endDate()));
    }

    @GetMapping("/repairs/stats")
    @PreAuthorize("hasRole('REPAIRER') or hasRole('BOTH')")
    public ResponseEntity<SalesStatsDto> getRepairStats() {
        return successResponse(dashboardService.getRepairStats());
    }

    /**
     * Count total repairs for today.
     */
    @GetMapping("/repairs/total")
    @PreAuthorize("hasRole('REPAIRER') or hasRole('BOTH')")
    public ResponseEntity<Long> getTotalRepairs() {
        return successResponse(dashboardService.getTotalRepairs());
    }
}
