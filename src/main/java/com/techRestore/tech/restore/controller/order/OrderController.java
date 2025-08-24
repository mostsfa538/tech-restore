package com.techRestore.tech.restore.controller.order;

import com.techRestore.tech.restore.dto.order.OrderRequestDTO;
import com.techRestore.tech.restore.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.dto.order.TrackingResponseDTO;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.UserRepository;
import com.techRestore.tech.restore.services.order.OrderService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;
	private final UserRepository userRepository;

	private UUID getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new RuntimeException("No authenticated user found");
		}

		String email = authentication.getName();
		User user = userRepository.findByEmail(email);

		if (user == null || !user.isActivate()) {
			throw new RuntimeException(
					"User account is deactivated or not found: " + email);
		}

		return user.getId();
	}

	@PostMapping
	public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) {
		UUID userId = getCurrentUserId();
		OrderResponseDTO order = orderService.createOrder(userId, request);
		return ResponseEntity.ok(order);
	}

	@GetMapping
	public ResponseEntity<Page<OrderResponseDTO>> getOrders(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		UUID userId = getCurrentUserId();
		Page<OrderResponseDTO> orders = orderService.getUserOrders(userId, page, size);
		return ResponseEntity.ok(orders);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponseDTO> getOrderDetails(@PathVariable UUID orderId) {
		UUID userId = getCurrentUserId();
		OrderResponseDTO order = orderService.getOrderDetails(userId, orderId);
		return ResponseEntity.ok(order);
	}

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
		UUID userId = getCurrentUserId();
		orderService.cancelOrder(userId, orderId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{orderId}/tracking")
	public ResponseEntity<TrackingResponseDTO> trackOrder(@PathVariable UUID orderId) {
		UUID userId = getCurrentUserId();
		TrackingResponseDTO tracking = orderService.trackOrder(userId, orderId);
		return ResponseEntity.ok(tracking);
	}
}