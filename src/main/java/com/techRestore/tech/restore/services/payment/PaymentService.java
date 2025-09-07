package com.techRestore.tech.restore.services.payment;

import com.techRestore.tech.restore.dto.payment.PaymentInitiationDto;
import com.techRestore.tech.restore.exception.CustomException;
import com.techRestore.tech.restore.model.entities.Address;
import com.techRestore.tech.restore.model.entities.Order;
import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.model.entities.Payment;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.model.enums.PaymentMethod;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.model.enums.PaymentType;
import com.techRestore.tech.restore.repository.PaymentRepository;
import com.techRestore.tech.restore.repository.OrderRepository;
import com.techRestore.tech.restore.repository.RepairRequestRepository;
import com.techRestore.tech.restore.repository.UserRepository;
import com.techRestore.tech.restore.repository.ShopRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${paymob.iframeId}")
    private String iframeId;

    @Value("${paymob.apiKey}")
    private String paymobApiKey;

    @Value("${paymob.authUrl}")
    private String paymobAuthUrl;

    @Value("${paymob.orderUrl}")
    private String paymobOrderUrl;

    @Value("${paymob.paymentKeyUrl}")
    private String paymobPaymentKeyUrl;

    @Value("${paymob.cardIntegrationId}")
    private int cardIntegrationId;

    @Value("${paymob.hmacSecret}")
    private String hmacSecretKey;

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public PaymentInitiationDto initiateCardPayment(UUID referenceId, UUID userId, PaymentType paymentType) {
        try {
            if (!isSupportedPaymentType(paymentType)) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Unsupported payment type: " + paymentType);
            }

            BigDecimal amount = getPaymentAmount(referenceId, paymentType);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found: " + userId));

            Payment payment = new Payment();
            setPaymentReferenceIds(payment, referenceId, paymentType);
            payment.setUser(user);
            payment.setAmount(amount);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            payment.setPaymentType(paymentType);

            if (payment.getPaymentType() == null) {
                throw new IllegalStateException("Payment type cannot be null");
            }
            paymentRepository.save(payment);

            String authToken = getAuthToken();
            String paymobOrderId = createPaymobOrder(authToken, payment);
            String paymentToken = generatePaymentKey(authToken, paymobOrderId, payment.getAmount(), cardIntegrationId,
                    userId);
            String paymentLink = generatePaymentIframeUrl(paymentToken);

            PaymentInitiationDto dto = new PaymentInitiationDto();
            dto.setPaymentURL(paymentLink);
            return dto;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error generating Paymob card payment link: " + e.getMessage());
        }
    }

    @Transactional
    public void initiateCashPayment(UUID referenceId, UUID userId, PaymentType paymentType) {
        try {
            if (!isSupportedPaymentType(paymentType)) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Unsupported payment type: " + paymentType);
            }

            BigDecimal amount = getPaymentAmount(referenceId, paymentType);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found: " + userId));

            Payment payment = new Payment();
            setPaymentReferenceIds(payment, referenceId, paymentType);
            payment.setUser(user);
            payment.setAmount(amount);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setPaymentMethod(PaymentMethod.CASH);
            payment.setPaymentType(paymentType);
            payment.setPaymentReference("CASH-" + UUID.randomUUID().toString());

            if (payment.getPaymentType() == null) {
                throw new IllegalStateException("Payment type cannot be null");
            }

            paymentRepository.save(payment);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error initiating cash payment: " + e.getMessage());
        }
    }

    @Transactional
    public void updateCashPaymentStatus(UUID paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Payment not found: " + paymentId));
        if (payment.getPaymentMethod() != PaymentMethod.CASH) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Payment is not a cash payment: " + paymentId);
        }
        payment.setPaymentStatus(status);
        if (status == PaymentStatus.COMPLETED) {
            payment.setPaidAt(LocalDateTime.now());
        }
        paymentRepository.save(payment);
    }

    @Transactional
    public void updatePaymentStatusOnOrderDelivered(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(
                        () -> new CustomException(HttpStatus.NOT_FOUND, "Payment not found for order: " + orderId));
        if (payment.getPaymentMethod() == PaymentMethod.CASH &&
                payment.getPaymentStatus() == PaymentStatus.PENDING &&
                order.getStatus().name().equals("DELIVERED")) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }
    }

    @Transactional
    public void processPaymentCallback(Map<String, Object> payload) {
        String success = (String) payload.get("success");
        String paymobOrderId = (String) payload.get("order_id");

        paymentRepository.findByTransactionId(paymobOrderId).ifPresent(payment -> {
            payment.setPaymentStatus("true".equals(success) ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
            paymentRepository.save(payment);
        });

        if (paymentRepository.findByTransactionId(paymobOrderId).isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Payment not found for transaction ID: " + paymobOrderId);
        }
    }

    public void handlePaymentCallback(Map<String, Object> payload, HttpServletRequest request) {
        String receivedHmac = request.getParameter("hmac");
        if (receivedHmac == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "HMAC is missing in the request");
        }

        List<String> hmacKeys = Arrays.asList(
                "amount_cents", "created_at", "currency", "error_occured", "has_parent_transaction",
                "id", "integration_id", "is_3d_secure", "is_auth", "is_capture", "is_refunded",
                "is_standalone_payment", "is_voided", "order.id", "owner", "pending",
                "source_data.pan", "source_data.sub_type", "source_data.type", "success");

        String concatenatedValues = concatenateValues(payload, hmacKeys);
        String calculatedHmac = calculateHmac(concatenatedValues, hmacSecretKey);

        if (!receivedHmac.equals(calculatedHmac)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid HMAC signature");
        }

        processPaymentCallback(payload);
    }

    private String generatePaymentKey(String authToken, String orderId, BigDecimal amount, int integrationId,
            UUID userId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        Address address = user.getAddresses() != null && !user.getAddresses().isEmpty()
                ? user.getAddresses().stream()
                        .filter(Address::isDefault)
                        .findFirst()
                        .orElse(user.getAddresses().get(0))
                : null;

        JSONObject billingData = new JSONObject();
        billingData.put("email", user.getEmail());
        billingData.put("first_name", user.getFirst_name());
        billingData.put("last_name", user.getLast_name());
        billingData.put("phone_number", user.getPhone());
        billingData.put("apartment", address != null && address.getNotes() != null ? address.getNotes() : "N/A");
        billingData.put("street", address != null ? address.getStreet() : "Unknown Street");
        billingData.put("building", address != null ? address.getBuilding() : "0");
        billingData.put("floor", "1");
        billingData.put("city", address != null ? address.getCity() : "Unknown City");
        billingData.put("country", "EG");
        billingData.put("state", address != null ? address.getState() : "Unknown");

        JSONObject requestBody = new JSONObject();
        requestBody.put("auth_token", authToken);
        requestBody.put("amount_cents", amount.multiply(BigDecimal.valueOf(100)).intValue());
        requestBody.put("expiration", 3600);
        requestBody.put("order_id", orderId);
        requestBody.put("billing_data", billingData);
        requestBody.put("currency", "EGP");
        requestBody.put("integration_id", integrationId);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(paymobPaymentKeyUrl, HttpMethod.POST, entity,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse.getString("token");
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "Failed to generate payment key: " + response.getStatusCode());
        }
    }

    private String createPaymobOrder(String authToken, Payment payment) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("auth_token", authToken);
        requestBody.put("delivery_needed", false);
        requestBody.put("amount_cents", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
        requestBody.put("currency", "EGP");
        requestBody.put("items", new JSONArray());

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(paymobOrderUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            String paymobOrderId = String.valueOf(jsonResponse.getInt("id"));
            payment.setTransactionId(paymobOrderId);
            paymentRepository.save(payment);
            return paymobOrderId;
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "Failed to create Paymob order: " + response.getStatusCode());
        }
    }

    private String generatePaymentIframeUrl(String paymentToken) {
        return "https://accept.paymob.com/api/acceptance/iframes/" + iframeId + "?payment_token=" + paymentToken;
    }

    private String getAuthToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("api_key", paymobApiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(paymobAuthUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse.getString("token");
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "Failed to authenticate with Paymob: " + response.getStatusCode());
        }
    }

    private String concatenateValues(Map<String, Object> payload, List<String> hmacKeys) {
        Map<String, Object> obj = (Map<String, Object>) payload.get("obj");
        if (obj == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid payload: missing 'obj' key");
        }

        StringBuilder concatenated = new StringBuilder();
        for (String key : hmacKeys) {
            String[] parts = key.split("\\.");
            Object value = parts.length == 1 ? obj.get(parts[0]) : getNestedValue(obj, parts);
            concatenated.append(value != null ? value.toString() : "");
        }
        return concatenated.toString();
    }

    private Object getNestedValue(Map<String, Object> obj, String[] parts) {
        Object current = obj;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    private String calculateHmac(String data, String secretKey) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(keySpec);
            byte[] macData = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : macData) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error calculating HMAC: " + e.getMessage());
        }
    }

    private boolean isSupportedPaymentType(PaymentType paymentType) {
        return paymentType == PaymentType.ORDER_PAYMENT || paymentType == PaymentType.REPAIR_PAYMENT;
    }

    private BigDecimal getPaymentAmount(UUID referenceId, PaymentType paymentType) {
        if (paymentType == PaymentType.ORDER_PAYMENT) {
            Order order = orderRepository.findById(referenceId)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Order not found: " + referenceId));
            return order.getTotalPrice();
        } else if (paymentType == PaymentType.REPAIR_PAYMENT) {
            RepairRequest repair = repairRequestRepository.findById(referenceId)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                            "Repair Request not found: " + referenceId));
            return repair.getPrice();
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Unsupported payment type: " + paymentType);
        }
    }

    private void setPaymentReferenceIds(Payment payment, UUID referenceId, PaymentType paymentType) {
        switch (paymentType) {
            case ORDER_PAYMENT:
            case REFUND_ORDER:
                payment.setOrderId(referenceId);
                break;
            case REPAIR_PAYMENT:
            case REFUND_REPAIR:
                payment.setRepairRequestId(referenceId);
                break;
            case SUBSCRIPTION_PAYMENT:
                payment.setSubscriptionId(referenceId);
                break;
            case COMMISSION_PAYMENT:
            case WITHDRAWAL:
                Shop shop = shopRepository.findById(referenceId)
                        .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Shop not found: " + referenceId));
                payment.setShop(shop);
                break;
            default:
                throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid payment type: " + paymentType);
        }
    }
}