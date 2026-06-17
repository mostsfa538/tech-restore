<div align="center">

# 🔧 Tech Restore

### A Comprehensive Tech Repair & E-Commerce Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

**Tech Restore** is a full-featured backend platform that connects customers with tech repair shops, enables product sales, manages deliveries, and handles secure online payments — all through a robust, role-based RESTful API.

[Features](#-features) · [Architecture](#-architecture) · [Getting Started](#-getting-started) · [API Reference](#-api-reference) · [Tech Stack](#-tech-stack)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Reference](#-api-reference)
- [Role-Based Access](#-role-based-access)
- [Payment Integration](#-payment-integration)
- [Real-Time Features](#-real-time-features)
- [Database Schema](#-database-schema)
- [Contributing](#-contributing)

---

## 🌟 Overview

**Tech Restore** is a multi-tenant B2C platform designed for the tech repair and sales industry. It serves as a marketplace where:

- **Customers** can browse products, place orders, submit repair requests, chat with shops, and track deliveries in real-time.
- **Shops** can manage their inventory, process orders and repairs, create promotional offers, view financial dashboards, and handle subscriptions.
- **Delivery Personnel** can accept/reject deliveries, update delivery statuses, and manage their profiles.
- **Assigners** act as logistics coordinators, assigning orders and repair pickups to delivery personnel.
- **Admins** have full oversight — managing users, shops, deliveries, transactions, and platform statistics.

---

## ✨ Features

### 🔐 Authentication & Security
- **JWT-Based Authentication** with access & refresh token rotation
- **OAuth2 Login** via Google (social sign-in)
- **Email Verification** with OTP codes (send, verify, resend)
- **Forgot/Reset Password** flow with OTP validation
- **Role-Based Access Control** (RBAC) with `@PreAuthorize`
- **Multi-device Logout** (logout from all sessions)
- **HTTP-Only Refresh Token Cookies** for enhanced security

### 🛒 E-Commerce & Product Management
- **Product Catalog** with search, category filtering, and price-range queries
- **Shopping Cart** — add, update quantity, remove items, clear cart
- **Order Lifecycle** — create, confirm, process, ship, deliver, cancel
- **Order Tracking** — real-time order status tracking for customers
- **Product Import** — bulk product upload via CSV file (Spring Batch)
- **Inventory Management** — stock updates, low-stock alerts, out-of-stock detection, CSV export
- **Category Management** — admin-managed product categories

### 🔧 Repair Request System
- **Full Repair Workflow** with state machine transitions:
  ```
  SUBMITTED → QUOTE_SENT → QUOTE_APPROVED → DEVICE_COLLECTED → REPAIRING → REPAIR_COMPLETED → DEVICE_DELIVERED
  ```
- **Quote System** — shops send price quotes, customers approve or reject
- **Repair Payments** — separate payment flow for repair services
- **Status Guards** — enforced valid state transitions to prevent illegal status changes

### 🏪 Shop Management
- **Shop Registration & Onboarding** with admin approval workflow
- **Shop Types** — `REPAIRER`, `SELLER`, or `BOTH`
- **Shop Dashboard** with sales stats, repair stats, total orders, and revenue tracking
- **Financial Reports** — detailed repair & order payment breakdowns
- **Revenue Tracking** — sales revenue, repair revenue, total profit per shop
- **Address Management** — multi-address support for shop locations
- **Review System** — customers rate shops; shops view their reviews

### 💰 Subscription & Monetization
- **Shop Subscriptions** — monthly subscription plans (Commission or Ratio-based)
- **Card & Cash Payments** for subscriptions
- **Subscription Renewal** — renew before or after expiration
- **Admin Confirmation** — admin can confirm cash subscription payments
- **Revenue Sharing** — configurable `repairRevenuePercentage` and `productRevenuePercentage` per shop

### 🎁 Offers & Promotions
- **Shop Offers** — create, update, delete promotional offers with discount types (percentage/fixed)
- **User Offer Browsing** — customers can view active offers
- **Admin Offer Oversight** — admins can view and delete any offer

### 🚚 Delivery Management
- **Delivery Registration** with admin approval
- **Order Delivery** — accept/reject orders, update delivery status
- **Repair Delivery** — collect devices for repair, return repaired devices
- **Delivery Profile** management
- **Assignment System** — Assigners allocate deliveries to delivery personnel

### 📋 Assigner Module
- **Order & Repair Assignment** — assign orders and repair pickups to specific delivery persons
- **Reassignment** — transfer deliveries between delivery persons with notes
- **Assignment Logs** — full audit trail of all assignments and reassignments
- **Available Delivery Persons** — view pool of available delivery personnel

### 💳 Payment System (Paymob Integration)
- **Card Payments** via Paymob payment gateway (iFrame-based)
- **Cash Payments** with admin confirmation flow
- **Payment Types** — `ORDER_PAYMENT`, `REPAIR_PAYMENT`, `SUBSCRIPTION`
- **Webhook Handling** — Paymob webhook callbacks for payment status updates
- **HMAC Verification** — secure payment callback validation
- **Refund Processing** — admin-initiated refunds
- **Transaction History** — per-user and global transaction views

### 💬 Real-Time Chat (WebSocket)
- **WebSocket Chat** between users and shops via STOMP protocol
- **Chat Sessions** — create, manage, and query chat sessions
- **Message History** — paginated chat message retrieval
- **Role-Based Messaging** — sender role tracking (`USER`, `SHOP`)

### 🔔 Notification System
- **Role-Specific Notifications** — separate endpoints for Users, Shops, Deliveries, and Assigners
- **Notification Types** — order lifecycle events, repair status updates
- **Notification History** — stored per-entity (users, shops)

### 🛡️ Admin Dashboard
- **Platform Statistics** — global stats (users, shops, orders, revenue)
- **User Management** — list, view, activate, deactivate, update roles
- **Shop Management** — approve, suspend, delete shops; view approved/suspended shops
- **Delivery Management** — approve, suspend, delete delivery accounts
- **Assigner Management** — approve, suspend, delete assigners
- **Transaction Oversight** — view all platform transactions
- **Product & Review Moderation** — admin-level product and review management

---

## 🏗 Architecture

The project follows a **modular, layered architecture** organized by domain:

```
┌─────────────────────────────────────────────────────┐
│                   API Layer (REST)                   │
│   Controllers · DTOs · Request/Response Mapping      │
├─────────────────────────────────────────────────────┤
│                  Service Layer                       │
│   Business Logic · Validation · Authorization        │
├─────────────────────────────────────────────────────┤
│                Repository Layer (JPA)                │
│   Spring Data JPA · Custom Queries · Pagination      │
├─────────────────────────────────────────────────────┤
│               Entity / Domain Layer                  │
│   JPA Entities · Enums · State Machines              │
├─────────────────────────────────────────────────────┤
│                Infrastructure Layer                  │
│   Security (JWT/OAuth2) · WebSocket · Email · Paymob │
└─────────────────────────────────────────────────────┘
```

### Module Breakdown

| Module | Description |
|--------|-------------|
| `common` | Shared entities, enums, security config, JWT, auth, payment, chat, notifications, email, exceptions |
| `user` | Customer-facing features — products, cart, orders, reviews, repair requests, offers |
| `shop` | Shop management — products, orders, repairs, inventory, offers, subscriptions, dashboard, financials |
| `delivery` | Delivery personnel — order delivery, repair device collection, profile management |
| `assigners` | Logistics coordination — assign/reassign deliveries, view delivery personnel, assignment logs |
| `admin` | Platform administration — user/shop/delivery/assigner management, stats, transactions |

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.3 |
| **Security** | Spring Security + JWT (jjwt 0.12.5) + OAuth2 (Google) |
| **Database** | PostgreSQL 42.7.3 |
| **ORM** | Spring Data JPA / Hibernate |
| **Validation** | Hibernate Validator + Jakarta Validation |
| **WebSocket** | Spring WebSocket + STOMP + Spring Messaging |
| **Email** | Spring Boot Starter Mail (Gmail SMTP) |
| **Payments** | Paymob Payment Gateway |
| **Batch Processing** | Spring Batch (CSV product imports) |
| **CSV Parsing** | OpenCSV 5.7.1 |
| **Caching** | Caffeine Cache |
| **UUID Generation** | uuid-creator 5.3.7 (Time-ordered UUIDs) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Build Tool** | Maven |
| **Boilerplate** | Lombok |
| **JSON** | Jackson + org.json |
| **Connection Pool** | HikariCP |

---

## 📁 Project Structure

```
tech-restore/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/com/techRestore/tech/restore/
│   │   │   ├── TechRestoreApplication.java          # Main entry point
│   │   │   │
│   │   │   ├── common/                              # Shared module
│   │   │   │   ├── controller/
│   │   │   │   │   ├── BaseController.java          # Base response helpers
│   │   │   │   │   ├── CategoryController.java      # Category CRUD
│   │   │   │   │   ├── auth/                        # Auth endpoints
│   │   │   │   │   ├── chat/                        # WebSocket + REST chat
│   │   │   │   │   ├── notification/                # Notification endpoints
│   │   │   │   │   └── payment/                     # Payment + webhook
│   │   │   │   ├── dto/                             # Shared DTOs
│   │   │   │   ├── exception/                       # Global exception handling
│   │   │   │   ├── interfaces/                      # OtpVerifiable, etc.
│   │   │   │   ├── model/
│   │   │   │   │   ├── entities/                    # 25 JPA entities
│   │   │   │   │   └── enums/                       # 22 enum types
│   │   │   │   ├── repository/                      # Shared repositories
│   │   │   │   ├── security/
│   │   │   │   │   ├── config/                      # Security configuration
│   │   │   │   │   ├── filter/                      # JWT auth filter
│   │   │   │   │   ├── jwt/                         # JWT utility
│   │   │   │   │   └── userdetails/                 # Custom UserDetailsService
│   │   │   │   ├── services/                        # Auth, payment, email, chat, etc.
│   │   │   │   └── utils/                           # Cookie utilities, helpers
│   │   │   │
│   │   │   ├── user/                                # Customer module
│   │   │   │   ├── controller/                      # 9 controllers
│   │   │   │   ├── dto/                             # User-specific DTOs
│   │   │   │   ├── repository/                      # User repositories
│   │   │   │   └── service/                         # User business logic
│   │   │   │
│   │   │   ├── shop/                                # Shop module
│   │   │   │   ├── controller/                      # 8 controllers
│   │   │   │   ├── dto/                             # Shop-specific DTOs
│   │   │   │   ├── repository/                      # Shop repositories
│   │   │   │   └── service/                         # Shop business logic
│   │   │   │
│   │   │   ├── delivery/                            # Delivery module
│   │   │   │   ├── controller/                      # DeliveryController
│   │   │   │   ├── dto/                             # Delivery DTOs
│   │   │   │   ├── repository/                      # Delivery repositories
│   │   │   │   └── service/                         # Delivery + RepairDelivery
│   │   │   │
│   │   │   ├── assigners/                           # Assigner module
│   │   │   │   ├── components/                      # Assigner utilities
│   │   │   │   ├── controller/                      # AssignerController
│   │   │   │   ├── dto/                             # Assigner DTOs
│   │   │   │   ├── repository/                      # Assigner repositories
│   │   │   │   └── service/                         # Assigner business logic
│   │   │   │
│   │   │   └── admin/                               # Admin module
│   │   │       ├── controller/                      # 5 controllers
│   │   │       ├── dto/                             # Admin DTOs (stats, etc.)
│   │   │       ├── repository/                      # Admin repositories
│   │   │       └── service/                         # Admin business logic
│   │   │
│   │   └── resources/
│   │       └── application.properties               # App configuration
│   │
│   └── test/                                        # Test suite
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+** (or use the included `mvnw` wrapper)
- **PostgreSQL 14+**
- **Gmail Account** (for email OTP — with App Password)
- **Paymob Account** (for payment integration)

### 1. Clone the Repository

```bash
git clone https://github.com/mostsfa538/tech-restore.git
cd tech-restore
```

### 2. Set Up PostgreSQL

Create a database:

```sql
CREATE DATABASE shopify;
```

### 3. Configure Application Properties

Update `src/main/resources/application.properties` with your credentials:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/shopify
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# JWT Secret (generate a new one for production!)
jwt.secret=YOUR_SECRET_KEY
jwt.expiration=900000
jwt.refresh-expiration=604800000

# Email (Gmail SMTP)
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET

# Paymob
paymob.apiKey=YOUR_PAYMOB_API_KEY
paymob.hmacSecret=YOUR_HMAC_SECRET
paymob.iframeId=YOUR_IFRAME_ID
paymob.cardIntegrationId=YOUR_INTEGRATION_ID
```

### 4. Build & Run

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or with Maven
mvn spring-boot:run
```

The application starts on **`http://localhost:8080`**

### 5. Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8080/swagger-ui/index.html
```

---

## ⚙ Configuration

### Key Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `jwt.expiration` | `900000` (15 min) | Access token TTL in ms |
| `jwt.refresh-expiration` | `604800000` (7 days) | Refresh token TTL in ms |
| `spring.datasource.hikari.maximum-pool-size` | `50` | Max DB connection pool |
| `spring.data.web.pageable.default-page-size` | `50` | Default pagination size |
| `spring.servlet.multipart.max-file-size` | `10MB` | Max CSV upload size |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto schema migration |

---

## 📡 API Reference

### Authentication (`/api/auth`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/register/user` | Register a new customer |
| `POST` | `/register/shop` | Register a new shop |
| `POST` | `/register/delivery` | Register delivery personnel |
| `POST` | `/register/assigner` | Register an assigner |
| `POST` | `/login` | Login (returns JWT + refresh cookie) |
| `POST` | `/refresh-token` | Refresh access token |
| `POST` | `/logout` | Logout current session |
| `POST` | `/logout-all` | Logout from all devices |
| `GET`  | `/get-code` | Request email OTP |
| `POST` | `/verify-email` | Verify email with OTP |
| `POST` | `/resend-otp` | Resend OTP code |
| `POST` | `/forgot-password` | Initiate password reset |
| `POST` | `/reset-password` | Reset password with OTP |

### User — Products (`/api/products`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | List all products (paginated) |
| `GET` | `/{id}` | Get product details |
| `GET` | `/shop/{shopId}` | Products by shop |
| `GET` | `/search?keyword=` | Search products |
| `GET` | `/category/{categoryId}` | Products by category |
| `GET` | `/price-range?minPrice=&maxPrice=` | Filter by price range |
| `GET` | `/{shopId}/{categoryId}` | Products by shop & category |

### User — Cart (`/api/cart`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | View cart |
| `POST` | `/items` | Add item to cart |
| `PUT` | `/items/{itemId}` | Update cart item quantity |
| `DELETE` | `/items/{itemId}` | Remove item from cart |
| `DELETE` | `/` | Clear entire cart |

### User — Orders (`/api/users/orders`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/` | Create a new order |
| `GET` | `/` | List user orders (paginated) |
| `GET` | `/{orderId}` | Get order details |
| `DELETE` | `/{orderId}/cancel` | Cancel an order |
| `GET` | `/{orderId}/tracking` | Track order status |

### User — Repair Requests (`/api/users/repair-request`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | List user's repair requests |
| `POST` | `/{shopId}` | Submit a repair request |
| `GET` | `/{requestId}` | Get repair request details |
| `PUT` | `/{shopId}/{requestId}` | Update repair request |
| `DELETE` | `/{requestId}/cancel` | Cancel repair request |
| `PUT` | `/{requestId}/status` | Update repair status |
| `POST` | `/repairs/{repairId}/confirm` | Confirm shop's repair quote |

### User — Reviews (`/api/reviews`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/{shopId}` | Create a review |
| `GET` | `/{id}` | Get review by ID |
| `GET` | `/{shopId}/reviews` | Get reviews for a shop |
| `PUT` | `/{id}` | Update a review |
| `DELETE` | `/cancel/{id}` | Delete a review |

### User — Offers (`/api/users`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/offers` | Browse active offers |
| `GET` | `/offers/{offerId}` | View offer details |

### Shop — Profile & Management (`/api/shops`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/{shopId}` | Get shop details |
| `PUT` | `/{id}` | Update shop profile |
| `GET` | `/address` | List shop addresses |
| `POST` | `/address` | Add shop address |
| `PUT` | `/address/{id}` | Update shop address |
| `DELETE` | `/address/{id}` | Delete shop address |
| `GET` | `/reviews` | View shop reviews |
| `GET` | `/payments/financial-report` | Financial report |
| `GET` | `/payments/repairs` | Repair payment transactions |
| `GET` | `/payments/orders` | Order payment transactions |

### Shop — Products (`/api/shops/products`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | List shop's products |
| `POST` | `/` | Add a new product |
| `PUT` | `/{productId}` | Update product |
| `DELETE` | `/{productId}` | Delete product |
| `PATCH` | `/{productId}/stock` | Update stock level |
| `POST` | `/import` | Bulk import via CSV |

### Shop — Orders (`/api/shops/orders/control`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | List all shop orders |
| `GET` | `/{orderId}` | Order details |
| `POST` | `/{orderId}/accept` | Accept an order |
| `POST` | `/{orderId}/reject` | Reject an order |
| `PUT` | `/{orderId}/status` | Update order status |
| `GET` | `/status/{status}` | Filter orders by status |

### Shop — Repairs (`/api/shops/repair-request`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | List repair requests |
| `GET` | `/{requestId}` | Repair request details |
| `PUT` | `/{requestId}/status` | Update repair status |
| `GET` | `/status/{status}` | Filter by repair status |
| `PUT` | `/{requestId}/price` | Set repair price/quote |

### Shop — Offers (`/api/shop/offers`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | List shop's offers |
| `GET` | `/{offerId}` | Offer details |
| `GET` | `/search?query=` | Search offers |
| `POST` | `/` | Create an offer |
| `PUT` | `/{offerId}` | Update an offer |
| `DELETE` | `/{offerId}` | Delete an offer |

### Shop — Inventory (`/api/shop/inventory`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/search?query=` | Search inventory |
| `GET` | `/low-stock` | Low stock products |
| `GET` | `/out-of-stock` | Out of stock products |
| `GET` | `/total-value` | Total inventory value |
| `GET` | `/total-items` | Total items count |
| `GET` | `/export` | Export inventory as CSV |

### Shop — Dashboard (`/api/shops/dashboard`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/sales/total` | Total sales in date range |
| `GET` | `/sales/stats` | Sales statistics |
| `POST` | `/orders/total` | Total orders in date range |
| `GET` | `/repairs/stats` | Repair statistics |
| `GET` | `/repairs/total` | Total repairs today |

### Shop — Subscriptions (`/api/subscriptions`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/card` | Subscribe via card payment |
| `POST` | `/cash` | Subscribe via cash |
| `POST` | `/renew/card/{shopEmail}` | Renew subscription (card) |
| `POST` | `/renew/cash/{shopEmail}` | Renew subscription (cash) |
| `GET` | `/` | Current subscription status |
| `GET` | `/all` | Subscription history |
| `POST` | `/cash/confirm/{paymentId}` | Admin: confirm cash payment |

### Delivery (`/api/delivery`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/profile` | View delivery profile |
| `PUT` | `/profile` | Update delivery profile |
| `GET` | `/orders/available` | Available orders for delivery |
| `GET` | `/orders/my-deliveries` | My assigned deliveries |
| `POST` | `/orders/{orderId}/accept` | Accept a delivery |
| `POST` | `/orders/{orderId}/reject` | Reject a delivery |
| `PUT` | `/orders/{orderId}/status` | Update delivery status |
| `GET` | `/repair/available` | Available repair pickups |
| `GET` | `/repair/my-deliveries` | My repair deliveries |
| `POST` | `/repair/{repairRequestId}/accept` | Accept repair delivery |
| `POST` | `/repair/{repairRequestId}/reject` | Reject repair delivery |
| `PUT` | `/repair/{repairRequestId}/status` | Update repair delivery status |

### Assigner (`/api/assigner`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/profile` | View assigner profile |
| `PUT` | `/profile` | Update assigner profile |
| `GET` | `/delivery-persons` | Available delivery persons |
| `GET` | `/orders-for-assignment` | Unassigned orders |
| `GET` | `/repairs-for-assignment` | Unassigned repair requests |
| `POST` | `/assign-order` | Assign order to delivery |
| `POST` | `/assign-repair` | Assign repair to delivery |
| `GET` | `/delivery/{id}/orders` | Orders by delivery person |
| `GET` | `/delivery/{id}/repairs` | Repairs by delivery person |
| `PUT` | `/reassign-order/{orderId}` | Reassign an order |
| `PUT` | `/reassign-repair/{repairId}` | Reassign a repair |
| `GET` | `/assignment-log` | View assignment history |

### Payments (`/api/payments`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/order/card/{orderId}` | Pay for order via card |
| `POST` | `/repair/card/{repairRequestId}` | Pay for repair via card |
| `GET` | `/transactions/all` | User transaction history |
| `POST` | `/subscription/cash/confirm/{paymentId}` | Admin: confirm cash payment |

### Admin (`/api/admin`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/stats` | Platform-wide statistics |
| `GET` | `/users` | List all users |
| `GET` | `/users/{userId}` | User details |
| `PUT` | `/users/{userId}` | Update user role |
| `PUT` | `/users/{userId}/activate` | Activate user |
| `PUT` | `/users/{userId}/deactivate` | Deactivate user |
| `GET` | `/shops` | List all shops |
| `GET` | `/shops/{shopId}` | Shop details |
| `GET` | `/shops/approved` | Approved shops |
| `GET` | `/shops/suspend` | Suspended shops |
| `PUT` | `/shops/{shopId}/approve` | Approve a shop |
| `PUT` | `/shops/{shopId}/suspend` | Suspend a shop |
| `DELETE` | `/{id}` | Delete a shop |
| `GET` | `/offers` | All platform offers |
| `DELETE` | `/offers/{offerId}` | Delete an offer |
| `GET` | `/deliveries` | All delivery accounts |
| `GET` | `/deliveries/{id}` | Delivery details |
| `GET` | `/deliveries/pending` | Pending deliveries |
| `GET` | `/deliveries/approved` | Approved deliveries |
| `GET` | `/deliveries/suspended` | Suspended deliveries |
| `PUT` | `/deliveries/{id}/approve` | Approve delivery |
| `PUT` | `/deliveries/{id}/suspend` | Suspend delivery |
| `DELETE` | `/deliveries/{id}` | Delete delivery |
| `GET` | `/assigners` | All assigners |
| `GET` | `/assigners/{id}` | Assigner details |
| `GET` | `/assigners/pending` | Pending assigners |
| `GET` | `/assigners/approved` | Approved assigners |
| `GET` | `/assigners/suspended` | Suspended assigners |
| `PUT` | `/assigners/{id}/approve` | Approve assigner |
| `PUT` | `/assigners/{id}/suspend` | Suspend assigner |
| `DELETE` | `/assigners/{id}` | Delete assigner |
| `GET` | `/transactions/all` | All platform transactions |
| `GET` | `/transactions/{userId}` | User transactions |
| `GET` | `/all-payments` | All payments |
| `GET` | `/assignment-logs` | All assignment logs |
| `PUT` | `/payment-refund/{orderId}` | Process refund |
| `GET` | `/search` | Search shops by name |

---

## 👥 Role-Based Access

| Role | Description |
|------|-------------|
| `GUEST` | Registered customer — browse, buy, repair, review |
| `ADMIN` | Platform administrator — full system control |
| `DELIVERY` | Delivery personnel — handle order & repair deliveries |
| `ASSIGNER` | Logistics coordinator — assign deliveries to delivery persons |

> **Note:** Shops are registered as separate entities (not user roles) with their own authentication flow, types (`REPAIRER`, `SELLER`, `BOTH`), and approval status.

---

## 💳 Payment Integration

Tech Restore integrates with **[Paymob](https://paymob.com/)** for secure payment processing:

```
┌──────────┐    ┌──────────────┐    ┌─────────┐
│  Client   │───▶│  Tech Restore │───▶│  Paymob  │
│           │    │   Backend     │    │  Gateway │
│           │◀───│              │◀───│          │
└──────────┘    └──────────────┘    └─────────┘
                      │  ▲
                      │  │ Webhook callback
                      ▼  │
                ┌──────────────┐
                │  PostgreSQL   │
                │  (Payments)   │
                └──────────────┘
```

**Supported Payment Flows:**
- Order payments (card)
- Repair payments (card)
- Subscription payments (card & cash)
- Refund processing

---

## 🔄 Real-Time Features

### WebSocket Chat

The platform supports real-time chat between users and shops using **STOMP over WebSocket**:

- **Endpoint:** WebSocket connection via STOMP
- **Chat Sessions:** Created per user-shop pair
- **Message Persistence:** All messages are stored in the database
- **REST Fallback:** Chat history also available via REST API

### Notifications

Role-specific notification endpoints deliver real-time updates for:
- Order status changes
- Repair progress updates  
- Delivery assignments
- Payment confirmations

---

## 🗄 Database Schema

### Core Entities (25 total)

| Entity | Description |
|--------|-------------|
| `User` | Customer accounts with role, OTP, and status |
| `Shop` | Repair/sales shops with subscriptions and financial tracking |
| `Product` | Shop products with pricing, stock, and condition |
| `Category` | Product categories |
| `Order` | Customer orders with items and payment |
| `OrderItem` | Individual items in an order |
| `OrderPayment` | Payment record linked to an order |
| `CartItem` | Shopping cart items |
| `RepairRequest` | Repair service requests with status workflow |
| `RepairPayment` | Payment for repair services |
| `Review` | Customer reviews for shops |
| `Offer` | Promotional offers from shops |
| `Payment` | General payment records |
| `Subscription` | Shop subscription plans |
| `Delivery` | Delivery personnel profiles |
| `Assigner` | Logistics coordinator profiles |
| `AssignmentLog` | Audit trail for delivery assignments |
| `Address` | Customer delivery addresses |
| `ShopAddress` | Shop location addresses |
| `ChatSession` | Chat sessions between users and shops |
| `ChatMessage` | Individual chat messages |
| `Notification` | System notifications |
| `SupportTicket` | Support tickets |
| `RefreshToken` | JWT refresh token storage |
| `InventoryHistory` | Stock change audit log |

---


<div align="center">

### Built with ❤️ using Spring Boot

**[⬆ Back to Top](#-tech-restore)**

</div>
