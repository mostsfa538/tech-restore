package com.techRestore.tech.restore.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@techrestore.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String verificationToken, String userType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Email Verification - Tech Restore");
            
            String verificationUrl = baseUrl + "/api/auth/" + userType + "/verify-email?token=" + verificationToken;
            String emailBody = String.format(
                "Dear User,\n\n" +
                "Thank you for registering with Tech Restore!\n\n" +
                "To complete your registration, please verify your email address by clicking the link below:\n\n" +
                "%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Tech Restore Team",
                verificationUrl
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendWelcomeEmail(String to, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to Tech Restore!");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Welcome to Tech Restore! Your email has been successfully verified.\n\n" +
                "You can now enjoy full access to our platform.\n\n" +
                "Best regards,\n" +
                "Tech Restore Team",
                name
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Welcome email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }
}