package com.techRestore.tech.restore.common.services.emailVerification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.exception.ExpiredOtpException;
import com.techRestore.tech.restore.common.exception.InvalidOtpException;
import com.techRestore.tech.restore.common.interfaces.OtpVerifiable;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServices {
  private final JavaMailSender mailSender;
  private final EntityRepositoryService entityRepositoryService;
  private final PasswordEncoder passwordEncoder;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Async
  public void generateAndSendOtp(String email) {
    OtpVerifiable entity = entityRepositoryService.findByEmail(email);

    String otp = String.valueOf(new Random().nextInt(900000) + 100000);
    entity.setOptCode(otp);
    entity.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

    entityRepositoryService.save(entity);

    sendOtpEmail(email);
  }

  public void sendOtpEmail(String email) {
    try {
      OtpVerifiable entity = entityRepositoryService.findByEmail(email);

      if (entity.getOptCode() == null || entity.getOptCode().isEmpty()) {
        throw new IllegalArgumentException("OTP not generated for " + entity.getEntityType());
      }

      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(entity.getEmail());
      helper.setSubject("Email Verification - TechRestore");

      String emailBody = createHtmlEmailContent(entity);
      helper.setText(emailBody, true);

      mailSender.send(mimeMessage);

    } catch (MailException e) {
      throw new IllegalArgumentException("Failed to send verification email", e);
    } catch (Exception e) {
      throw new IllegalArgumentException("Unexpected error occurred while sending email", e);
    }
  }

  private String createHtmlEmailContent(OtpVerifiable entity) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
    String expiryTime = entity.getOtpExpiry().format(formatter);

    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Email Verification</title>
          <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">
          <style>
            body {
              font-family: 'Poppins', Arial, sans-serif;
              background-color: white;
              margin: 0;
              padding: 0;
              color: #333;
            }
            .container {
              max-width: 600px;
              margin: 40px auto;
              background: #ffffff;
              border-radius: 12px;
              box-shadow: 0 4px 12px rgba(0,0,0,0.15);
              overflow: hidden;
            }
            .header {
              background: linear-gradient(135deg, #2563eb, #4f46e5);
              padding: 20px;
              text-align: center;
              color: #ffffff;
            }
            .header h1 {
              margin: 0;
              font-size: 24px;
            }
            .content {
              padding: 30px;
              line-height: 1.6;
            }
            .code {
              display: inline-block;
              background: #f1f5f9;
              padding: 12px 24px;
              border-radius: 8px;
              font-size: 20px;
              font-weight: bold;
              letter-spacing: 3px;
              color: #1e3a8a;
              margin: 20px 0;
            }
            .footer {
              text-align: center;
              font-size: 12px;
              color: #6b7280;
              padding: 20px;
              background: #f9fafb;
            }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h1>TechRestore</h1>
            </div>
            <div class="content">
              <p>Hello <strong>%s</strong>,</p>
              <p>Thank you for registering with <strong>TechRestore</strong> as a %s!</p>
              <p>Your verification code is:</p>
              <div class="code">%s</div>
              <p>This code will expire on: <strong>%s</strong></p>
              <p>Please enter this code to verify your email address.</p>
              <p>If you didn't request this verification, please ignore this email.</p>
              <p>Best regards,<br>
              <strong>The TechRestore Team</strong></p>
            </div>
          </div>
        </body>
        </html>
        """.formatted(
        entity.getDisplayName(),
        entity.getEntityType(),
        entity.getOptCode(),
        expiryTime);
  }

  public void verifyOtp(String email, String otp) {
    if (email == null || otp == null) {
      throw new IllegalArgumentException("Email and OTP cannot be null");
    }

    OtpVerifiable entity = entityRepositoryService.findByEmail(email);

    if (!otp.equals(entity.getOptCode())) {
      throw new InvalidOtpException("OTP code is not correct");
    }

    if (entity.getOtpExpiry() == null || LocalDateTime.now().isAfter(entity.getOtpExpiry())) {
      throw new ExpiredOtpException("Code is expired");
    }

    entity.setActivate(true);
    entity.setOptCode(null);
    entity.setOtpExpiry(null);

    entityRepositoryService.save(entity);
  }

  public void resendOtp(String email) {
    generateAndSendOtp(email);
  }

  public void forgotPassword(String email) {
    generateAndSendOtp(email);
  }

  public void resetPassword(String email, String otp, String newPassword, String confirmPassword) {
    OtpVerifiable entity = entityRepositoryService.findByEmail(email);

    if (entity.getOptCode() == null || !entity.getOptCode().equals(otp)) {
      throw new InvalidOtpException("Invalid OTP");
    }

    if (entity.getOtpExpiry() == null || LocalDateTime.now().isAfter(entity.getOtpExpiry())) {
      throw new ExpiredOtpException("OTP expired");
    }

    if (!newPassword.equals(confirmPassword)) {
      throw new IllegalArgumentException("Passwords do not match");
    }

    entity.setPassword(passwordEncoder.encode(newPassword));
    entity.setOptCode(null);
    entity.setOtpExpiry(null);

    entityRepositoryService.save(entity);
  }
}