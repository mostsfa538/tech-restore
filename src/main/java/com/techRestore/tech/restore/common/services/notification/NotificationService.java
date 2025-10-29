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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final int CLEANUP_BATCH_SIZE = 100;
    private static final int NOTIFICATION_RETENTION_DAYS = 30;
    
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final DeliveryRepository deliveryRepository;
    private final AssignerRepository assignerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    private UUID getCurrentUserId() {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new NotFoundException("User not found: " + email);
        }
        validateEntityActivation(user.isActivate(), "User", email);
        return user.getId();
    }

    private UUID getCurrentShopId() {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();
        Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Shop not found: " + email));

        validateEntityActivation(shop.isActivate(), "Shop", email);
        return shop.getId();
    }

    private UUID getCurrentDeliveryId() {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();
        Delivery delivery = deliveryRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Delivery not found: " + email));

        validateEntityActivation(delivery.isActivate(), "Delivery", email);
        return delivery.getId();
    }

    private UUID getCurrentAssignerId() {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();
        Assigner assigner = assignerRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Assigner not found: " + email));

        validateEntityActivation(assigner.isActivate(), "Assigner", email);
        return assigner.getId();
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }
        return authentication;
    }

    private void validateEntityActivation(boolean isActive, String entityType, String email) {
        if (!isActive) {
            throw new ActivationException(entityType + " account is deactivated: " + email);
        }
    }

    @Async("taskExecutor")
    public void sendToShop(UUID shopId, String message) {
        try {
            sendToShopSync(shopId, message);
        } catch (Exception e) {
            logger.error("Failed to send notification to shop {}: {}", shopId, e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    public void sendToUser(UUID userId, String message) {
        try {
            sendToUserSync(userId, message);
        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", userId, e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    public void sendToAllDelivery(String message) {
        try {
            sendToAllDeliverySync(message);
        } catch (Exception e) {
            logger.error("Failed to send notification to all deliveries: {}", e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    public void sendToAssigners(String message) {
        try {
            sendToAssignersSync(message);
        } catch (Exception e) {
            logger.error("Failed to send notification to assigners: {}", e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    public void sendToAssigner(UUID assignerId, String message) {
        try {
            sendToAssignerSync(assignerId, message);
        } catch (Exception e) {
            logger.error("Failed to send notification to assigner {}: {}", assignerId, e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    public void sendToDelivery(UUID deliveryId, String message) {
        try {
            sendToDeliverySync(deliveryId, message);
        } catch (Exception e) {
            logger.error("Failed to send notification to delivery {}: {}", deliveryId, e.getMessage(), e);
        }
    }

    // ==================== Synchronous Transactional Methods ====================

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void sendToShopSync(UUID shopId, String message) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found with ID: " + shopId));
        
        messagingTemplate.convertAndSendToUser(shop.getEmail(), "/queue/notifications", message);
        addToHistory(shop.getNotificationHistory(), message, shop::setNotificationHistory);
        shopRepository.save(shop);
        
        logger.debug("Notification sent to shop {}: {}", shopId, message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void sendToUserSync(UUID userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", message);
        addToHistory(user.getNotificationHistory(), message, user::setNotificationHistory);
        userRepository.save(user);
        
        logger.debug("Notification sent to user {}: {}", userId, message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void sendToAllDeliverySync(String message) {
        int page = 0;
        Slice<Delivery> deliveriesSlice;
        int totalSent = 0;
        
        do {
            deliveriesSlice = deliveryRepository.findAll(PageRequest.of(page, CLEANUP_BATCH_SIZE));
            List<Delivery> updatedDeliveries = new ArrayList<>();
            
            for (Delivery delivery : deliveriesSlice.getContent()) {
                try {
                    messagingTemplate.convertAndSendToUser(delivery.getEmail(), "/queue/notifications", message);
                    addToHistory(delivery.getNotificationHistory(), message, delivery::setNotificationHistory);
                    updatedDeliveries.add(delivery);
                } catch (Exception e) {
                    logger.error("Failed to send notification to delivery {}: {}", delivery.getId(), e.getMessage());
                }
            }
            
            deliveryRepository.saveAll(updatedDeliveries);
            totalSent += updatedDeliveries.size();
            page++;
        } while (deliveriesSlice.hasNext());
        
        logger.info("Notification sent to {} deliveries", totalSent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void sendToAssignersSync(String message) {
        int page = 0;
        Slice<Assigner> assignersSlice;
        int totalSent = 0;
        
        do {
            assignersSlice = assignerRepository.findAll(PageRequest.of(page, CLEANUP_BATCH_SIZE));
            List<Assigner> updatedAssigners = new ArrayList<>();
            
            for (Assigner assigner : assignersSlice.getContent()) {
                try {
                    messagingTemplate.convertAndSendToUser(assigner.getEmail(), "/queue/notifications", message);
                    addToHistory(assigner.getNotificationHistory(), message, assigner::setNotificationHistory);
                    updatedAssigners.add(assigner);
                } catch (Exception e) {
                    logger.error("Failed to send notification to assigner {}: {}", assigner.getId(), e.getMessage());
                }
            }
            
            assignerRepository.saveAll(updatedAssigners);
            totalSent += updatedAssigners.size();
            page++;
        } while (assignersSlice.hasNext());
        
        logger.info("Notification sent to {} assigners", totalSent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void sendToAssignerSync(UUID assignerId, String message) {
        Assigner assigner = assignerRepository.findById(assignerId)
                .orElseThrow(() -> new NotFoundException("Assigner not found with ID: " + assignerId));

        messagingTemplate.convertAndSendToUser(assigner.getEmail(), "/queue/notifications", message);
        addToHistory(assigner.getNotificationHistory(), message, assigner::setNotificationHistory);
        assignerRepository.save(assigner);
        
        logger.debug("Notification sent to assigner {}: {}", assignerId, message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void sendToDeliverySync(UUID deliveryId, String message) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery person not found with ID: " + deliveryId));
        
        messagingTemplate.convertAndSendToUser(delivery.getEmail(), "/queue/notifications", message);
        addToHistory(delivery.getNotificationHistory(), message, delivery::setNotificationHistory);
        deliveryRepository.save(delivery);
        
        logger.debug("Notification sent to delivery {}: {}", deliveryId, message);
    }

    // ==================== Notification Retrieval Methods ====================

    @Transactional(readOnly = true)
    public JsonNode getUserNotifications() {
        UUID currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + currentUserId));
        return parseNotificationHistory(user.getNotificationHistory());
    }

    @Transactional(readOnly = true)
    public JsonNode getShopNotifications() {
        UUID shopId = getCurrentShopId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found with ID: " + shopId));
        return parseNotificationHistory(shop.getNotificationHistory());
    }

    @Transactional(readOnly = true)
    public JsonNode getDeliveryNotifications() {
        UUID deliveryId = getCurrentDeliveryId();
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found with ID: " + deliveryId));
        return parseNotificationHistory(delivery.getNotificationHistory());
    }

    @Transactional(readOnly = true)
    public JsonNode getAssignerNotifications() {
        UUID assignerId = getCurrentAssignerId();
        Assigner assigner = assignerRepository.findById(assignerId)
                .orElseThrow(() -> new NotFoundException("Assigner not found with ID: " + assignerId));
        return parseNotificationHistory(assigner.getNotificationHistory());
    }

    @Transactional(readOnly = true)
    public JsonNode getUserNotificationById(String notifId) {
        UUID currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + currentUserId));
        return findNotificationById(user.getNotificationHistory(), notifId);
    }

    @Transactional(readOnly = true)
    public JsonNode getShopNotificationById(String notifId) {
        UUID currentShopId = getCurrentShopId();
        Shop shop = shopRepository.findById(currentShopId)
                .orElseThrow(() -> new NotFoundException("Shop not found with ID: " + currentShopId));
        return findNotificationById(shop.getNotificationHistory(), notifId);
    }

    @Transactional(readOnly = true)
    public JsonNode getDeliveryNotificationById(String notifId) {
        UUID deliveryId = getCurrentDeliveryId();
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found with ID: " + deliveryId));
        return findNotificationById(delivery.getNotificationHistory(), notifId);
    }

    @Transactional(readOnly = true)
    public JsonNode getAssignerNotificationById(String notifId) {
        UUID assignerId = getCurrentAssignerId();
        Assigner assigner = assignerRepository.findById(assignerId)
                .orElseThrow(() -> new NotFoundException("Assigner not found with ID: " + assignerId));
        return findNotificationById(assigner.getNotificationHistory(), notifId);
    }


    private JsonNode parseNotificationHistory(String notificationHistory) {
        try {
            if (notificationHistory == null || notificationHistory.trim().isEmpty() || 
                notificationHistory.equals("null")) {
                return objectMapper.createArrayNode();
            }
            return objectMapper.readTree(notificationHistory);
        } catch (Exception e) {
            logger.error("Failed to parse notification history: {}", e.getMessage());
            return objectMapper.createArrayNode();
        }
    }

    private JsonNode findNotificationById(String notificationHistory, String notifId) {
        JsonNode historyNode = parseNotificationHistory(notificationHistory);
        
        if (!historyNode.isArray()) {
            throw new RuntimeException("Notification history is not an array");
        }
        
        for (JsonNode notif : historyNode) {
            if (notif.has("id") && notif.get("id").asText().equals(notifId)) {
                return notif;
            }
        }
        
        throw new NotFoundException("Notification not found with ID: " + notifId);
    }

    private void addToHistory(String currentHistory, String message, Consumer<String> setter) {
        try {
            ArrayNode arrayNode = parseHistoryAsArray(currentHistory);
            
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

    private ArrayNode parseHistoryAsArray(String currentHistory) throws Exception {
        if (currentHistory == null || currentHistory.trim().isEmpty() || currentHistory.equals("null")) {
            return objectMapper.createArrayNode();
        }
        
        JsonNode historyNode = objectMapper.readTree(currentHistory);
        if (!historyNode.isArray()) {
            logger.warn("Invalid notification history format, initializing new array");
            return objectMapper.createArrayNode();
        }
        
        return (ArrayNode) historyNode;
    }


    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanNotifications() {
        logger.info("Starting notification cleanup task");
        
        try {
            cleanShopNotifications();
            cleanUserNotifications();
            cleanDeliveryNotifications();
            cleanAssignerNotifications();
            
            logger.info("Notification cleanup task completed successfully");
        } catch (Exception e) {
            logger.error("Error during notification cleanup: {}", e.getMessage(), e);
        }
    }

    @Transactional
    protected void cleanShopNotifications() {
        int page = 0;
        Slice<Shop> shopsSlice;
        int totalCleaned = 0;
        
        do {
            shopsSlice = shopRepository.findAll(PageRequest.of(page, CLEANUP_BATCH_SIZE));
            List<Shop> updatedShops = new ArrayList<>();
            
            for (Shop shop : shopsSlice.getContent()) {
                cleanHistory(shop.getNotificationHistory(), shop::setNotificationHistory);
                updatedShops.add(shop);
            }
            
            shopRepository.saveAll(updatedShops);
            totalCleaned += updatedShops.size();
            page++;
        } while (shopsSlice.hasNext());
        
        logger.info("Cleaned notifications for {} shops", totalCleaned);
    }

    @Transactional
    protected void cleanUserNotifications() {
        int page = 0;
        Slice<User> usersSlice;
        int totalCleaned = 0;
        
        do {
            usersSlice = userRepository.findAll(PageRequest.of(page, CLEANUP_BATCH_SIZE));
            List<User> updatedUsers = new ArrayList<>();
            
            for (User user : usersSlice.getContent()) {
                cleanHistory(user.getNotificationHistory(), user::setNotificationHistory);
                updatedUsers.add(user);
            }
            
            userRepository.saveAll(updatedUsers);
            totalCleaned += updatedUsers.size();
            page++;
        } while (usersSlice.hasNext());
        
        logger.info("Cleaned notifications for {} users", totalCleaned);
    }

    @Transactional
    protected void cleanDeliveryNotifications() {
        int page = 0;
        Slice<Delivery> deliveriesSlice;
        int totalCleaned = 0;
        
        do {
            deliveriesSlice = deliveryRepository.findAll(PageRequest.of(page, CLEANUP_BATCH_SIZE));
            List<Delivery> updatedDeliveries = new ArrayList<>();
            
            for (Delivery delivery : deliveriesSlice.getContent()) {
                cleanHistory(delivery.getNotificationHistory(), delivery::setNotificationHistory);
                updatedDeliveries.add(delivery);
            }
            
            deliveryRepository.saveAll(updatedDeliveries);
            totalCleaned += updatedDeliveries.size();
            page++;
        } while (deliveriesSlice.hasNext());
        
        logger.info("Cleaned notifications for {} deliveries", totalCleaned);
    }

    @Transactional
    protected void cleanAssignerNotifications() {
        int page = 0;
        Slice<Assigner> assignersSlice;
        int totalCleaned = 0;
        
        do {
            assignersSlice = assignerRepository.findAll(PageRequest.of(page, CLEANUP_BATCH_SIZE));
            List<Assigner> updatedAssigners = new ArrayList<>();
            
            for (Assigner assigner : assignersSlice.getContent()) {
                cleanHistory(assigner.getNotificationHistory(), assigner::setNotificationHistory);
                updatedAssigners.add(assigner);
            }
            
            assignerRepository.saveAll(updatedAssigners);
            totalCleaned += updatedAssigners.size();
            page++;
        } while (assignersSlice.hasNext());
        
        logger.info("Cleaned notifications for {} assigners", totalCleaned);
    }

    private void cleanHistory(String currentHistory, Consumer<String> setter) {
        try {
            ArrayNode arrayNode = parseHistoryAsArray(currentHistory);
            ArrayNode newArray = objectMapper.createArrayNode();
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(NOTIFICATION_RETENTION_DAYS);
            
            for (JsonNode notif : arrayNode) {
                if (notif.has("timestamp")) {
                    LocalDateTime timestamp = LocalDateTime.parse(notif.get("timestamp").asText());
                    if (timestamp.isAfter(cutoffDate)) {
                        newArray.add(notif);
                    }
                }
            }
            
            setter.accept(objectMapper.writeValueAsString(newArray));
        } catch (Exception e) {
            logger.error("Error cleaning notification history: {}", e.getMessage(), e);
            setter.accept("[]");
        }
    }
}