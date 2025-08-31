# Email Verification System

This document describes the email verification system implemented for the Tech Restore application.

## Overview

The email verification system ensures that users and shops verify their email addresses before they can log in to the application. This helps maintain data integrity and reduces spam registrations.

## Features

### For Users (Guests)
- Email verification required before login
- Verification token expires in 24 hours
- Resend verification email functionality
- Welcome email after successful verification

### For Shops
- Email verification required before login
- Verification token expires in 24 hours
- Resend verification email functionality
- Welcome email after successful verification

## API Endpoints

### User Authentication

#### Register User
```
POST /api/auth/register
```
- Sends verification email to the provided email address
- Returns success message with verification instructions

#### Verify Email
```
GET /api/auth/verify-email?token={verification_token}
```
- Verifies the email using the token from the verification email
- Returns success/error message

#### Resend Verification Email
```
POST /api/auth/resend-verification?email={user_email}
```
- Resends verification email to the specified email address
- Only works for unverified accounts

#### Login
```
POST /api/auth/login
```
- Blocks login if email is not verified
- Returns verification reminder if email is unverified

### Shop Authentication

#### Register Shop
```
POST /api/auth/shops/register
```
- Sends verification email to the shop's email address
- Returns success message with verification instructions

#### Verify Email
```
GET /api/auth/shops/verify-email?token={verification_token}
```
- Verifies the shop's email using the token
- Returns success/error message

#### Resend Verification Email
```
POST /api/auth/shops/resend-verification?email={shop_email}
```
- Resends verification email to the shop's email address
- Only works for unverified accounts

#### Login
```
POST /api/auth/shops/login
```
- Blocks login if email is not verified
- Returns verification reminder if email is unverified

## Configuration

### Email Settings

The application requires email configuration in `application.properties`:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${EMAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# App Configuration
app.email.from=${EMAIL_FROM:noreply@techrestore.com}
app.base-url=${BASE_URL:http://localhost:8080}
```

### Environment Variables

Set these environment variables for email functionality:
- `EMAIL_USERNAME`: SMTP username (e.g., your Gmail address)
- `EMAIL_PASSWORD`: SMTP password (e.g., Gmail app password)
- `EMAIL_FROM`: Sender email address for verification emails
- `BASE_URL`: Base URL of your application for verification links

## Database Changes

### User Entity
Added fields:
- `email_verified` (boolean): Whether email is verified
- `email_verification_token` (string): Verification token
- `email_token_expiry` (datetime): Token expiration time

### Shop Entity
Added fields:
- `email_verified` (boolean): Whether email is verified
- `email_verification_token` (string): Verification token
- `email_token_expiry` (datetime): Token expiration time

## Security Features

1. **Secure Token Generation**: Uses cryptographically secure random tokens
2. **Token Expiration**: Tokens expire after 24 hours
3. **One-time Use**: Tokens are deleted after successful verification
4. **Login Protection**: Prevents login until email is verified

## Email Templates

The system sends two types of emails:

### Verification Email
- Subject: "Email Verification - Tech Restore"
- Contains verification link with token
- Expires in 24 hours

### Welcome Email
- Subject: "Welcome to Tech Restore!"
- Sent after successful email verification
- Confirms account activation

## Error Handling

The system handles various error scenarios:
- Invalid verification tokens
- Expired tokens
- Already verified emails
- Email sending failures (registration continues even if email fails)

## Testing

Use the `EmailVerificationUtilsTest` class to test token generation and expiration logic.

```bash
mvn test -Dtest=EmailVerificationUtilsTest
```