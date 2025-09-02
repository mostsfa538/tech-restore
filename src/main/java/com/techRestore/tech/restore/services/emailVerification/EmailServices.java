package com.techRestore.tech.restore.services.emailVerification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import com.techRestore.tech.restore.exception.ExpiredOtpException;
import com.techRestore.tech.restore.exception.InvalidOtpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String email) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new NotFoundException("Email is not exist");
            }
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

    public void verifyOtp(String email, String otp) {
        if (email == null || otp == null) {
            throw new RuntimeException("Email and OTP cannot be null");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("Email is not exist");
        }

        if (!otp.equals(user.getOptCode())) {
            throw new InvalidOtpException("Opt code is not correct");
        }

        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new ExpiredOtpException("Code is expired");
        }

        user.setActivate(true);
        user.setOptCode(null);
        user.setOtpExpiry(null);

        userRepository.save(user);
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
        sendOtpEmail(user.getEmail());
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("Email does not exist");
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setOptCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);
        sendOtpEmail(user.getEmail());
    }

    public void resetPassword(String email, String otp, String newPassword, String confirmPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("Email does not exist");
        }

        if (user.getOptCode() == null || !user.getOptCode().equals(otp)) {
            throw new InvalidOtpException("Invalid OTP");
        }

        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new ExpiredOtpException("OTP expired");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOptCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

}
