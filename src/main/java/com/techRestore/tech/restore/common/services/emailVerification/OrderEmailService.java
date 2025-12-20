package com.techRestore.tech.restore.common.services.emailVerification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderEmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOrderShippedEmail(String customerEmail, String customerName,
                                      String orderId, String trackingNumber,
                                      String carrier, LocalDateTime shippedDate) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject("Your Order Has Been Shipped - TechRestore");

            String emailBody = createOrderShippedHtml(customerName, orderId,
                    trackingNumber, carrier, shippedDate);
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MailException e) {
            throw new IllegalArgumentException("Failed to send order shipped email", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unexpected error occurred while sending email", e);
        }
    }

    @Async
    public void sendRepairCompletedEmail(String customerEmail, String customerName,
                                         String repairRequestId, String deviceType,
                                         String issueDescription, LocalDateTime completedDate,
                                         String shopName, String pickupInstructions) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject("Your Repair is Complete - TechRestore");

            String emailBody = createRepairCompletedHtml(customerName, repairRequestId,
                    deviceType, issueDescription,
                    completedDate, shopName, pickupInstructions);
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MailException e) {
            throw new IllegalArgumentException("Failed to send repair completed email", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unexpected error occurred while sending email", e);
        }
    }

    private String createOrderShippedHtml(String customerName, String orderId,
                                          String trackingNumber, String carrier,
                                          LocalDateTime shippedDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        String formattedDate = shippedDate.format(formatter);

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Order Shipped</title>
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
              background: linear-gradient(135deg, #10b981, #059669);
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
            .info-box {
              background: #f0fdf4;
              border-left: 4px solid #10b981;
              padding: 15px;
              margin: 20px 0;
              border-radius: 4px;
            }
            .info-box p {
              margin: 8px 0;
            }
            .tracking {
              display: inline-block;
              background: #f1f5f9;
              padding: 12px 24px;
              border-radius: 8px;
              font-size: 16px;
              font-weight: bold;
              letter-spacing: 1px;
              color: #1e3a8a;
              margin: 10px 0;
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
              <h1>📦 Order Shipped!</h1>
            </div>
            <div class="content">
              <p>Hello <strong>%s</strong>,</p>
              <p>Great news! Your order has been shipped and is on its way to you.</p>
              
              <div class="info-box">
                <p><strong>Order ID:</strong> %s</p>
                <p><strong>Shipped Date:</strong> %s</p>
                <p><strong>Carrier:</strong> %s</p>
              </div>

              <p><strong>Tracking Number:</strong></p>
              <div class="tracking">%s</div>

              <p>You can use this tracking number to monitor your shipment's progress on the carrier's website.</p>
              
              <p>If you have any questions about your order, please don't hesitate to contact us.</p>

              <p>Thank you for choosing TechRestore!</p>

              <p>Best regards,<br>
              <strong>The TechRestore Team</strong></p>
            </div>
            <div class="footer">
              <p>&copy; 2024 TechRestore. All rights reserved.</p>
            </div>
          </div>
        </body>
        </html>
        """.formatted(customerName, orderId, formattedDate, carrier, trackingNumber);
    }

    private String createRepairCompletedHtml(String customerName, String repairRequestId,
                                             String deviceType, String issueDescription,
                                             LocalDateTime completedDate, String shopName,
                                             String pickupInstructions) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        String formattedDate = completedDate.format(formatter);

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Repair Completed</title>
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
              background: linear-gradient(135deg, #8b5cf6, #7c3aed);
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
            .info-box {
              background: #faf5ff;
              border-left: 4px solid #8b5cf6;
              padding: 15px;
              margin: 20px 0;
              border-radius: 4px;
            }
            .info-box p {
              margin: 8px 0;
            }
            .pickup-box {
              background: #fef3c7;
              border: 2px solid #f59e0b;
              padding: 15px;
              margin: 20px 0;
              border-radius: 8px;
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
              <h1>✅ Repair Completed!</h1>
            </div>
            <div class="content">
              <p>Hello <strong>%s</strong>,</p>
              <p>Good news! Your device repair has been completed and is ready for pickup.</p>
              
              <div class="info-box">
                <p><strong>Repair Request ID:</strong> %s</p>
                <p><strong>Device:</strong> %s</p>
                <p><strong>Issue:</strong> %s</p>
                <p><strong>Completed Date:</strong> %s</p>
                <p><strong>Repair Shop:</strong> %s</p>
              </div>

              <div class="pickup-box">
                <p><strong>📍 Pickup Instructions:</strong></p>
                <p>%s</p>
              </div>

              <p>Please bring a valid ID when picking up your device. If you have any questions about your repair, feel free to contact the shop directly.</p>

              <p>Thank you for trusting TechRestore with your repair needs!</p>

              <p>Best regards,<br>
              <strong>The TechRestore Team</strong></p>
            </div>
            <div class="footer">
              <p>&copy; 2024 TechRestore. All rights reserved.</p>
            </div>
          </div>
        </body>
        </html>
        """.formatted(customerName, repairRequestId, deviceType, issueDescription,
                formattedDate, shopName, pickupInstructions);
    }
}