package com.techRestore.tech.restore.controller.order;

import com.techRestore.tech.restore.dto.order.OrderRequestDTO;
import com.techRestore.tech.restore.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.dto.order.TrackingResponseDTO;
import com.techRestore.tech.restore.services.order.OrderService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) {
		OrderResponseDTO order = orderService.createOrder(request);
		return ResponseEntity.ok(order);
	}

	@GetMapping
	public ResponseEntity<Page<OrderResponseDTO>> getOrders(Pageable pageable) {
		Page<OrderResponseDTO> orders = orderService.getUserOrders(pageable);
		return ResponseEntity.ok(orders);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponseDTO> getOrderDetails(@PathVariable UUID orderId) {
		OrderResponseDTO order = orderService.getOrderDetails(orderId);
		return ResponseEntity.ok(order);
	}

	@DeleteMapping("/{orderId}/cancel")
	public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
		orderService.cancelOrder(orderId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{orderId}/tracking")
	public ResponseEntity<TrackingResponseDTO> trackOrder(@PathVariable UUID orderId) {
		TrackingResponseDTO tracking = orderService.trackOrder(orderId);
		return ResponseEntity.ok(tracking);
	}
}