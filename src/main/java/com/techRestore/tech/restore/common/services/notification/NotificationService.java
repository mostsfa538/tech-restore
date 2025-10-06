package com.techRestore.tech.restore.common.services.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techRestore.tech.restore.assigners.repository.AssignerRepository;
import com.techRestore.tech.restore.common.exception.ActivationException;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Assigner;
import com.techRestore.tech.restore.common.model.entities.Delivery;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final AssignerRepository assignerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new NotFoundException("User not found: " + email);
        }
        if (!user.isActivate()) {
            throw new ActivationException("User account is deactivated: " + email);
        }

        return user.getId();
    }

    private UUID getCurrentshopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        String email = authentication.getName();
        Shop shop = shopRepository.findByEmail(email).orElse(null);

        if (shop == null) {
            throw new NotFoundException("shop not found: " + email);
        }
        if (!shop.isActivate()) {
            throw new ActivationException("shop account is deactivated: " + email);
        }

        return shop.getId();
    }

    private UUID getCurrentDeliveryId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        String email = authentication.getName();
        Delivery delivery = deliveryRepository.findByEmail(email).orElse(null);

        if (delivery == null) {
            throw new NotFoundException("shop not found: " + email);
        }
        if (!delivery.isActivate()) {
            throw new ActivationException("shop account is deactivated: " + email);
        }

        return delivery.getId();
    }

    private UUID getCurrentAssignerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        String email = authentication.getName();
        Assigner assigner = assignerRepository.findByEmail(email).orElse(null);

        if (assigner == null) {
            throw new NotFoundException("shop not found: " + email);
        }
        if (!assigner.isActivate()) {
            throw new ActivationException("shop account is deactivated: " + email);
        }

        return assigner.getId();
    }

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

    @Transactional
    public void sendToAssigners(String message) {
        List<Assigner> assigners = assignerRepository.findAll();
        for (Assigner assigner : assigners) {
            messagingTemplate.convertAndSendToUser(assigner.getEmail(), "/queue/notifications", message);
            addToHistory(assigner.getNotificationHistory(), message, assigner::setNotificationHistory);
            assignerRepository.save(assigner);
        }
    }

    @Transactional
    public void sendToAssigner(UUID assignerId, String message) {
        Assigner assigner = assignerRepository.findById(assignerId)
                .orElseThrow(() -> new NotFoundException("Assigner not found with ID: " + assignerId));

        messagingTemplate.convertAndSendToUser(assigner.getEmail(), "/queue/notifications", message);
        addToHistory(assigner.getNotificationHistory(), message, assigner::setNotificationHistory);
        assignerRepository.save(assigner);
    }

    @Transactional
    public void sendToDelivery(UUID deliveryId, String message) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery person not found with ID: " + deliveryId));
        messagingTemplate.convertAndSendToUser(delivery.getEmail(), "/queue/notifications", message);
        addToHistory(delivery.getNotificationHistory(), message, delivery::setNotificationHistory);
        deliveryRepository.save(delivery);
    }

    public JsonNode getUserNotifications() {
        UUID currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + currentUserId));
        try {
            return objectMapper.readTree(user.getNotificationHistory());
        } catch (Exception e) {
            throw new RuntimeException("Invalid notification history format", e);
        }
    }

    public JsonNode getShopNotifications() {
        UUID shopId = getCurrentshopId();
        Shop shop = shopRepository.findById(shopId).orElse(null);
        try {
            return objectMapper.readTree(shop.getNotificationHistory());
        } catch (Exception e) {
            throw new RuntimeException("Invalid notification history format", e);
        }
    }

    public JsonNode getDeliveryNotifications() {
        UUID deliveryId = getCurrentDeliveryId();
        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        try {
            return objectMapper.readTree(delivery.getNotificationHistory());
        } catch (Exception e) {
            throw new RuntimeException("Invalid notification history format", e);
        }
    }

    public JsonNode getAssignerNotifications() {
        UUID assignerId = getCurrentAssignerId();
        Assigner assigner = assignerRepository.findById(assignerId).orElse(null);
        try {
            return objectMapper.readTree(assigner.getNotificationHistory());
        } catch (Exception e) {
            throw new RuntimeException("Invalid notification history format", e);
        }
    }

    public JsonNode getUserNotificationById(String notifId) {
        UUID currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + currentUserId));

        try {
            JsonNode historyNode = objectMapper.readTree(user.getNotificationHistory());
            if (!historyNode.isArray()) {
                throw new RuntimeException("Notification history is not an array");
            }
            for (JsonNode notif : historyNode) {
                if (notif.has("id") && notif.get("id").asText().equals(notifId)) {
                    return notif;
                }
            }
            throw new NotFoundException("Notification not found with ID: " + notifId);
        } catch (Exception e) {
            throw new RuntimeException("Invalid notification history format", e);
        }
    }

    public JsonNode getٍShopNotificationById(String notifId) {
        UUID currentShopId = getCurrentshopId();
        Shop shop = shopRepository.findById(currentShopId)
                .orElseThrow(() -> new NotFoundException("Shop not found with ID: " + currentShopId));

        try {
            JsonNode historyNode = objectMapper.readTree(shop.getNotificationHistory());
            if (!historyNode.isArray()) {
                throw new RuntimeException("Notification history is not an array");
            }
            for (JsonNode notif : historyNode) {
                if (notif.has("id") && notif.get("id").asText().equals(notifId)) {
                    return notif;
                }
            }
            throw new NotFoundException("Notification not found with ID: " + notifId);
        } catch (Exception e) {
            throw new RuntimeException("Invalid notification history format", e);
        }
    }

    public JsonNode getDeliveryNotificationById(String notifId) {
        UUID deliveryId = getCurrentDeliveryId();
        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        try {
            JsonNode historyNode = objectMapper.readTree(delivery.getNotificationHistory());
            if (!historyNode.isArray()) {
                throw new RuntimeException("Notification history is not an array");
            }
            for (JsonNode notif : historyNode) {
                if (notif.has("id") && notif.get("id").asText().equals(notifId)) {
                    return notif;
                }
            }
            throw new NotFoundException("Notification not found with ID: " + notifId);
        } catch (Exception e) {
            throw new RuntimeException("Invalid notification history format", e);
        }
    }

    public JsonNode getAssignerNotificationById(String notifId) {
        UUID assignerId = getCurrentAssignerId();
        Assigner assigner = assignerRepository.findById(assignerId).orElse(null);
        try {
            JsonNode historyNode = objectMapper.readTree(assigner.getNotificationHistory());
            if (!historyNode.isArray()) {
                throw new RuntimeException("Notification history is not an array");
            }
            for (JsonNode notif : historyNode) {
                if (notif.has("id") && notif.get("id").asText().equals(notifId)) {
                    return notif;
                }
            }
            throw new NotFoundException("Notification not found with ID: " + notifId);
        } catch (Exception e) {
            throw new RuntimeException("Invalid notification history format", e);
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
            newNotif.put("id", UUID.randomUUID().toString());
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