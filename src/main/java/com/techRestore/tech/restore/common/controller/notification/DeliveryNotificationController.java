package com.techRestore.tech.restore.common.controller.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.techRestore.tech.restore.common.services.notification.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications/delivery")
public class DeliveryNotificationController {
  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<JsonNode>getDeliveryNotification(){
    return ResponseEntity.ok(notificationService.getDeliveryNotifications());
  }

  @GetMapping("/{notificationId}")
  public ResponseEntity<JsonNode>getNotificationById(@PathVariable String notificationId) {
    return ResponseEntity.ok(notificationService.getDeliveryNotificationById(notificationId));
  }
}
