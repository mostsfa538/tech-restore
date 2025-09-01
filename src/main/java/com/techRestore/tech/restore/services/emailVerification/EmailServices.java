package com.techRestore.tech.restore.services.emailVerification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServices {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(User user) {
        try {
            if (user.getOptCode() == null || user.getOptCode().isEmpty()) {
                throw new RuntimeException("OTP not generated for user");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Email Verification - TechRestore");

            String emailBody = createSimpleEmailContent(user);
            message.setText(emailBody);

            mailSender.send(message);

        } catch (MailException e) {
            throw new RuntimeException("Failed to send verification email", e);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred while sending email", e);
        }
    }

    private String createSimpleEmailContent(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        String expiryTime = user.getOtpExpiry().format(formatter);

        String lastName = user.getLast_name() != null ? user.getLast_name() : "";

        return String.format("""
                Hello %s %s,

                Thank you for registering with TechRestore!

                Your verification code is: %s

                This code will expire on: %s

                Please enter this code to verify your email address.

                If you didn't request this verification, please ignore this email.

                Best regards,
                The Tech Restore Team

                ---
                This is an automated message. Please do not reply to this email.
                """,
                user.getFirst_name(),
                lastName,
                user.getOptCode(),
                expiryTime);
    }

    public boolean verifyOtp(String email, String otp) {
        if (email == null || otp == null) {
            throw new RuntimeException("Email and OTP cannot be null");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("Email is not exist");
        }

        if (!otp.equals(user.getOptCode())) {
            return false;
        }

        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            return false;
        }

        user.setActivate(true);
        user.setOptCode(null);
        user.setOtpExpiry(null);

        userRepository.save(user);
        return true;
    }

    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("Email is not exist");
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setOptCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);
        sendOtpEmail(user);
    }
}
