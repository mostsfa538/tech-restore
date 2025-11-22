package com.techRestore.tech.restore.common.controller.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.techRestore.tech.restore.common.services.notification.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications/shops")
public class ShopNotificationController {
  
  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<JsonNode>getShopNotification(){
    return ResponseEntity.ok(notificationService.getShopNotifications());
  }

  @GetMapping("/{notificationId}")
  public ResponseEntity<JsonNode>getNotificationById(@PathVariable String notificationId) {
    return ResponseEntity.ok(notificationService.getShopNotificationById(notificationId));
  }

  @DeleteMapping("/{notifId}")
  public ResponseEntity<?> deleteNotification(@PathVariable String notifId) {
    notificationService.deleteShopNotification(notifId);
    return ResponseEntity.ok("Notification deleted successfully");
  }

}
