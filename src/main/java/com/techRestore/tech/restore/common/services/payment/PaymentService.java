package com.techRestore.tech.restore.common.services.payment;

import com.techRestore.tech.restore.common.dto.payment.AdminPaymentDto;
import com.techRestore.tech.restore.common.dto.payment.PaymentDto;
import com.techRestore.tech.restore.common.dto.payment.PaymentInitiationDto;
import com.techRestore.tech.restore.common.exception.CustomException;
import com.techRestore.tech.restore.common.model.entities.*;
import com.techRestore.tech.restore.common.model.enums.*;
import com.techRestore.tech.restore.common.repository.PaymentRepository;
import com.techRestore.tech.restore.shop.dto.subscription.SubscriptionRequestDto;
import com.techRestore.tech.restore.shop.dto.subscription.SubscriptionResponseDto;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.shop.repository.SubscriptionRepository;
import com.techRestore.tech.restore.user.repository.OrderRepository;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    // Paymob Configuration
    @Value("${paymob.iframeId}") private String iframeId;
    @Value("${paymob.apiKey}") private String paymobApiKey;
    @Value("${paymob.authUrl}") private String paymobAuthUrl;
    @Value("${paymob.orderUrl}") private String paymobOrderUrl;
    @Value("${paymob.paymentKeyUrl}") private String paymobPaymentKeyUrl;
    @Value("${paymob.cardIntegrationId}") private int cardIntegrationId;
    @Value("${paymob.hmacSecret}") private String hmacSecretKey;

    // Repositories
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final ShopRepository shopRepository;
    private final SubscriptionRepository subscriptionRepository;

    // ============================================================================
    // USER TRANSACTIONS
    // ============================================================================

    /** Get all payment transactions for a specific user */
    public Page<PaymentDto> getAllUserTransactions(UUID userId, Pageable pageable) {
        return paymentRepository.findAllByUserId(userId, pageable).map(this::toDto);
    }

    /** Get all payment transactions for admin dashboard */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminPaymentDto> getAllTransactions(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::toAdminDto);
    }

    // ============================================================================
    // CARD PAYMENTS - USER
    // ============================================================================

    /** Initiate card payment for order or repair */
    @Transactional
    public PaymentInitiationDto initiateCardPayment(UUID referenceId, UUID userId, PaymentType paymentType) {
        if (!isSupportedPaymentType(paymentType)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Unsupported payment type: " + paymentType);
        }

        User user = getUserById(userId);
        Payment payment = findPaymentByReferenceId(referenceId);
        
        if (payment == null) {
            payment = createNewPayment(referenceId, paymentType, user);
        } else {
            validateExistingPayment(payment, paymentType, referenceId);
            updateExistingPayment(payment, user);
        }

        String authToken = getAuthToken();
        String paymobOrderId = createPaymobOrder(authToken, payment);
        payment.setTransactionId(paymobOrderId);
        paymentRepository.saveAndFlush(payment);

        String paymentToken = generatePaymentKey(authToken, paymobOrderId, payment.getAmount(), cardIntegrationId, userId);
        String paymentLink = generatePaymentIframeUrl(paymentToken);

        PaymentInitiationDto dto = new PaymentInitiationDto();
        dto.setPaymentURL(paymentLink);
        return dto;
    }

    /** Initiate card payment for shop subscription */
    @Transactional
    public PaymentInitiationDto initiateCardSubscriptionPayment(UUID shopId, SubscriptionRequestDto request) {
        validateSubscriptionRequest(request);
        Shop shop = getShopById(shopId);
        
        if (!shop.isSubscriptionActive()) {
            shop.setActivate(true);
        }
        
        BigDecimal totalAmount = BigDecimal.valueOf(1000).multiply(BigDecimal.valueOf(request.getMonths()));
        Payment payment = createSubscriptionPayment(shopId, request, totalAmount);

        String authToken = getAuthToken();
        String paymobOrderId = createPaymobOrder(authToken, payment);
        payment.setTransactionId(paymobOrderId);
        paymentRepository.saveAndFlush(payment);

        String paymentToken = generatePaymentKeyForShop(authToken, paymobOrderId, totalAmount, shop);
        String paymentLink = generatePaymentIframeUrl(paymentToken);

        PaymentInitiationDto dto = new PaymentInitiationDto();
        dto.setPaymentURL(paymentLink);
        return dto;
    }

    // ============================================================================
    // CASH PAYMENTS
    // ============================================================================

    /** Initiate cash payment for order or repair */
    @Transactional
    public void initiateCashPayment(UUID referenceId, UUID userId, PaymentType paymentType) {
        validateSupportedPaymentType(paymentType);
        BigDecimal amount = getPaymentAmount(referenceId, paymentType);
        User user = getUserById(userId);

        Payment payment = new Payment();
        setPaymentReferenceIds(payment, referenceId, paymentType);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentType(paymentType);
        payment.setPaymentReference("CASH-" + UUID.randomUUID().toString());
        paymentRepository.save(payment);
    }

    /** Initiate cash payment for shop subscription */
    @Transactional
    public void initiateCashSubscriptionPayment(UUID shopId, SubscriptionRequestDto request) {
        validateSubscriptionRequest(request);
        Shop shop = getShopById(shopId);
        BigDecimal totalAmount = BigDecimal.valueOf(1000).multiply(BigDecimal.valueOf(request.getMonths()));

        Payment payment = createSubscriptionPayment(shopId, request, totalAmount);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentReference("CASH-SUB-" + UUID.randomUUID().toString());
        paymentRepository.save(payment);
    }

    /** Update cash payment status (admin confirmation) */
    @Transactional
    public void updateCashPaymentStatus(UUID paymentId, PaymentStatus status) {
        Payment payment = getPaymentById(paymentId);
        validateCashPayment(payment);

        payment.setPaymentStatus(status);
        if (status == PaymentStatus.COMPLETED) {
            payment.setPaidAt(LocalDateTime.now());
            handlePaymentCompletion(payment);
        }
        paymentRepository.save(payment);
    }

    // ============================================================================
    // SUBSCRIPTION
    // ============================================================================

    /** Get current subscription for a shop */
    @Transactional(readOnly = true)
    public SubscriptionResponseDto getShopSubscription(UUID shopId) { 
        Subscription subscription = subscriptionRepository
            .findFirstByShopIdOrderByCreatedAtDesc(shopId)
            .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Subscription not found"));
        return toSubscriptionDto(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> getShopAllSubscription(UUID shopId) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByShopIdOrderByCreatedAtDesc(shopId);
        if (subscriptions.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "No subscriptions found");
        }
        return subscriptions.stream()
            .map(this::toSubscriptionDto)
            .collect(Collectors.toList());
    }

    /** Handle Paymob webhook callback */
    public void handlePaymentCallback(Map<String, Object> payload, HttpServletRequest request) {
        validateHmacSignature(payload, request);
        processPaymentCallback(payload);
    }

    @Transactional
    public void handlePaymentResponse(String success, String orderId, String transactionId) {
        log.info("Payment callback: success={}, orderId={}, transactionId={}", success, orderId, transactionId);
        
        if (!"true".equalsIgnoreCase(success)) {
            log.error("Payment failed: success=false");
            throw new CustomException(HttpStatus.BAD_REQUEST, "Payment failed");
        }

        Payment payment = null;
        if (orderId != null && !orderId.trim().isEmpty()) {
            payment = paymentRepository.findByTransactionId(orderId).orElse(null);
        }
        if (payment == null && transactionId != null && !transactionId.trim().isEmpty()) {
            payment = paymentRepository.findByTransactionId(transactionId).orElse(null);
        }
        
        if (payment == null) {
            log.error("Payment not found for orderId={} or transactionId={}", orderId, transactionId);
            throw new CustomException(HttpStatus.NOT_FOUND, "Payment not found");
        }

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setTransactionId(orderId != null ? orderId : transactionId);
            paymentRepository.saveAndFlush(payment);
            handlePaymentCompletion(payment);
            log.info("Payment completed: ID={}", payment.getId());
        }
    }

    // Payment Creation
    private Payment createNewPayment(UUID referenceId, PaymentType paymentType, User user) {
        BigDecimal amount = getPaymentAmount(referenceId, paymentType);
        Payment payment = new Payment();
        setPaymentReferenceIds(payment, referenceId, paymentType);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setPaymentType(paymentType);
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }

    private Payment createSubscriptionPayment(UUID shopId, SubscriptionRequestDto request, BigDecimal totalAmount) {
        Payment payment = new Payment();
        payment.setShop(getShopById(shopId));
        payment.setAmount(totalAmount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentType(PaymentType.SUBSCRIPTION_PAYMENT);
        payment.setSubscriptionId(UUID.randomUUID());
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setPaymentReference("SUB-" + request.getMonths() + "M-" + System.currentTimeMillis());
        payment.setDetails("Subscription for " + request.getMonths() + " months");
        return payment;
    }

    private void updateExistingPayment(Payment payment, User user) {
        BigDecimal amount = getPaymentAmountFromPayment(payment);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setUpdatedAt(LocalDateTime.now());
    }

    // Validation
    private void validateSubscriptionRequest(SubscriptionRequestDto request) {
        if (request.getMonths() == null || request.getMonths() < 1 || request.getMonths() > 12) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Months must be 1-12");
        }
    }

    private void validateExistingPayment(Payment payment, PaymentType expectedType, UUID referenceId) {
        PaymentType actualType = determinePaymentTypeFromPayment(payment);
        if (!actualType.equals(expectedType)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Payment type mismatch");
        }
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Payment already completed");
        }
    }

    private void validateSupportedPaymentType(PaymentType paymentType) {
        if (!isSupportedPaymentType(paymentType)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Unsupported payment type");
        }
    }

    private void validateCashPayment(Payment payment) {
        if (payment.getPaymentMethod() != PaymentMethod.CASH) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Not a cash payment");
        }
    }

    // Entity Lookup
    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Shop getShopById(UUID shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Shop not found"));
    }

    private Payment getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private BigDecimal getPaymentAmount(UUID referenceId, PaymentType paymentType) {
        return switch (paymentType) {
            case ORDER_PAYMENT -> orderRepository.findById(referenceId)
                    .map(Order::getTotalPrice)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Order not found"));
            case REPAIR_PAYMENT -> repairRequestRepository.findById(referenceId)
                    .map(RepairRequest::getPrice)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Repair not found"));
            default -> throw new CustomException(HttpStatus.BAD_REQUEST, "Unsupported payment type");
        };
    }

    private BigDecimal getPaymentAmountFromPayment(Payment payment) {
        if (payment.getOrderId() != null) {
            return orderRepository.findById(payment.getOrderId())
                    .map(Order::getTotalPrice)
                    .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "Order price missing"));
        } else if (payment.getRepairRequestId() != null) {
            return repairRequestRepository.findById(payment.getRepairRequestId())
                    .map(RepairRequest::getPrice)
                    .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "Repair price missing"));
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid payment reference");
    }

    private Payment findPaymentByReferenceId(UUID referenceId) {
        return paymentRepository.findByOrderId(referenceId).or(() ->
                paymentRepository.findByRepairRequestId(referenceId)).orElse(null);
    }

    private PaymentType determinePaymentTypeFromPayment(Payment payment) {
        if (payment.getOrderId() != null) {
            return PaymentType.ORDER_PAYMENT;
        } else if (payment.getRepairRequestId() != null) {
            return PaymentType.REPAIR_PAYMENT;
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid payment type");
    }

    private void setPaymentReferenceIds(Payment payment, UUID referenceId, PaymentType paymentType) {
        switch (paymentType) {
            case ORDER_PAYMENT, REFUND_ORDER -> payment.setOrderId(referenceId);
            case REPAIR_PAYMENT, REFUND_REPAIR -> payment.setRepairRequestId(referenceId);
            case SUBSCRIPTION_PAYMENT -> payment.setSubscriptionId(referenceId);
            case COMMISSION_PAYMENT, WITHDRAWAL -> payment.setShop(getShopById(referenceId));
            default -> throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid payment type");
        }
    }

    private String getAuthToken() {
        RestTemplate restTemplate = new RestTemplate();
        JSONObject body = new JSONObject().put("api_key", paymobApiKey);
        ResponseEntity<String> response = restTemplate.exchange(paymobAuthUrl, HttpMethod.POST,
                new HttpEntity<>(body.toString(), jsonHeaders()), String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return new JSONObject(response.getBody()).getString("token");
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "Paymob auth failed");
    }

    private String createPaymobOrder(String authToken, Payment payment) {
        RestTemplate restTemplate = new RestTemplate();
        JSONObject body = new JSONObject()
                .put("auth_token", authToken)
                .put("delivery_needed", false)
                .put("amount_cents", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue())
                .put("currency", "EGP")
                .put("items", new JSONArray());

        ResponseEntity<String> response = restTemplate.exchange(paymobOrderUrl, HttpMethod.POST,
                new HttpEntity<>(body.toString(), jsonHeaders()), String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String orderId = String.valueOf(new JSONObject(response.getBody()).getInt("id"));
            payment.setTransactionId(orderId);
            paymentRepository.save(payment);
            return orderId;
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "Failed to create Paymob order");
    }

    private String generatePaymentKey(String authToken, String orderId, BigDecimal amount, int integrationId, UUID userId) {
        User user = getUserById(userId);
        return generatePaymentKey(authToken, orderId, amount, integrationId, createBillingData(user));
    }

    private String generatePaymentKeyForShop(String authToken, String orderId, BigDecimal amount, Shop shop) {
        return generatePaymentKey(authToken, orderId, amount, cardIntegrationId, createShopBillingData(shop));
    }

    private String generatePaymentKey(String authToken, String orderId, BigDecimal amount, int integrationId, JSONObject billingData) {
        RestTemplate restTemplate = new RestTemplate();
        JSONObject body = new JSONObject()
                .put("auth_token", authToken)
                .put("amount_cents", amount.multiply(BigDecimal.valueOf(100)).intValue())
                .put("expiration", 3600)
                .put("order_id", orderId)
                .put("billing_data", billingData)
                .put("currency", "EGP")
                .put("integration_id", integrationId);

        ResponseEntity<String> response = restTemplate.exchange(paymobPaymentKeyUrl, HttpMethod.POST,
                new HttpEntity<>(body.toString(), jsonHeaders()), String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return new JSONObject(response.getBody()).getString("token");
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "Failed to generate payment key");
    }

    private JSONObject createBillingData(User user) {
        Address address = user.getAddresses().stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElseGet(() -> user.getAddresses().isEmpty() ? null : user.getAddresses().get(0));

        JSONObject billing = new JSONObject()
                .put("email", user.getEmail())
                .put("first_name", user.getFirst_name())
                .put("last_name", user.getLast_name())
                .put("phone_number", user.getPhone() != null ? user.getPhone() : "N/A")
                .put("apartment", "N/A")
                .put("country", "EG");

        if (address != null) {
            billing.put("street", address.getStreet())
                .put("building", address.getBuilding())
                .put("floor", "1")
                .put("city", address.getCity())
                .put("state", address.getState());
        } else {
            billing.put("street", "Unknown Street")
                .put("building", "0")
                .put("floor", "1")
                .put("city", "Unknown City")
                .put("state", "Unknown");
        }

        return billing;
    }


    private JSONObject createShopBillingData(Shop shop) {
        ShopAddress address = shop.getAddresses().stream()
                .filter(ShopAddress::isDefault)
                .findFirst()
                .orElseGet(() -> shop.getAddresses().isEmpty() ? null : shop.getAddresses().get(0));

        JSONObject billing = new JSONObject()
                .put("email", shop.getEmail())
                .put("first_name", shop.getName())
                .put("last_name", "Shop")
                .put("phone_number", shop.getPhone() != null ? shop.getPhone() : "N/A")
                .put("apartment", "N/A")
                .put("country", "EG");

        if (address != null) {
            billing.put("street", address.getStreet())
                .put("building", address.getBuilding())
                .put("floor", "1")
                .put("city", address.getCity())
                .put("state", address.getState());
        } else {
            billing.put("street", shop.getName())
                .put("building", "0")
                .put("floor", "1")
                .put("city", "Cairo")
                .put("state", "Cairo");
        }

        return billing;
    }


    private String generatePaymentIframeUrl(String paymentToken) {
        return "https://accept.paymob.com/api/acceptance/iframes/" + iframeId + "?payment_token=" + paymentToken;
    }

    // HMAC Validation
    private void validateHmacSignature(Map<String, Object> payload, HttpServletRequest request) {
        String receivedHmac = request.getParameter("hmac");
        if (receivedHmac == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "HMAC missing");
        }

        String concatenatedValues = concatenateValues(payload, hmacKeys());
        String calculatedHmac = calculateHmac(concatenatedValues, hmacSecretKey);
        log.debug("HMAC - Received: {}, Calculated: {}", receivedHmac, calculatedHmac);

        if (!receivedHmac.equals(calculatedHmac)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid HMAC signature");
        }
    }

    private void processPaymentCallback(Map<String, Object> payload) {
        String success = (String) payload.get("success");
        String paymobOrderId = (String) payload.get("order_id");

        paymentRepository.findByTransactionId(paymobOrderId).ifPresent(payment -> {
            payment.setPaymentStatus("true".equals(success) ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
            paymentRepository.save(payment);
            if ("true".equals(success)) handlePaymentCompletion(payment);
        });
    }

    private List<String> hmacKeys() {
        return Arrays.asList("amount_cents", "created_at", "currency", "error_occured", "has_parent_transaction",
                "id", "integration_id", "is_3d_secure", "is_auth", "is_capture", "is_refunded",
                "is_standalone_payment", "is_voided", "order.id", "owner", "pending",
                "source_data.pan", "source_data.sub_type", "source_data.type", "success");
    }

    private String concatenateValues(Map<String, Object> payload, List<String> keys) {
        Map<String, Object> obj = (Map<String, Object>) payload.get("obj");
        if (obj == null) throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid payload");
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String[] parts = key.split("\\.");
            Object value = parts.length == 1 ? obj.get(parts[0]) : getNestedValue(obj, parts);
            sb.append(value != null ? value.toString() : "");
        }
        return sb.toString();
    }

    private Object getNestedValue(Map<String, Object> obj, String[] parts) {
        Object current = obj;
        for (String part : parts) {
            if (current instanceof Map) current = ((Map<?, ?>) current).get(part);
            else return null;
        }
        return current;
    }

    private String calculateHmac(String data, String secretKey) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(keySpec);
            byte[] macData = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(macData);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "HMAC calculation failed");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) result.append(String.format("%02x", b));
        return result.toString();
    }

    // Completion Logic
    private void handlePaymentCompletion(Payment payment) {
        if (payment.getPaymentType() == PaymentType.SUBSCRIPTION_PAYMENT) {
            createSubscriptionFromPayment(payment);
        }
    }

    private void createSubscriptionFromPayment(Payment payment) {
        Subscription subscription = new Subscription();
        subscription.setShopId(payment.getShop().getId());
        subscription.setPaymentId(payment.getId());
        subscription.setMonths((int) (payment.getAmount().divide(BigDecimal.valueOf(1000), 0, BigDecimal.ROUND_DOWN).longValue()));
        subscription.setType(SubscriptionType.COMMISSION);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(subscription.getMonths()));
        subscriptionRepository.save(subscription);

        Shop shop = payment.getShop();
        shop.setActivate(true);
        shop.setSubscriptionMonths(subscription.getMonths());
        shop.setSubscriptionStartDate(subscription.getStartDate());
        shop.setSubscriptionEndDate(subscription.getEndDate());
        shopRepository.save(shop);
    }

    // Support Methods
    private boolean isSupportedPaymentType(PaymentType paymentType) {
        return paymentType == PaymentType.ORDER_PAYMENT ||
               paymentType == PaymentType.REPAIR_PAYMENT ||
               paymentType == PaymentType.SUBSCRIPTION_PAYMENT;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // DTO Converters
    private PaymentDto toDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentType(payment.getPaymentType());
        dto.setPaymentReference(payment.getPaymentReference());
        dto.setTransactionId(payment.getTransactionId());
        dto.setDetails(payment.getDetails());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }

    private AdminPaymentDto toAdminDto(Payment payment) {
        AdminPaymentDto dto = new AdminPaymentDto();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentType(payment.getPaymentType());
        dto.setPaymentReference(payment.getPaymentReference());
        dto.setTransactionId(payment.getTransactionId());
        dto.setDetails(payment.getDetails());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        dto.setUserId(Optional.ofNullable(payment.getUser()).map(User::getId).orElse(null));
        dto.setShopId(Optional.ofNullable(payment.getShop()).map(Shop::getId).orElse(null));
        return dto;
    }

    private SubscriptionResponseDto toSubscriptionDto(Subscription subscription) {
        SubscriptionResponseDto dto = new SubscriptionResponseDto();
        dto.setId(subscription.getId());
        dto.setShopId(subscription.getShopId());
        dto.setMonths(subscription.getMonths());
        dto.setBaseAmount(BigDecimal.valueOf(1000));
        dto.setTotalAmount(BigDecimal.valueOf(1000).multiply(BigDecimal.valueOf(subscription.getMonths())));
        dto.setType(subscription.getType());
        dto.setStartDate(subscription.getStartDate());
        dto.setEndDate(subscription.getEndDate());
        dto.setCreatedAt(subscription.getCreatedAt());
        return dto;
    }
}