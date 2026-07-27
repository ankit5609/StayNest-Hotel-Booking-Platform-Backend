<div align="center">

# 🏨 StayNest — Backend API

### *Robust, scalable Spring Boot REST API for the StayNest hotel booking platform*

[![Live API](https://img.shields.io/badge/🌐_Live_API-Render-4ade80?style=for-the-badge)](https://hotel-booking-app-0swn.onrender.com/api/v1)
[![Swagger UI](https://img.shields.io/badge/📄_Swagger_UI-Online-orange?style=for-the-badge)](https://hotel-booking-app-0swn.onrender.com/swagger-ui.html)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL_16-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

</div>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Domain Model](#-domain-model)
- [API Reference](#-api-reference)
- [Security Model](#-security-model)
- [AI Integration](#-ai-integration)
- [Payments Integration](#-payments-integration)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [Docker](#-docker)
- [Database](#-database)
- [Scheduled Jobs](#-scheduled-jobs)
- [Error Handling](#-error-handling)
- [Contributing](#-contributing)

---

## 🌟 Overview

The **StayNest Backend** is a production-grade REST API built with **Spring Boot 3.5** and **Java 21**. It powers the full lifecycle of a hotel booking platform — from hotel discovery and room inventory management to secure Stripe payments, AI-powered hotel Q&A, and automated refund processing.

```
Guest Flow:
  Register/Login → Search Hotels → View Rooms → Init Booking → Pay → Confirm

Manager Flow:
  Create Hotel → Add Rooms → Set Inventory → Go Live → Track Bookings → Process Refunds

AI Flow:
  User Question → OpenAI Embeddings → pgvector Similarity Search → GPT Answer
```

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        StayNest Frontend                        │
│            React 19 + Vite  (staynest.arclite.site)             │
└──────────────────────────────┬──────────────────────────────────┘
                               │  HTTPS / REST
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot API (Java 21)                   │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────┐│
│  │  Controllers │→│   Services   │→│      Repositories      ││
│  │  (REST layer)│  │ (Business    │  │  (Spring Data JPA)     ││
│  │              │  │  Logic)      │  │                        ││
│  └──────────────┘  └──────┬───────┘  └──────────┬─────────────┘│
│                           │                      │              │
│  ┌──────────────┐         │         ┌────────────▼─────────────┐│
│  │ Spring       │         │         │   Neon Postgres + pgvec  ││
│  │ Security +   │         │         │   (Cloud-hosted DB)      ││
│  │ JWT Filter   │         │         └──────────────────────────┘│
│  └──────────────┘         │                                     │
│                    ┌──────▼───────┐                             │
│                    │   External   │                             │
│                    │   Services:  │                             │
│                    │  • Stripe    │                             │
│                    │  • OpenAI    │                             │
│                    │  • Cloudinary│                             │
│                    │  • Gmail SMTP│                             │
│                    └──────────────┘                             │
└─────────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Package | Responsibility |
|---|---|---|
| **Controller** | `controller/` | HTTP request mapping, request validation, response shaping |
| **Service** | `service/` | Business logic, transaction orchestration |
| **Repository** | `repository/` | Spring Data JPA queries, pgvector similarity search |
| **Entity** | `entity/` | JPA-mapped database tables |
| **DTO** | `dto/` | Request/response data transfer objects with Bean Validation |
| **Security** | `security/` | JWT filter, UserDetails service, security configuration |
| **Config** | `config/` | Spring beans: ModelMapper, Cloudinary, Stripe, CORS |
| **Advice** | `advice/` | Global exception handler (`@ControllerAdvice`) |

---

## 🛠 Tech Stack

### Core

| Technology | Version | Purpose |
|---|---|---|
| [Java](https://openjdk.org/projects/jdk/21/) | 21 | Language (LTS) |
| [Spring Boot](https://spring.io/projects/spring-boot) | 3.5.15 | Application framework |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | — | ORM + repository layer |
| [Spring Security](https://spring.io/projects/spring-security) | — | Authentication & authorisation |
| [Spring Validation](https://beanvalidation.org) | — | Request body validation |
| [PostgreSQL](https://www.postgresql.org/) | 16 | Primary relational database |
| [pgvector](https://github.com/pgvector/pgvector) | — | Vector similarity search for AI |

### Integrations

| Library | Version | Purpose |
|---|---|---|
| [JJWT](https://github.com/jwtk/jjwt) | 0.12.6 | JWT creation & validation |
| [Stripe Java SDK](https://github.com/stripe/stripe-java) | 32.1.0 | Payment sessions & webhooks |
| [Spring AI (OpenAI)](https://spring.io/projects/spring-ai) | 1.1.8 | Hotel Q&A + vector embeddings |
| [Spring AI (pgvector)](https://spring.io/projects/spring-ai) | 1.1.8 | Vector store for RAG |
| [Cloudinary](https://cloudinary.com) | 2.4.0 | Hotel & room photo uploads |
| [Spring Mail](https://spring.io/guides/gs/sending-email/) | — | Gmail SMTP for OTP emails |
| [SpringDoc OpenAPI](https://springdoc.org) | 2.8.3 | Swagger UI & OpenAPI spec |
| [ModelMapper](https://modelmapper.org) | 3.2.6 | Entity ↔ DTO mapping |
| [Lombok](https://projectlombok.org) | — | Boilerplate reduction |

### Infrastructure

| Tool | Purpose |
|---|---|
| [Neon Postgres](https://neon.tech) | Serverless Postgres (production) |
| [Render](https://render.com) | Backend hosting (production) |
| [Docker](https://www.docker.com) | Containerisation for deployment |
| [Maven](https://maven.apache.org) | Build tool & dependency management |

---

## 📁 Project Structure

```
HotelBookingApp/
├── src/
│   ├── main/
│   │   ├── java/com/cybernode/projects/HotelBookingApp/
│   │   │   ├── HotelBookingAppApplication.java     # Spring Boot entry point
│   │   │   │
│   │   │   ├── controller/                         # REST controllers
│   │   │   │   ├── AuthController.java             # /auth/login, register, forgot-password
│   │   │   │   ├── HotelBrowseController.java      # /hotels search, details, AI Q&A
│   │   │   │   ├── HotelBookingController.java     # /bookings CRUD
│   │   │   │   ├── HotelController.java            # /admin/hotels CRUD (manager)
│   │   │   │   ├── RoomAdminController.java        # /admin/hotels/{id}/rooms CRUD
│   │   │   │   ├── InventoryController.java        # /admin/inventory management
│   │   │   │   ├── ReviewController.java           # /hotels/{id}/reviews
│   │   │   │   ├── UserController.java             # /users profile, wishlist, guests
│   │   │   │   ├── WebhookController.java          # /webhooks/stripe (Stripe events)
│   │   │   │   └── HealthController.java           # /health ping endpoint
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── impl/
│   │   │   │   │   ├── HotelServiceImpl.java       # Hotel search & management logic
│   │   │   │   │   ├── BookingServiceImpl.java     # Booking lifecycle + inventory mgmt
│   │   │   │   │   ├── RoomServiceImpl.java        # Room CRUD
│   │   │   │   │   ├── InventoryServiceImpl.java   # Inventory date management
│   │   │   │   │   ├── UserServiceImpl.java        # User profile, wishlist, guests
│   │   │   │   │   └── ReviewServiceImpl.java      # Review CRUD
│   │   │   │   ├── PricingUpdateService.java       # Hotel min-price cache updates
│   │   │   │   └── HotelService.java (interface)   # + other service interfaces
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── User.java          # App user (GUEST / MANAGER / ADMIN roles)
│   │   │   │   ├── Hotel.java         # Hotel listing with photos[], amenities[]
│   │   │   │   ├── HotelContactInfo.java  # Embedded contact details
│   │   │   │   ├── Room.java          # Room type with price, photos[], amenities[]
│   │   │   │   ├── Inventory.java     # Per-date room availability counts
│   │   │   │   ├── HotelMinPrice.java # Cached minimum nightly price per hotel
│   │   │   │   ├── Booking.java       # Booking with status, guests, payment session
│   │   │   │   ├── Guest.java         # Travelling companion profile
│   │   │   │   └── Review.java        # Hotel review with rating
│   │   │   │
│   │   │   ├── dto/                   # Request & response DTOs with @Valid annotations
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── HotelRepository.java
│   │   │   │   ├── HotelMinPriceRepository.java   # Min-price cache queries
│   │   │   │   ├── BookingRepository.java
│   │   │   │   ├── InventoryRepository.java
│   │   │   │   ├── RoomRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── GuestRepository.java
│   │   │   │   └── ReviewRepository.java
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── SecurityConfig.java         # CORS, route protection, filter chain
│   │   │   │   ├── JwtAuthFilter.java          # JWT extraction & validation per request
│   │   │   │   ├── JwtService.java             # Token generation & claims parsing
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java              # ModelMapper, PasswordEncoder beans
│   │   │   │   ├── CloudinaryConfig.java       # Cloudinary SDK configuration
│   │   │   │   └── OpenApiConfig.java          # Swagger/OpenAPI metadata
│   │   │   │
│   │   │   ├── advice/
│   │   │   │   └── GlobalExceptionHandler.java # Maps all exceptions → ApiResponse
│   │   │   │
│   │   │   ├── enums/
│   │   │   │   └── BookingStatus.java          # RESERVED, CONFIRMED, CANCELLED, REFUNDED
│   │   │   │
│   │   │   ├── exception/                      # Custom exception classes
│   │   │   └── strategy/                       # Refund calculation strategies
│   │   │
│   │   └── resources/
│   │       └── application.properties          # Spring configuration
│   │
│   └── test/                                   # Unit & integration tests
│
├── Dockerfile                                  # Multi-stage Docker build
├── docker-compose.yml                          # Local Postgres + pgvector setup
├── pom.xml                                     # Maven dependencies
└── openapi.json                                # Generated OpenAPI 3.0 spec
```

---

## 🗃 Domain Model

```
User ─────────────────┐
  │ (GUEST/MANAGER/   │
  │  ADMIN)           │
  │                   │
  ├─── owns ──────→ Hotel ──────→ Room ──────→ Inventory
  │                    │            │         (date, count)
  │                    │            │
  ├─── books ──────→ Booking ←─────┘
  │                    │
  │                    ├─── has ───→ Guest (many-to-many)
  │                    │
  │                    └─── status: RESERVED → CONFIRMED → CANCELLED/REFUNDED
  │
  ├─── reviews ────→ Review (hotel + rating)
  └─── wishlist ───→ Hotel[]
```

### Entity Summary

| Entity | Key Fields |
|---|---|
| `User` | email, password (hashed), roles[], name, wishlist[] |
| `Hotel` | name, city, photos[], amenities[], contactInfo, active, owner |
| `Room` | type, price, totalCount, photos[], amenities[], hotel |
| `Inventory` | date, reservedCount, bookedCount, roomId |
| `Booking` | hotel, room, user, checkInDate, checkOutDate, roomsCount, amount, bookingStatus, paymentSessionId |
| `Guest` | name, email, phone — linked to bookings via join table |
| `HotelMinPrice` | hotelId, minPrice — pre-computed cache for search filtering |
| `Review` | hotel, user, rating, comment |

### Booking Status Lifecycle

```
                    ┌─────────┐
                    │  INIT   │  ← initBooking()
                    └────┬────┘
         15min timeout   │  makePayment()
         auto-release ←──┤
                    ┌────▼────┐
                    │RESERVED │  ← Stripe session created, inventory held
                    └────┬────┘
             Stripe      │  verifyPayment() / Stripe webhook
             success  ───┤
                    ┌────▼────┐
                    │CONFIRMED│  ← Inventory permanently booked
                    └────┬────┘
                         │
              ┌──────────┴────────────┐
         cancelBooking()         Refund approved
              │                       │
         ┌────▼────┐            ┌─────▼──────┐
         │CANCELLED│            │  REFUNDED  │
         └─────────┘            └────────────┘
```

---

## 📡 API Reference

Interactive API documentation is available at **[Swagger UI](https://hotel-booking-app-0swn.onrender.com/swagger-ui.html)**.

### Authentication — `/api/v1/auth`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/auth/signup` | Register new account | None |
| `POST` | `/auth/login` | Login, receive JWT | None |
| `POST` | `/auth/logout` | Logout | JWT |
| `POST` | `/auth/forgot-password` | Send OTP to email | None |
| `POST` | `/auth/reset-password` | Reset via OTP token | None |
| `POST` | `/auth/change-password` | Change password | JWT |

### Hotel Discovery — `/api/v1/hotels`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/hotels` | Search hotels (city, dates, rooms, price, amenities) | None |
| `GET` | `/hotels/{hotelId}/info` | Hotel details + rooms | None |
| `GET` | `/hotels/{hotelId}/reviews` | Paginated hotel reviews | None |
| `POST` | `/hotels/{hotelId}/ask` | AI-powered hotel Q&A | None |

### Bookings — `/api/v1/bookings`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/bookings/init` | Create booking, hold inventory | JWT |
| `POST` | `/bookings/{id}/addGuests` | Attach guest profiles | JWT |
| `POST` | `/bookings/{id}/payments` | Create Stripe Checkout session | JWT |
| `POST` | `/bookings/{id}/verify-payment` | Poll & confirm payment status | JWT |
| `GET` | `/bookings/{id}` | Get booking details | JWT |
| `GET` | `/bookings/{id}/status` | Get booking status | JWT |
| `POST` | `/bookings/{id}/cancel` | Cancel booking | JWT |
| `GET` | `/bookings` | Get all user bookings | JWT |

### Manager Hotel CRUD — `/api/v1/admin/hotels`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/admin/hotels` | Create new hotel | Manager |
| `PUT` | `/admin/hotels/{id}` | Update hotel profile | Manager |
| `DELETE` | `/admin/hotels/{id}` | Delete hotel | Manager |
| `PATCH` | `/admin/hotels/{id}/activate` | Toggle hotel live/draft | Manager |
| `POST` | `/admin/hotels/{id}/photos` | Upload hotel photo to Cloudinary | Manager |
| `GET` | `/admin/hotels` | Get all hotels owned by manager | Manager |
| `GET` | `/admin/hotels/{id}` | Get specific hotel | Manager |
| `GET` | `/admin/hotels/{id}/bookings` | Get bookings for hotel | Manager |
| `POST` | `/admin/hotels/bookings/{id}/refund` | Process refund | Manager |
| `GET` | `/admin/hotels/{id}/reports` | Revenue & occupancy report | Manager |

### Manager Room CRUD — `/api/v1/admin/hotels/{hotelId}/rooms`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/admin/hotels/{id}/rooms` | Create room type | Manager |
| `GET` | `/admin/hotels/{id}/rooms` | Get all rooms | Manager |
| `PUT` | `/admin/hotels/{id}/rooms/{roomId}` | Update room | Manager |
| `DELETE` | `/admin/hotels/{id}/rooms/{roomId}` | Delete room | Manager |
| `POST` | `/admin/hotels/{id}/rooms/{roomId}/photos` | Upload room photo | Manager |

### Inventory — `/api/v1/admin/inventory`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/admin/inventory/rooms/{roomId}` | Get inventory calendar | Manager |
| `PATCH` | `/admin/inventory/rooms/{roomId}` | Update inventory counts | Manager |

### User — `/api/v1/users`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/users/profile` | Get user profile | JWT |
| `PUT` | `/users/profile` | Update profile | JWT |
| `POST` | `/users/wishlist/{hotelId}` | Add to wishlist | JWT |
| `DELETE` | `/users/wishlist/{hotelId}` | Remove from wishlist | JWT |
| `GET` | `/users/wishlist` | Get wishlist | JWT |
| `GET` | `/users/guests` | Get guest profiles | JWT |
| `POST` | `/users/guests` | Create guest profile | JWT |
| `PUT` | `/users/guests/{id}` | Update guest | JWT |
| `DELETE` | `/users/guests/{id}` | Delete guest | JWT |

### Reviews — `/api/v1/hotels/{hotelId}/reviews`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/hotels/{id}/reviews` | List reviews | None |
| `POST` | `/hotels/{id}/reviews` | Post review | JWT |
| `DELETE` | `/reviews/{id}` | Delete review | JWT |

### Webhooks — `/api/v1/webhooks`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/webhooks/stripe` | Stripe event receiver (checkout.session.completed) |

---

## 🔐 Security Model

### Authentication

- **JWT tokens** issued on login, signed with a secret key using HS256
- Every protected request passes through `JwtAuthFilter` which:
  1. Extracts the `Authorization: Bearer <token>` header
  2. Validates signature and expiry via `JwtService`
  3. Loads `UserDetails` and sets `SecurityContext`

### Role Hierarchy

```
ADMIN   →  All permissions
MANAGER →  Hotel/room/inventory/booking management for owned hotels
GUEST   →  Search, book, manage own bookings
```

### Security Configuration Highlights

```java
// Public endpoints (no auth required)
/api/v1/auth/**
/api/v1/hotels/**          (browse only)
/api/v1/webhooks/**
/swagger-ui/**
/v3/api-docs/**

// JWT required
/api/v1/bookings/**
/api/v1/users/**

// MANAGER or ADMIN required
/api/v1/admin/**
```

### CORS

Configured to allow requests from `staynest.arclite.site`, `localhost:5173`, and `localhost:3000`.

---

## 🤖 AI Integration

StayNest uses **Spring AI 1.1.8** with **OpenAI** and **pgvector** to power two AI features:

### 1. Hotel Q&A (`POST /hotels/{id}/ask`)

Guests can ask natural language questions about a specific hotel (e.g. *"Does this hotel have a spa?"*, *"What's the check-in time?"*).

**How it works (RAG pipeline):**
```
1. Hotel description, amenities, and reviews are chunked and embedded via OpenAI
2. Embeddings stored in pgvector's vector_store table
3. On question: embed the query → similarity search → retrieve top-k chunks
4. Pass chunks + question to GPT → generate contextual answer
```

### 2. AI Search Assistant (`POST /hotels/ask` from frontend)

The AI assistant on the search page understands natural language hotel queries and returns structured recommendations.

### pgvector Setup

The `vector_store` table is automatically managed by Spring AI. It stores:
- `embedding VECTOR(1536)` — OpenAI ada-002 embeddings
- `content TEXT` — Original text chunk
- `metadata JSONB` — Source hotel ID and chunk info

---

## 💳 Payments Integration

StayNest uses **Stripe Checkout** for secure payment processing.

### Payment Flow

```
1. POST /bookings/{id}/payments
   → Creates Stripe Checkout Session
   → Inventory held (RESERVED status)
   → Returns { url: "https://checkout.stripe.com/..." }

2. User completes payment on Stripe-hosted page

3. Stripe POSTs to /webhooks/stripe (checkout.session.completed)
   → Server verifies Stripe signature
   → Booking status updated to CONFIRMED
   → Inventory permanently booked (reservedCount → bookedCount)

4. Frontend polls GET /bookings/{id}/status every 3s (up to 120s)
   → Displays confirmation when status = CONFIRMED
```

### Auto-Release Scheduler

Bookings in `RESERVED` status older than 15 minutes are automatically released by a `@Scheduled` job running every 5 minutes:

```java
@Scheduled(cron = "0 */5 * * * *")
public void releaseAbandonedReservations() {
    // Finds stale RESERVED bookings → releases inventory → marks CANCELLED
}
```

This prevents inventory from being permanently held by abandoned checkouts.

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+**
- **Docker & Docker Compose** (for local Postgres)
- A **Neon** or local **PostgreSQL 16** database with `pgvector` extension

### 1. Clone the Repository

```bash
git clone https://github.com/ankit5609/Hotel-Booking-Platform.git
cd Hotel-Booking-Platform
```

### 2. Start Local Database

```bash
docker compose up -d
# Starts PostgreSQL 16 + pgvector at localhost:5432
```

### 3. Configure Environment

```bash
cp .env.example .env
# Edit .env with your credentials (see Environment Variables section)
```

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

API will be available at **http://localhost:8080/api/v1**
Swagger UI at **http://localhost:8080/swagger-ui.html**

### Build Only (No Run)

```bash
./mvnw clean compile          # Compile
./mvnw clean package          # Package JAR (skip tests)
./mvnw clean package -DskipTests
```

---

## 🔑 Environment Variables

Create a `.env` file in the project root (Spring Boot reads this via environment):

```env
# ─── Database ─────────────────────────────────────────────────
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hotel_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres123

# ─── JWT ───────────────────────────────────────────────────────
JWT_SECRET=your-256-bit-base64-secret-key
JWT_EXPIRY_MS=86400000

# ─── Stripe ────────────────────────────────────────────────────
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_SUCCESS_URL=https://staynest.arclite.site/payments/success
STRIPE_FAILURE_URL=https://staynest.arclite.site/payments/failure

# ─── OpenAI (Spring AI) ────────────────────────────────────────
SPRING_AI_OPENAI_API_KEY=sk-...

# ─── Cloudinary ────────────────────────────────────────────────
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret

# ─── Email (Gmail SMTP) ─────────────────────────────────────────
SPRING_MAIL_USERNAME=your-gmail@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

# ─── Frontend URL (CORS) ────────────────────────────────────────
FRONTEND_URL=https://staynest.arclite.site
```

---

## 🐳 Docker

### Build & Run with Docker

```bash
# Build the image
docker build -t staynest-api .

# Run the container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/hotel_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres123 \
  staynest-api
```

### Dockerfile (Multi-Stage Build)

```dockerfile
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml (Local Development)

```yaml
services:
  postgres:
    image: pgvector/pgvector:pg16
    container_name: hotelbooking-postgres
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres123
      POSTGRES_DB: hotel_db
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
```

---

## 🗄 Database

### Schema Overview

| Table | Description |
|---|---|
| `hotel` | Hotel listings with `TEXT[]` photos and amenities arrays |
| `room` | Room types with `TEXT[]` photos and amenities arrays |
| `inventory` | Per-date availability: `reserved_count` + `booked_count` per room |
| `hotel_min_price` | Pre-computed min nightly price per hotel for search performance |
| `booking` | Full booking record with status and Stripe session ID |
| `booking_guest` | Join table: booking ↔ guest (many-to-many) |
| `guest` | Travelling companion profiles |
| `app_user` | User accounts with hashed passwords |
| `user_roles` | User role assignments |
| `user_wishlist` | User ↔ hotel wishlist join table |
| `review` | Hotel reviews with numeric rating |
| `vector_store` | Spring AI pgvector embeddings for RAG |

### Inventory Logic

For a booking from **July 30 → August 2** (3 nights):
- Inventory is reserved for dates: **July 30, July 31, August 1**
- Check-out date (August 2) is **excluded** — the room becomes available again
- `reservedCount` is incremented on `initBooking()` and decremented to `bookedCount` on confirmation

---

## ⏱ Scheduled Jobs

| Job | Schedule | Description |
|---|---|---|
| `releaseAbandonedReservations` | `0 */5 * * * *` (every 5 min) | Finds RESERVED bookings older than 15 minutes → releases inventory → cancels booking |

---

## ⚠️ Error Handling

All exceptions are caught by `GlobalExceptionHandler` and mapped to a consistent response envelope:

```json
{
  "timeStamp": "2026-07-28T10:00:00",
  "data": null,
  "error": {
    "message": "Room not available for the selected dates",
    "status": 409
  }
}
```

### Common Status Codes

| Status | Scenario |
|---|---|
| `400` | Invalid request body (validation failure) |
| `401` | Missing or invalid JWT token |
| `403` | Insufficient role permissions |
| `404` | Resource not found (hotel, booking, room) |
| `409` | Conflict — room unavailable, duplicate guest email |
| `410` | Booking expired (RESERVED → abandoned) |
| `422` | Business rule violation (e.g., activate hotel with no photos) |
| `500` | Unhandled server error |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Commit your changes: `git commit -m "feat: add my feature"`
4. Push to your branch: `git push origin feat/my-feature`
5. Open a Pull Request

### Code Style

- Follow standard Java naming conventions
- Use Lombok annotations to reduce boilerplate
- All DTOs must have `@Valid` Bean Validation annotations
- Controllers must not contain business logic — delegate to services
- Use `@Transactional` on service methods that modify data

---

<div align="center">

Made with ❤️ &nbsp;·&nbsp; Built on **Spring Boot 3.5** + **Java 21** + **PostgreSQL**

[⬆ Back to top](#-staynest--backend-api)

</div>
