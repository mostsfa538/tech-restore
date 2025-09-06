package com.techRestore.tech.restore.services.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.techRestore.tech.restore.dto.payment.PaymentInitiationDto;
import com.techRestore.tech.restore.exception.CustomException;
import com.techRestore.tech.restore.model.entities.Address;
import com.techRestore.tech.restore.model.entities.RepairPayment;
import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.model.enums.PaymentMethod;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.repository.RepairPaymentRepository;
import com.techRestore.tech.restore.repository.RepairRequestRepository;
import com.techRestore.tech.restore.repository.UserRepository;

import org.json.JSONObject;
import org.json.JSONArray;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepairPaymentService {

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

    private final RepairPaymentRepository repairPaymentRepository;
    private final ProcessingService processingService;
    private final UserRepository userRepository;
    private final RepairRequestRepository orderRepository;

    @Transactional
    public PaymentInitiationDto initiateCardPayment(UUID repairRequestId, UUID userId) {
        try {

           RepairRequest repair = orderRepository.findById(repairRequestId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Repair Request not found"));
            RepairPayment payment = new RepairPayment();
            payment.setRepairRequestId(repairRequestId);
            payment.setUserId(userId);
            payment.setAmount(repair.getPrice());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

            repairPaymentRepository.save(payment);

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

    private String createPaymobOrder(String authToken, RepairPayment payment) {
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
            payment.setTransactionId(paymobOrderId);
            repairPaymentRepository.save(payment);
            return paymobOrderId;
        } else {
            throw new RuntimeException("Failed to create Paymob order: " + response.getStatusCode());
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
            throw new RuntimeException("Failed to authenticate with Paymob: " + response.getStatusCode());
        }
    }
}