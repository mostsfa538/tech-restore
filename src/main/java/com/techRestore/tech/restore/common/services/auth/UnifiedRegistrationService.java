package com.techRestore.tech.restore.common.services.auth;

import com.techRestore.tech.restore.common.interfaces.RegistrationStrategy;
import com.techRestore.tech.restore.common.services.emailVerification.EmailServices;
import com.techRestore.tech.restore.common.utils.EmailValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedRegistrationService {

    private final EmailValidatorService emailValidatorService;
    private final EmailServices emailService;

    /**
     * Unified registration method that handles User, Shop, and Delivery
     * registration
     * 
     * @param registrationData the registration request data
     * @param strategy         the strategy to handle specific entity type
     * @param <T>              the entity type (User, Shop, Delivery)
     * @param <R>              the registration data type
     * @return success message or entity ID
     */
    @Transactional
    public <T, R> String register(R registrationData, RegistrationStrategy<T, R> strategy) {
        String email = strategy.getEmail(registrationData);

        emailValidatorService.validateUniqueEmail(email);

        try {
            T entity = strategy.createEntity(registrationData);

            T savedEntity = strategy.saveEntity(entity);

            emailService.generateAndSendOtp(email);

            String successMessage = strategy.getSuccessMessage(savedEntity);

            log.info("Registration successful for email: {} - Type: {}",
                    email, entity.getClass().getSimpleName());

            return successMessage;

        } catch (Exception e) {
            log.error("Error during registration for email: {} - Error: {}", email, e.getMessage());
            throw new IllegalArgumentException("Failed to create " +
                    registrationData.getClass().getSimpleName().replace("Registration", "") +
                    ": " + e.getMessage());
        }
    }
}