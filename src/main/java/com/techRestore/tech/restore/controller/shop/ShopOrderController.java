package com.techRestore.tech.restore.controller.shop;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.dto.order.OrderStatusUpdateDTO;
import com.techRestore.tech.restore.model.enums.OrderStatus;
import com.techRestore.tech.restore.services.shop.ShopOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shops/orders/control")
@RequiredArgsConstructor
public class ShopOrderController extends BaseController {

    private final ShopOrderService shopOrderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getAllShopOrders(Pageable pageable) {
        return successResponse(shopOrderService.getAllShopOrders(pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable UUID orderId) {
        return successResponse(shopOrderService.getOrderById(orderId));
    }

    @PostMapping("/{orderId}/accept")
    public ResponseEntity<String> acceptOrder(@PathVariable UUID orderId) {
        shopOrderService.acceptOrder(orderId);
        return updatedResponse("Order accepted successfully");
    }

    @PostMapping("/{orderId}/reject")
    public ResponseEntity<String> rejectOrder(@PathVariable UUID orderId) {
        shopOrderService.rejectOrder(orderId);
        return updatedResponse("Order rejected successfully");
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<String> updateStatus(@PathVariable UUID orderId, 
                                             @RequestBody OrderStatusUpdateDTO statusDto) {
        shopOrderService.setStatus(orderId, statusDto);
        return updatedResponse("Order status updated successfully");
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByStatus(@PathVariable OrderStatus status, 
                                                                  Pageable pageable) {
        return successResponse(shopOrderService.getOrdersByStatus(status, pageable));
    }
}