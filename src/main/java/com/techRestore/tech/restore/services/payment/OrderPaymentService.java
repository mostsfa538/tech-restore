package com.techRestore.tech.restore.services.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.dto.payment.PaymentInitiationDto;
import com.techRestore.tech.restore.exception.CustomException;
import com.techRestore.tech.restore.model.entities.Address;
import com.techRestore.tech.restore.model.entities.Order;
import com.techRestore.tech.restore.model.entities.OrderPayment;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.model.enums.PaymentMethod;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.repository.OrderPaymentRepository;
import com.techRestore.tech.restore.repository.OrderRepository;
import com.techRestore.tech.restore.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


@Service
@RequiredArgsConstructor
public class OrderPaymentService {

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


  private final OrderPaymentRepository orderPaymentRepository;
  private final ProcessingService processingService;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;

  @Transactional
    public PaymentInitiationDto initiateCardPayment(UUID orderId, UUID userId) {
        try {
            // if (orderPaymentRepository.existsByOrderIdAndUserIdAndStatus(orderId, userId, PaymentStatus.COMPLETED)) {
            //     throw new CustomException(HttpStatus.BAD_REQUEST, "User has already paid for this order.");
            // }

            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Order not found"));

            BigDecimal price = order.getTotalPrice();
            OrderPayment payment = new OrderPayment();
            payment.setOrderId(orderId);
            payment.setUserId(userId);
            payment.setAmount(price);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

            orderPaymentRepository.save(payment);

            String authToken = getAuthToken();

            String paymobOrderId = createPaymobOrder(authToken, payment);

            String paymentToken = generatePaymentKey(authToken, paymobOrderId, payment.getAmount(), cardIntegrationId, userId);

            String paymentLink = generatePaymentIframeUrl(paymentToken);

            PaymentInitiationDto dto = new PaymentInitiationDto();
            dto.setPaymentURL(paymentLink);
            return dto;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating Paymob card payment link: " + e.getMessage());
        }
    }

    private String generatePaymentKey(String authToken, String orderId, BigDecimal amount, int integrationId, UUID userId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Address address = null;
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            address = user.getAddresses().stream()
                    .filter(Address::isDefault)
                    .findFirst()
                    .orElse(user.getAddresses().get(0)); 
        }

        JSONObject billingData = new JSONObject();
        billingData.put("email", user.getEmail());
        billingData.put("first_name", user.getFirst_name());
        billingData.put("last_name", user.getLast_name());
        billingData.put("phone_number", user.getPhone());

        if (address != null) {
            billingData.put("apartment", address.getNotes() != null ? address.getNotes() : "N/A");
            billingData.put("street", address.getStreet());
            billingData.put("building", address.getBuilding());
            billingData.put("floor", "1");
            billingData.put("city", address.getCity());
            billingData.put("country", "EG");
            billingData.put("state", address.getState());
        } else {
            billingData.put("apartment", "N/A");
            billingData.put("street", "Unknown Street");
            billingData.put("building", "0");
            billingData.put("floor", "0");
            billingData.put("city", "Unknown City");
            billingData.put("country", "EG");
            billingData.put("state", "Unknown");
        }


        JSONObject requestBody = new JSONObject();
        requestBody.put("auth_token", authToken);
        requestBody.put("amount_cents", amount.multiply(BigDecimal.valueOf(100)).intValue());
        requestBody.put("expiration", 3600);
        requestBody.put("order_id", orderId);
        requestBody.put("billing_data", billingData);
        requestBody.put("currency", "EGP");
        requestBody.put("integration_id", integrationId);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(paymobPaymentKeyUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse.getString("token");
        } else {
            throw new RuntimeException("Failed to generate payment key: " + response.getStatusCode());
        }
    }

     private String createPaymobOrder(String authToken, OrderPayment payment) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("auth_token", authToken);
        requestBody.put("delivery_needed", "false");
        requestBody.put("amount_cents", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); 
        requestBody.put("currency", "EGP");
        requestBody.put("items", new JSONArray());

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(paymobOrderUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());

            String paymobOrderId = String.valueOf(jsonResponse.getInt("id"));
            payment.setPaymentId(paymobOrderId);
            orderPaymentRepository.save(payment);

            return paymobOrderId;
        } else {
            throw new RuntimeException("Failed to create Paymob order: " + response.getStatusCode());
        }
    }

    private String generatePaymentIframeUrl(String paymentToken) {
        return "https://accept.paymob.com/api/acceptance/iframes/" + iframeId + "?payment_token=" + paymentToken;
    }

    public void handlePaymentCallback(Map<String, Object> payload, HttpServletRequest request) {
        
        String receivedHmac = request.getParameter("hmac");
        System.out.println("Received HMAC: " + receivedHmac);
        if (receivedHmac == null) {
            throw new CustomException(HttpStatus.NOT_FOUND,"HMAC is missing in the request");
        }

        List<String> hmacKeys = Arrays.asList(
                "amount_cents",
                "created_at",
                "currency",
                "error_occured",
                "has_parent_transaction",
                "id",
                "integration_id",
                "is_3d_secure",
                "is_auth",
                "is_capture",
                "is_refunded",
                "is_standalone_payment",
                "is_voided",
                "order.id",
                "owner",
                "pending",
                "source_data.pan",
                "source_data.sub_type",
                "source_data.type",
                "success"
        );

        String concatenatedValues = concatenateValues(payload, hmacKeys);

        String calculatedHmac = calculateHmac(concatenatedValues, hmacSecretKey);
        System.out.println("Calculated HMAC: " + calculatedHmac);

        if (!receivedHmac.equals(calculatedHmac)) {
            throw new CustomException(HttpStatus.NOT_FOUND,"Invalid HMAC signature");
        }

        processingService.processPaymentCallback(payload);
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
            throw new RuntimeException("Failed to authenticate with Paymob: " + response.getStatusCode());
        }
    }



    private String concatenateValues(Map<String, Object> payload, List<String> hmacKeys) {
        Map<String, Object> obj = (Map<String, Object>) payload.get("obj");
        if (obj == null) {
            throw new CustomException(HttpStatus.NOT_FOUND,"Invalid payload: missing 'obj' key");
        }

        StringBuilder concatenated = new StringBuilder();

        for (String key : hmacKeys) {
            String[] parts = key.split("\\.");
            Object value = null;

            if (parts.length == 1) {
                value = obj.get(parts[0]);
            } else if (parts.length == 2) {
                Object nestedObj = obj.get(parts[0]);
                if (nestedObj instanceof Map) {
                    value = ((Map<?, ?>) nestedObj).get(parts[1]);
                }
            }

            if (value == null) {
                value = "";
            } else if (value instanceof Boolean) {
                value = value.toString();
            } else if (value instanceof Map || value instanceof List) {
                value = value.toString();
            }

            concatenated.append(value);
        }

        return concatenated.toString();
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
            throw new RuntimeException("Error calculating HMAC", e);
        }
    }
  
  
}
