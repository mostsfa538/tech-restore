package com.techRestore.tech.restore.services.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Delivery;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.DeliveryRepository;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final DeliveryRepository deliveryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void sendToShop(UUID shopId, String message) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found with ID: " + shopId));
        messagingTemplate.convertAndSendToUser(shop.getEmail(), "/queue/notifications", message);
        addToHistory(shop.getNotificationHistory(), message, shop::setNotificationHistory);
        shopRepository.save(shop);
    }

    @Transactional
    public void sendToUser(UUID userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", message);
        addToHistory(user.getNotificationHistory(), message, user::setNotificationHistory);
        userRepository.save(user);
    }

    @Transactional
    public void sendToAllDelivery(String message) {
        List<Delivery> deliveries = deliveryRepository.findAll();
        for (Delivery delivery : deliveries) {
            messagingTemplate.convertAndSendToUser(delivery.getEmail(), "/queue/notifications", message);
            addToHistory(delivery.getNotificationHistory(), message, delivery::setNotificationHistory);
            deliveryRepository.save(delivery);
        }
    }

    private void addToHistory(String currentHistory, String message, Consumer<String> setter) {
        try {
            ArrayNode arrayNode;
            if (currentHistory == null || currentHistory.trim().isEmpty() || currentHistory.equals("null")) {
                arrayNode = objectMapper.createArrayNode();
            } else {
                JsonNode historyNode = objectMapper.readTree(currentHistory);
                if (!historyNode.isArray()) {
                    arrayNode = objectMapper.createArrayNode();
                } else {
                    arrayNode = (ArrayNode) historyNode;
                }
            }

            ObjectNode newNotif = objectMapper.createObjectNode();
            newNotif.put("timestamp", LocalDateTime.now().toString());
            newNotif.put("message", message);
            arrayNode.add(newNotif);
            setter.accept(objectMapper.writeValueAsString(arrayNode));
        } catch (Exception e) {
            logger.error("Error processing notification history: {}", e.getMessage(), e);
            setter.accept("[]");
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cleanNotifications() {
        logger.info("Running notification cleanup task");
        List<Shop> shops = shopRepository.findAll();
        for (Shop shop : shops) {
            cleanHistory(shop.getNotificationHistory(), shop::setNotificationHistory);
            shopRepository.save(shop);
            logger.debug("Cleaned notification history for shop: {}", shop.getEmail());
        }

        List<User> users = userRepository.findAll();
        for (User user : users) {
            cleanHistory(user.getNotificationHistory(), user::setNotificationHistory);
            userRepository.save(user);
            logger.debug("Cleaned notification history for user: {}", user.getEmail());
        }

        List<Delivery> deliveries = deliveryRepository.findAll();
        for (Delivery delivery : deliveries) {
            cleanHistory(delivery.getNotificationHistory(), delivery::setNotificationHistory);
            deliveryRepository.save(delivery);
            logger.debug("Cleaned notification history for delivery: {}", delivery.getEmail());
        }
    }

    private void cleanHistory(String currentHistory, Consumer<String> setter) {
        try {
            ArrayNode arrayNode;
            if (currentHistory == null || currentHistory.trim().isEmpty() || currentHistory.equals("null")) {
                arrayNode = objectMapper.createArrayNode();
            } else {
                JsonNode historyNode = objectMapper.readTree(currentHistory);
                if (!historyNode.isArray()) {
                    logger.warn("Invalid notification history format, initializing new array");
                    arrayNode = objectMapper.createArrayNode();
                } else {
                    arrayNode = (ArrayNode) historyNode;
                }
            }

            ArrayNode newArray = objectMapper.createArrayNode();
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            for (JsonNode notif : arrayNode) {
                LocalDateTime timestamp = LocalDateTime.parse(notif.get("timestamp").asText());
                if (timestamp.isAfter(thirtyDaysAgo)) {
                    newArray.add(notif);
                }
            }
            setter.accept(objectMapper.writeValueAsString(newArray));
            logger.debug("Cleaned old notifications from history");
        } catch (Exception e) {
            logger.error("Error cleaning notification history: {}", e.getMessage(), e);
            setter.accept("[]");
        }
    }
}