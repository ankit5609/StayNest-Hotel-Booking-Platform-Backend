<div align="center">

<h1>🏨 StayNest — Backend API</h1>

<p><em>Production-grade Spring Boot REST API powering India's premier hotel booking platform</em></p>

<a href="https://hotel-booking-app-0swn.onrender.com/api/v1"><img src="https://img.shields.io/badge/🌐_Live_API-Render-4ade80?style=for-the-badge" alt="Live API"/></a>
<a href="https://hotel-booking-app-0swn.onrender.com/swagger-ui.html"><img src="https://img.shields.io/badge/📄_Swagger_UI-Interactive_Docs-orange?style=for-the-badge" alt="Swagger UI"/></a>
<a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot"/></a>
<a href="https://openjdk.org/projects/jdk/21/"><img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java 21"/></a>
<a href="https://www.postgresql.org/"><img src="https://img.shields.io/badge/PostgreSQL_16-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/></a>
<a href="https://stripe.com"><img src="https://img.shields.io/badge/Stripe-635BFF?style=for-the-badge&logo=stripe&logoColor=white" alt="Stripe"/></a>
<a href="https://openai.com"><img src="https://img.shields.io/badge/OpenAI_RAG-412991?style=for-the-badge&logo=openai&logoColor=white" alt="OpenAI"/></a>

<br/><br/>

> ⚡ **Cold start warning:** Backend runs on Render's free tier. First request after inactivity takes ~50 seconds to wake up.

</div>

---

## 📖 Table of Contents

<details open>
<summary><strong>Click to expand / collapse</strong></summary>

- [Overview](#-overview)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Domain Model](#-domain-model)
- [Booking Lifecycle](#-booking-lifecycle-deep-dive)
- [Dynamic Pricing Engine](#-dynamic-pricing-engine)
- [AI Integration (RAG)](#-ai-integration-rag-pipeline)
- [Payments (Stripe)](#-payments--stripe-checkout)
- [Security Model](#-security-model)
- [Scheduled Jobs](#-background-scheduled-jobs)
- [Cancellation & Refund Policy](#-cancellation--refund-policy)
- [API Reference](#-api-reference)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables-reference)
- [Docker](#-docker--containerisation)
- [Database Schema](#-database-schema)
- [Error Handling](#-error-handling--response-format)
- [Contributing](#-contributing)

</details>

---

## 🌟 Overview

The **StayNest Backend** is a production-grade REST API built with **Spring Boot 3.5** and **Java 21**. It powers the complete lifecycle of a hotel booking platform — from hotel discovery with AI-powered Q&A, dynamic surge pricing, Stripe Checkout integration, to automated inventory management, refund processing, and email notifications.

```
🧳 Guest Flow:
   Register → Login → Search Hotels → Browse Rooms → Init Booking
   → Add Guests → Pay via Stripe → Confirm → Cancel/Refund

🏨 Manager Flow:
   Create Hotel → Upload Photos → Add Room Types → Set Inventory
   → Activate Listing → View Bookings → Process Refunds → View Reports

🤖 AI Flow:
   User Question → Embed Query → pgvector Similarity Search
   → Retrieve Top-K Chunks → GPT Generates Contextual Answer

💰 Pricing Flow (Hourly Batch):
   Load All Hotels → Per-Hotel: Fetch Inventory Rows → Apply Decorator Chain
   [Base → Surge → Occupancy → Urgency → Holiday → (AI optional)]
   → Save Updated Prices → Refresh HotelMinPrice Cache
```

---

## 🏗 System Architecture

```
                    ┌──────────────────────────────────────┐
                    │         StayNest Frontend             │
                    │   React 19 + Vite  (Vercel CDN)      │
                    └──────────────────┬───────────────────┘
                                       │  HTTPS / REST
                                       ▼
┌──────────────────────────────────────────────────────────────────────┐
│                    Spring Boot API  (Java 21)                        │
│                    Hosted on Render · Port 8080                      │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    Spring Security Filter Chain                 │  │
│  │   CORS Filter → JWTAuthFilter → Role Checks → Controller      │  │
│  └─────────────────────────────┬──────────────────────────────────┘  │
│                                │                                      │
│  ┌─────────────┐  ┌────────────▼──────────┐  ┌─────────────────────┐ │
│  │ Controllers │→ │      Services          │→ │    Repositories     │ │
│  │ (REST Layer)│  │  (Business Logic)      │  │ (Spring Data JPA)   │ │
│  └─────────────┘  └────────────┬──────────┘  └──────────┬──────────┘ │
│                                │                         │            │
│                     ┌──────────▼──────────┐              │            │
│                     │  Strategy Engine     │   ┌──────────▼─────────┐ │
│                     │  (Pricing Decorator  │   │  Neon Postgres 16  │ │
│                     │   Chain + AI)        │   │  + pgvector        │ │
│                     └─────────────────────┘   └────────────────────┘ │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │                    External Integrations                          │ │
│  │  Stripe (Payments)  ·  OpenAI/OpenRouter (AI)  ·  Cloudinary     │ │
│  │  Gmail SMTP (Email Notifications)                                │ │
│  └──────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Package | Responsibility |
|---|---|---|
| **Controller** | `controller/` | HTTP mapping, `@Valid` request validation, response shaping |
| **Service** | `service/impl/` | Business logic, transaction orchestration, Stripe calls |
| **Repository** | `repository/` | Spring Data JPA + custom `@Query` with pessimistic locking |
| **Entity** | `entity/` | JPA-mapped DB tables with Hibernate annotations |
| **DTO** | `dto/` | Request/response objects with Bean Validation constraints |
| **Strategy** | `strategy/` | Decorator pattern pricing engine (pluggable multipliers) |
| **Security** | `security/` | JWT filter chain, BCrypt encoding, CORS configuration |
| **Config** | `config/` | Bean definitions: ModelMapper, Cloudinary, PricingRules |
| **Advice** | `advice/` | `@ControllerAdvice` global exception → API response mapper |

---

## 🛠 Tech Stack

### Core Framework

| Technology | Version | Why |
|---|---|---|
| [Java](https://openjdk.org/projects/jdk/21/) | **21 LTS** | Virtual threads, records, pattern matching, long-term support |
| [Spring Boot](https://spring.io/projects/spring-boot) | **3.5.15** | Auto-configuration, embedded Tomcat, rich ecosystem |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | — | Repository abstractions + custom JPQL/native queries |
| [Spring Security](https://spring.io/projects/spring-security) | — | Stateless JWT filter chain, role-based access control |
| [Spring Validation](https://beanvalidation.org) | — | `@NotNull`, `@Min`, `@Email` on DTOs |
| [Spring Scheduling](https://docs.spring.io/spring-framework) | — | `@Scheduled` for pricing batch + reservation auto-release |
| [Spring Mail](https://spring.io) | — | Gmail SMTP email notifications |
| [PostgreSQL](https://www.postgresql.org/) | **16** | ACID-compliant relational DB with `TEXT[]` array support |

### Security & Auth

| Library | Version | Purpose |
|---|---|---|
| [JJWT](https://github.com/jwtk/jjwt) | **0.12.6** | HS256 JWT token creation, signing, validation, claims parsing |
| [BCrypt](https://docs.spring.io/spring-security) | — | Password hashing (`BCryptPasswordEncoder`) |

### Payments

| Library | Version | Purpose |
|---|---|---|
| [Stripe Java SDK](https://github.com/stripe/stripe-java) | **32.1.0** | Checkout sessions, webhook event handling, refunds |

### AI & Vector Search

| Library | Version | Purpose |
|---|---|---|
| [Spring AI](https://spring.io/projects/spring-ai) | **1.1.8** | OpenAI chat + embeddings abstraction |
| [spring-ai-starter-model-openai](https://spring.io/projects/spring-ai) | 1.1.8 | OpenAI/OpenRouter adapter for chat and embeddings |
| [spring-ai-starter-vector-store-pgvector](https://spring.io/projects/spring-ai) | 1.1.8 | pgvector integration for RAG similarity search |
| [OpenRouter](https://openrouter.ai) | — | LLM routing (gpt-oss-20b for chat, text-embedding-3-small) |

### Media & Email

| Library | Version | Purpose |
|---|---|---|
| [Cloudinary](https://cloudinary.com) | **2.4.0** | Hotel & room photo upload, URL generation, CDN delivery |
| Spring Mail | — | Gmail SMTP — booking confirmation, OTP, expiry notifications |

### Developer Experience

| Library | Version | Purpose |
|---|---|---|
| [Lombok](https://projectlombok.org) | — | `@Getter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j` |
| [ModelMapper](https://modelmapper.org) | **3.2.6** | Entity ↔ DTO mapping (`modelMapper.map(entity, Dto.class)`) |
| [SpringDoc OpenAPI](https://springdoc.org) | **2.8.3** | Auto-generates Swagger UI + OpenAPI 3.0 JSON spec |

### Testing

| Library | Purpose |
|---|---|
| Spring Boot Test | `@SpringBootTest` integration tests |
| Spring Security Test | Auth context for endpoint tests |
| Testcontainers (PostgreSQL) | Real Postgres in Docker for repository tests |
| Testcontainers (JUnit Jupiter) | Container lifecycle management for JUnit 5 |

---

## 📁 Project Structure

```
HotelBookingApp/
├── src/
│   ├── main/
│   │   ├── java/com/cybernode/projects/HotelBookingApp/
│   │   │   │
│   │   │   ├── HotelBookingAppApplication.java       # @SpringBootApplication entry point
│   │   │   │
│   │   │   ├── controller/                           # REST API layer
│   │   │   │   ├── AuthController.java               # POST /auth/* (login, signup, OTP)
│   │   │   │   ├── HotelBrowseController.java        # GET /hotels (search + details + AI Q&A)
│   │   │   │   ├── HotelBookingController.java       # POST /bookings/* (full booking flow)
│   │   │   │   ├── HotelController.java              # /admin/hotels/* (manager hotel CRUD)
│   │   │   │   ├── RoomAdminController.java          # /admin/hotels/{id}/rooms/* (room CRUD)
│   │   │   │   ├── InventoryController.java          # /admin/inventory/* (date availability)
│   │   │   │   ├── ReviewController.java             # /hotels/{id}/reviews (guest reviews)
│   │   │   │   ├── UserController.java               # /users/* (profile, wishlist, guests)
│   │   │   │   ├── WebhookController.java            # POST /webhooks/stripe (Stripe events)
│   │   │   │   └── HealthController.java             # GET /health (uptime ping)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── impl/
│   │   │   │   │   ├── BookingServiceImpl.java       # ★ Core booking lifecycle (586 lines)
│   │   │   │   │   │                                 #   initBooking, addGuests, initiatePayments,
│   │   │   │   │   │                                 #   capturePayment, cancelBooking,
│   │   │   │   │   │                                 #   @Scheduled releaseExpiredReservations
│   │   │   │   │   ├── HotelServiceImpl.java         # Hotel CRUD, search, activation
│   │   │   │   │   ├── RoomServiceImpl.java          # Room type CRUD + photo upload
│   │   │   │   │   ├── InventoryServiceImpl.java     # Inventory date range management
│   │   │   │   │   ├── UserServiceImpl.java          # Profile, wishlist, guest profiles
│   │   │   │   │   └── ReviewServiceImpl.java        # Review create/delete
│   │   │   │   ├── PricingUpdateService.java         # ★ Hourly pricing batch job
│   │   │   │   ├── AiPricingService.java             # OpenRouter AI price adjustment
│   │   │   │   ├── CheckoutService.java              # Stripe Checkout session factory
│   │   │   │   └── NotificationService.java          # Email dispatch (confirm, OTP, expiry)
│   │   │   │
│   │   │   ├── entity/                               # JPA-mapped database tables
│   │   │   │   ├── User.java                         # Users with roles[] and wishlist[]
│   │   │   │   ├── Hotel.java                        # Hotel with photos[] and amenities[] TEXT[]
│   │   │   │   ├── HotelContactInfo.java             # @Embedded contact details block
│   │   │   │   ├── Room.java                         # Room type with price, photos[], amenities[]
│   │   │   │   ├── Inventory.java                    # ★ Per-date room availability with surgeFactor
│   │   │   │   ├── HotelMinPrice.java                # Cached minimum nightly price per hotel
│   │   │   │   ├── Booking.java                      # Full booking record + Stripe session ID
│   │   │   │   ├── Guest.java                        # Companion traveller profiles
│   │   │   │   └── Review.java                       # Hotel review with star rating
│   │   │   │
│   │   │   ├── dto/                                  # Request & Response DTOs
│   │   │   │   ├── BookingRequest.java               # hotelId, roomId, dates, roomsCount
│   │   │   │   ├── BookingDto.java                   # Full booking response
│   │   │   │   ├── HotelDto.java                     # Hotel listing response
│   │   │   │   ├── RoomDto.java                      # Room type response
│   │   │   │   ├── HotelReportDto.java               # Revenue/occupancy report response
│   │   │   │   └── GuestDto.java                     # Guest profile response
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── InventoryRepository.java          # ★ Pessimistic-locked custom queries:
│   │   │   │   │                                     #   findAndLockAvailableInventory,
│   │   │   │   │                                     #   initBooking, confirmBooking,
│   │   │   │   │                                     #   releaseReservedInventory, cancelBooking
│   │   │   │   ├── BookingRepository.java            # findByPaymentSessionId, status queries
│   │   │   │   ├── HotelRepository.java              # Hotel search with price filter JOINs
│   │   │   │   ├── HotelMinPriceRepository.java      # Min-price cache lookups
│   │   │   │   ├── RoomRepository.java
│   │   │   │   ├── UserRepository.java               # findByEmail for auth
│   │   │   │   ├── GuestRepository.java
│   │   │   │   └── ReviewRepository.java
│   │   │   │
│   │   │   ├── strategy/                             # Decorator-pattern pricing engine
│   │   │   │   ├── PricingStrategy.java              # Interface: calculatePrice(Inventory)
│   │   │   │   ├── BasePricingStrategy.java          # Returns inventory.getPrice() as base
│   │   │   │   ├── SurgePricingStrategy.java         # Multiplies by inventory.getSurgeFactor()
│   │   │   │   ├── OccupancyPricingStrategy.java     # +20% if occupancy > 80%
│   │   │   │   ├── UrgencyPricingStrategy.java       # +15% if check-in within 7 days
│   │   │   │   ├── HolidayPricingStrategy.java       # +25% on configured holiday dates
│   │   │   │   ├── AiDynamicPricingStrategy.java     # AI price adjustment (optional)
│   │   │   │   └── PricingService.java               # ★ Builds decorator chain + calls AI
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── WebSecurityConfig.java            # Filter chain: CORS, JWT, route matchers
│   │   │   │   ├── JWTAuthFilter.java                # Extracts + validates JWT per request
│   │   │   │   ├── JWTService.java                   # Token generation, parsing, expiry
│   │   │   │   └── AuthService.java                  # login, signup, OTP, reset logic
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java                    # ModelMapper + PasswordEncoder beans
│   │   │   │   ├── CloudinaryConfig.java             # Cloudinary SDK bean
│   │   │   │   ├── PricingRuleProperties.java        # @ConfigurationProperties for pricing params
│   │   │   │   └── OpenApiConfig.java                # Swagger metadata + JWT bearer scheme
│   │   │   │
│   │   │   ├── advice/
│   │   │   │   └── GlobalExceptionHandler.java       # Maps all exceptions → ApiResponse envelope
│   │   │   │
│   │   │   ├── enums/
│   │   │   │   └── BookingStatus.java                # RESERVED, GUESTS_ADDED, PAYMENTS_PENDING,
│   │   │   │                                         # CONFIRMED, COMPLETED, CANCELLED,
│   │   │   │                                         # REFUND_PENDING, REFUNDED, EXPIRED,
│   │   │   │                                         # PAYMENT_FAILED
│   │   │   │
│   │   │   └── exception/                            # Custom exception classes
│   │   │       ├── ResourceNotFoundException.java    # → HTTP 404
│   │   │       └── UnAuthorisedException.java        # → HTTP 403
│   │   │
│   │   └── resources/
│   │       └── application.properties               # All config with ${ENV:default} fallbacks
│   │
│   └── test/                                        # Unit & integration tests
│
├── Dockerfile                                       # Multi-stage Maven → JRE build
├── docker-compose.yml                               # Local pgvector/pg16 Postgres
├── pom.xml                                          # Maven dependencies (Spring AI BOM managed)
└── openapi.json                                     # Generated OpenAPI 3.0 spec (91 KB)
```

---

## 🗃 Domain Model

```
                 ┌──────────────────────────────────────────────────┐
                 │                    User                          │
                 │  id · email · password(BCrypt) · name            │
                 │  roles[] · wishlist[] (→ Hotel)                  │
                 └──────┬────────────────────────────┬─────────────┘
                        │ owns (HOTEL_MANAGER)        │ books (GUEST)
                        ▼                             ▼
              ┌──────────────────┐       ┌────────────────────────────────┐
              │      Hotel       │       │           Booking              │
              │ name · city      │       │ checkIn · checkOut · roomsCount│
              │ photos[] TEXT[]  │←──────│ amount · bookingStatus         │
              │ amenities[] TEXT[]       │ paymentSessionId               │
              │ active · avgRating│      │ refundAmount                   │
              └──────┬───────────┘       └────────────┬───────────────────┘
                     │ hasMany                        │ hasMany (join table)
                     ▼                               ▼
              ┌──────────────┐              ┌──────────────────┐
              │     Room     │              │      Guest       │
              │ type · price │              │ name · email     │
              │ totalCount   │              │ phone            │
              │ photos[] TEXT[]             └──────────────────┘
              │ amenities[]  │
              └──────┬───────┘
                     │ hasMany (per date)
                     ▼
         ┌────────────────────────────────────────────────┐
         │                  Inventory                     │
         │  date (unique per hotel+room+date)             │
         │  totalCount  →  how many rooms physically exist │
         │  bookedCount →  confirmed reservations         │
         │  reservedCount → held during checkout (temp)  │
         │  surgeFactor · price (= basePrice × surgeFactor)│
         │  city · closed (block bookings on a date)      │
         └────────────────────────────────────────────────┘

         ┌────────────────────────────────────────────────┐
         │                HotelMinPrice                   │
         │  hotel · date · price                          │
         │  Pre-computed min nightly price per hotel/date │
         │  Used for efficient search price filtering     │
         └────────────────────────────────────────────────┘
```

---

## 🔄 Booking Lifecycle Deep Dive

### Complete Status Machine

```
                        User calls initBooking()
                                  │
                         ┌────────▼────────┐
                         │    RESERVED      │  ← Inventory reservedCount++
                         │  (Held for 15m) │     Stripe session NOT yet created
                         └────────┬────────┘
                     15 min       │  addGuests()
                   @Scheduled     │
               auto-release ──────┤
                    (EXPIRED)     │
                         ┌────────▼────────┐
                         │  GUESTS_ADDED   │  ← Guests linked to booking
                         └────────┬────────┘
                                  │  initiatePayments()
                         ┌────────▼────────────┐
                         │  PAYMENTS_PENDING    │  ← Stripe Checkout session created
                         │                     │     Redirect URL returned to frontend
                         └────────┬──┬──────────┘
                       Stripe     │  │  Stripe session
                       success    │  │  expires/fails
                    webhook ──────┘  └──── webhook
                         │                    │
               ┌──────────▼───────┐  ┌────────▼────────────┐
               │    CONFIRMED     │  │  EXPIRED /           │
               │ (Permanent book) │  │  PAYMENT_FAILED      │
               │ reservedCount    │  │  inventory released   │
               │ → bookedCount    │  └─────────────────────┘
               └──────────┬───────┘
                          │  cancelBooking() (before check-in date)
               ┌──────────▼───────────┐
               │   REFUND_PENDING     │  ← Refund amount calculated
               │                     │     Stripe refund initiated
               └──────────┬───────────┘
                          │  Manager approves refund
               ┌──────────▼───────┐
               │    REFUNDED      │  ← Stripe refund confirmed
               └──────────────────┘
```

### Inventory Locking (Race Condition Prevention)

To prevent double-booking under concurrent requests, the `InventoryRepository` uses **PostgreSQL pessimistic locking**:

```java
// findAndLockAvailableInventory uses SELECT ... FOR UPDATE SKIP LOCKED
// This ensures only ONE transaction can hold inventory rows at a time
inventoryRepository.findAndLockAvailableInventory(
    room.getId(),
    checkInDate,
    stayEndDate,         // checkOutDate.minusDays(1)
    roomsCount
);
```

**Why `stayEndDate = checkOutDate.minusDays(1)`?**

> A booking July 30 → August 2 occupies 3 nights: July 30, 31, August 1.
> The guest checks **out** on August 2, so that room is available for new arrivals.
> Inventory is only held for nights the guest actually sleeps there.

### Key Code: `initialiseBooking`

```java
// 1. Validate dates
if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn))
    throw new IllegalArgumentException("Check-out must be after check-in");

// 2. Lock and fetch available inventory rows
long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
LocalDate stayEndDate = checkOut.minusDays(1);
List<Inventory> inventoryList = inventoryRepository
    .findAndLockAvailableInventory(roomId, checkIn, stayEndDate, roomsCount);

// 3. All nights must be available
if (inventoryList.size() != nights)
    throw new IllegalStateException("Room not available for all selected dates");

// 4. Increment reservedCount in DB
inventoryRepository.initBooking(roomId, checkIn, stayEndDate, roomsCount);

// 5. Calculate dynamic price via strategy chain
BigDecimal pricePerRoom = pricingService.calculateTotalPrice(inventoryList);
BigDecimal totalPrice = pricePerRoom.multiply(BigDecimal.valueOf(roomsCount));

// 6. Create RESERVED booking
Booking booking = Booking.builder()
    .bookingStatus(BookingStatus.RESERVED)
    .hotel(hotel).room(room).user(currentUser)
    .checkInDate(checkIn).checkOutDate(checkOut)
    .roomsCount(roomsCount).amount(totalPrice)
    .build();
```

---

## 💰 Dynamic Pricing Engine

StayNest uses a **Decorator Pattern** pricing pipeline that applies multiple pricing multipliers in sequence, each wrapping the previous strategy.

### Strategy Chain

```
Inventory Row
     │
     ▼
┌─────────────────────┐
│  BasePricingStrategy │  → Returns inventory.getPrice() (base nightly rate)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────┐
│  SurgePricingStrategy        │  → price × inventory.getSurgeFactor()
│  (manual manager adjustment)│    (surgeFactor set per-day by manager)
└──────────┬──────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│  OccupancyPricingStrategy         │  → if bookedCount/totalCount > 0.80:
│  (demand-based surge)             │       price × 1.20  (configurable)
└──────────┬───────────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│  UrgencyPricingStrategy           │  → if checkInDate within 7 days:
│  (last-minute premium)            │       price × 1.15  (configurable)
└──────────┬───────────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│  HolidayPricingStrategy           │  → if date in [Jan 1, Aug 15, Oct 2, Dec 25]:
│  (holiday premium)                │       price × 1.25  (configurable)
└──────────┬───────────────────────┘
           │  (only when AI enabled)
           ▼
┌──────────────────────────────────┐
│  AiDynamicPricingStrategy         │  → Calls OpenRouter LLM with:
│  (optional AI override)           │     - current price, occupancy, velocity
│                                   │     Returns multiplier in [0.80, 1.30]
└──────────────────────────────────┘
           │
           ▼
     Final Nightly Price
```

### All Multipliers Are Configurable

Every multiplier is driven by `application.properties` / environment variables — no code changes needed:

```properties
pricing.rules.occupancy-threshold=0.8       # >80% occupancy triggers surge
pricing.rules.occupancy-multiplier=1.2      # +20% when above threshold
pricing.rules.urgency-window-days=7         # Last-minute window
pricing.rules.urgency-multiplier=1.15       # +15% last-minute premium
pricing.rules.holiday-multiplier=1.25       # +25% on holidays
pricing.rules.holiday-dates=2026-01-01,2026-08-15,2026-10-02,2026-12-25
```

### Hourly Price Batch (`PricingUpdateService`)

Every hour, the `PricingUpdateService` recalculates prices for all inventory rows across all hotels and refreshes the `HotelMinPrice` cache:

```java
@Scheduled(cron = "0 0 * * * *")   // Every hour at :00
public void updatePrices() {
    // Paginates hotels in batches of 100 to avoid memory pressure
    // For each hotel:
    //   1. Fetch all inventory rows (today → +1 year)
    //   2. Compute booking velocity (# confirmed bookings in last 7 days)
    //   3. Apply decorator chain per inventory row
    //   4. Save updated prices in bulk
    //   5. Recompute HotelMinPrice for search filter accuracy
}
```

> **Why pre-compute `HotelMinPrice`?**
> The search endpoint filters by price range. Scanning all `Inventory` rows per search query would be expensive. `HotelMinPrice` provides a per-hotel-per-day cached minimum that makes price range filtering O(1) per hotel.

---

## 🤖 AI Integration (RAG Pipeline)

StayNest uses **Spring AI 1.1.8** with **OpenRouter** (OpenAI-compatible) and **pgvector** for two AI-powered features.

### Feature 1: Per-Hotel Q&A (`POST /hotels/{id}/ask`)

Guests can ask natural language questions about any hotel and receive intelligent answers:

- *"Does this hotel have a rooftop pool?"*
- *"What's the cancellation policy?"*
- *"Which room type is best for a family of 4?"*

**RAG Pipeline:**

```
                       Guest types question
                              │
               ┌──────────────▼──────────────────────┐
               │  1. Embed the question                │
               │     OpenRouter → text-embedding-3-small│
               │     Produces 1536-dim vector          │
               └──────────────┬──────────────────────┘
                              │
               ┌──────────────▼──────────────────────┐
               │  2. pgvector similarity search        │
               │     COSINE_DISTANCE with HNSW index  │
               │     Retrieves top-K=5 chunks          │
               │     (from hotel's amenities, reviews) │
               └──────────────┬──────────────────────┘
                              │
               ┌──────────────▼──────────────────────┐
               │  3. Build GPT prompt                  │
               │     System: "You are a hotel concierge│
               │     for {HotelName}. Answer based on  │
               │     context only."                    │
               │     Context: top-K retrieved chunks   │
               │     User: original question           │
               └──────────────┬──────────────────────┘
                              │
               ┌──────────────▼──────────────────────┐
               │  4. Call gpt-oss-20b via OpenRouter   │
               │     Returns grounded answer           │
               └─────────────────────────────────────┘
```

### Feature 2: Natural Language Hotel Search

When `search.nl.enabled=true`, a guest can describe a hotel in plain English and the system will interpret it as structured search filters via AI.

### pgvector Configuration

```properties
spring.ai.vectorstore.pgvector.index-type=HNSW          # Approximate nearest-neighbour
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
spring.ai.vectorstore.pgvector.dimensions=1536           # text-embedding-3-small output
spring.ai.vectorstore.pgvector.initialize-schema=true   # Auto-creates vector_store table
review.qa.top-k=5                                        # Retrieve top 5 chunks
review.qa.similarity-threshold=0.5                      # Min cosine similarity score
```

---

## 💳 Payments — Stripe Checkout

### Payment Flow

```
Frontend                    Backend                    Stripe
   │                           │                          │
   │  POST /bookings/{id}/     │                          │
   │  payments                 │                          │
   │──────────────────────────►│                          │
   │                           │  Create Checkout Session  │
   │                           │──────────────────────────►│
   │                           │  ← sessionUrl             │
   │  ← { url: sessionUrl }    │                          │
   │                           │                          │
   │  Redirect to sessionUrl   │                          │
   │──────────────────────────────────────────────────────►│
   │                           │                          │
   │                           │     User pays on Stripe  │
   │                           │                          │
   │                           │  POST /webhooks/stripe   │
   │                           │  checkout.session.       │
   │                           │  completed event         │
   │                           │◄─────────────────────────│
   │                           │                          │
   │                           │  Verify Stripe signature  │
   │                           │  Update booking CONFIRMED │
   │                           │  Shift reservedCount      │
   │                           │  → bookedCount            │
   │                           │  Send confirmation email  │
   │                           │                          │
   │  Frontend polls           │                          │
   │  GET /bookings/{id}/      │                          │
   │  status every 3s (120s)   │                          │
   │──────────────────────────►│                          │
   │  ← { status: CONFIRMED }  │                          │
```

### Webhook Events Handled

| Stripe Event | Action |
|---|---|
| `checkout.session.completed` | Confirm booking, shift inventory, send confirmation email |
| `checkout.session.expired` | Release held inventory, mark booking EXPIRED, notify user |
| `payment_intent.payment_failed` | Release inventory, mark PAYMENT_FAILED, notify user |

> **Idempotency:** Each webhook handler checks current booking status before applying changes. Duplicate events from Stripe are safely ignored.

---

## 🔐 Security Model

### Authentication Flow

```
Client Request
     │  Authorization: Bearer eyJhbGci...
     ▼
JWTAuthFilter
     │  1. Extract token from header
     │  2. Parse claims: email, roles, expiry via JWTService (HS256)
     │  3. Load UserDetails via CustomUserDetailsService
     │  4. Set SecurityContextHolder
     ▼
Route Matcher (WebSecurityConfig)
     │
     ├─ /admin/**          → requires ROLE_HOTEL_MANAGER
     ├─ /bookings/**       → requires any authenticated user
     ├─ /users/**          → requires any authenticated user
     ├─ /auth/logout       → requires any authenticated user
     ├─ /reviews/**        → requires any authenticated user
     └─ everything else    → public (no auth needed)
```

### Role Hierarchy

| Role | Access Level |
|---|---|
| `GUEST` | Search hotels, book rooms, manage own bookings, manage own guest profiles |
| `HOTEL_MANAGER` | All GUEST permissions + create/manage hotels, rooms, inventory, process refunds |
| `ADMIN` | Full platform access (future) |

### CORS Configuration

Dynamically reads comma-separated origins from `app.cors.allowed-origins`:

```java
configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
configuration.setAllowedHeaders(List.of("*"));
configuration.setAllowCredentials(true);
```

Default allowed origins: `https://staynest.arclite.site`, `http://localhost:3000`, `http://localhost:5173`

### Password Security

All passwords are hashed using **BCrypt** (work factor 10) via Spring Security's `BCryptPasswordEncoder`. Plaintext passwords are never stored or logged.

---

## ⏱ Background Scheduled Jobs

| Job | Class | Cron | What It Does |
|---|---|---|---|
| **Reservation Auto-Release** | `BookingServiceImpl` | `0 */5 * * * *` (every 5 min) | Finds RESERVED/GUESTS_ADDED/PAYMENTS_PENDING bookings older than 15 minutes → releases inventory → marks EXPIRED |
| **Dynamic Price Update** | `PricingUpdateService` | `0 0 * * * *` (every hour) | Recalculates surge prices for all hotels via decorator chain → refreshes `HotelMinPrice` cache for next 365 days |

### Reservation Expiry Logic

```java
@Scheduled(cron = "0 */5 * * * *")
@Transactional
public void releaseExpiredReservations() {
    LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);

    // Find all non-confirmed bookings older than 15 minutes
    List<Booking> stale = bookingRepository.findByBookingStatusInAndCreatedAtBefore(
        List.of(RESERVED, GUESTS_ADDED, PAYMENTS_PENDING), cutoff
    );

    for (Booking booking : stale) {
        // Pessimistic lock, release reservedCount, mark EXPIRED
        inventoryRepository.releaseReservedInventory(...);
        booking.setBookingStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);
    }
}
```

---

## 💸 Cancellation & Refund Policy

Refund amounts are calculated based on how far in advance the cancellation occurs relative to check-in:

| Days Before Check-In | Refund |
|---|---|
| ≥ 7 days | **100%** full refund |
| 3–6 days | **50%** partial refund (configurable) |
| < 3 days | **0%** no refund |

All thresholds are configurable via environment variables:

```properties
cancellation.full-refund-days=7
cancellation.partial-refund-days=3
cancellation.partial-refund-percent=50
```

On cancellation:
1. Refund amount is calculated and stored on the booking
2. Booking status set to `REFUND_PENDING`
3. Inventory `bookedCount` is released (room available again)
4. Stripe refund is automatically initiated via `Refund.create()`
5. Booking status updated to `REFUNDED`

---

## 📡 API Reference

> 📄 **Full interactive docs:** [swagger-ui.html](https://hotel-booking-app-0swn.onrender.com/swagger-ui.html)
>
> All endpoints are prefixed with `/api/v1`

### 🔑 Authentication — `/auth`

| Method | Endpoint | Body | Description |
|---|---|---|---|
| `POST` | `/auth/signup` | `{ name, email, password }` | Register a new account |
| `POST` | `/auth/login` | `{ email, password }` | Login, returns JWT + user info |
| `POST` | `/auth/logout` | — | Invalidate current session |
| `POST` | `/auth/forgot-password` | `{ email }` | Send OTP to registered email |
| `POST` | `/auth/reset-password` | `{ token, newPassword }` | Reset password via OTP token |
| `POST` | `/auth/change-password` | `{ oldPassword, newPassword }` | Change password (authenticated) |

### 🏨 Hotel Discovery — `/hotels`

| Method | Endpoint | Query Params | Description |
|---|---|---|---|
| `GET` | `/hotels` | `city, checkIn, checkOut, rooms, minPrice, maxPrice, amenities, page, size` | Search hotels with all filters |
| `GET` | `/hotels/{id}/info` | `checkIn, checkOut, rooms` | Hotel details + available rooms |
| `GET` | `/hotels/{id}/reviews` | `page, size` | Paginated guest reviews |
| `POST` | `/hotels/{id}/ask` | — | Body: `{ question }` — AI Q&A |

### 📅 Bookings — `/bookings`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/bookings/init` | Create booking, hold inventory for 15 min | JWT |
| `POST` | `/bookings/{id}/addGuests` | Attach guest IDs to booking | JWT |
| `POST` | `/bookings/{id}/payments` | Create Stripe Checkout session, get redirect URL | JWT |
| `POST` | `/bookings/{id}/verify-payment` | Poll & confirm payment via Stripe session ID | JWT |
| `GET` | `/bookings` | All bookings for authenticated user | JWT |
| `GET` | `/bookings/{id}` | Single booking full details | JWT |
| `GET` | `/bookings/{id}/status` | Lightweight status check for polling | JWT |
| `POST` | `/bookings/{id}/cancel` | Cancel confirmed booking, initiate refund | JWT |

### 🏗 Manager: Hotels — `/admin/hotels`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/admin/hotels` | All hotels owned by this manager | Manager |
| `GET` | `/admin/hotels/{id}` | Single hotel (must be owned) | Manager |
| `POST` | `/admin/hotels` | Create new hotel listing | Manager |
| `PUT` | `/admin/hotels/{id}` | Update hotel profile, photos, amenities | Manager |
| `DELETE` | `/admin/hotels/{id}` | Delete hotel (blocked if active bookings exist) | Manager |
| `PATCH` | `/admin/hotels/{id}/activate` | Toggle hotel live/draft | Manager |
| `POST` | `/admin/hotels/{id}/photos` | Upload photo → Cloudinary, returns CDN URL | Manager |
| `GET` | `/admin/hotels/{id}/bookings` | All bookings for this hotel | Manager |
| `POST` | `/admin/hotels/bookings/{id}/refund` | Approve and process pending refund | Manager |
| `GET` | `/admin/hotels/{id}/reports` | Revenue & occupancy statistics | Manager |

### 🛏 Manager: Rooms — `/admin/hotels/{hotelId}/rooms`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/admin/hotels/{id}/rooms` | List all room types | Manager |
| `POST` | `/admin/hotels/{id}/rooms` | Create room type with price and capacity | Manager |
| `PUT` | `/admin/hotels/{id}/rooms/{roomId}` | Update room details | Manager |
| `DELETE` | `/admin/hotels/{id}/rooms/{roomId}` | Delete room type | Manager |
| `POST` | `/admin/hotels/{id}/rooms/{roomId}/photos` | Upload room photo | Manager |

### 📆 Manager: Inventory — `/admin/inventory`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/admin/inventory/rooms/{roomId}` | Get availability calendar (date range) | Manager |
| `PATCH` | `/admin/inventory/rooms/{roomId}` | Update room counts per date | Manager |

### 👥 User — `/users`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/users/profile` | Get current user profile | JWT |
| `PUT` | `/users/profile` | Update name, phone, etc. | JWT |
| `GET` | `/users/wishlist` | Get saved hotels | JWT |
| `POST` | `/users/wishlist/{hotelId}` | Add hotel to wishlist | JWT |
| `DELETE` | `/users/wishlist/{hotelId}` | Remove hotel from wishlist | JWT |
| `GET` | `/users/guests` | Get all guest profiles | JWT |
| `POST` | `/users/guests` | Create new guest profile | JWT |
| `PUT` | `/users/guests/{id}` | Update guest | JWT |
| `DELETE` | `/users/guests/{id}` | Delete guest profile | JWT |

### ⭐ Reviews — `/hotels/{id}/reviews`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/hotels/{id}/reviews` | Get paginated reviews | None |
| `POST` | `/hotels/{id}/reviews` | Post a review (must have stayed) | JWT |
| `DELETE` | `/reviews/{id}` | Delete own review | JWT |

### 🔗 Webhooks

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/webhooks/stripe` | Receives Stripe events. Verifies `Stripe-Signature` header before processing. |

### ❤️ Health

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/health` | Returns `{ status: "UP" }` — used for Render uptime checks |

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (JDK) — [Download](https://adoptium.net)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** — [Download](https://www.docker.com/get-started)
- Accounts for: **Stripe**, **OpenRouter**, **Cloudinary**, **Gmail**

### 1. Clone the Repository

```bash
git clone https://github.com/ankit5609/StayNest-Hotel-Booking-Platform-Backend.git
cd StayNest-Hotel-Booking-Platform-Backend
```

### 2. Start Local Postgres + pgvector

```bash
docker compose up -d
# Postgres 16 + pgvector starts at localhost:5432
# Database: hotel_db  |  User: postgres  |  Password: postgres123
```

### 3. Configure Environment

```bash
cp .env.example .env
# Edit .env with your API keys (see Environment Variables section below)
```

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

| URL | Description |
|---|---|
| `http://localhost:8080/api/v1/health` | Health check |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON |

### Useful Build Commands

```bash
./mvnw clean compile                    # Compile only
./mvnw clean package -DskipTests        # Build JAR (skip tests)
./mvnw test                             # Run all tests
./mvnw spring-boot:run -Dspring-boot.run.profiles=local  # Run with local profile
```

---

## 🔑 Environment Variables Reference

All variables have `${ENV_VAR:default}` fallbacks in `application.properties` — defaults work for local dev.

```env
# ─── Database ─────────────────────────────────────────────────────────────
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hotel_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres123

# ─── JPA ──────────────────────────────────────────────────────────────────
SPRING_JPA_HIBERNATE_DDL_AUTO=update      # use 'validate' in production
SPRING_JPA_SHOW_SQL=false                 # set true for local debugging

# ─── Server ───────────────────────────────────────────────────────────────
PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api/v1
BACKEND_BASE_URL=https://hotel-booking-app-0swn.onrender.com/api/v1

# ─── Security / JWT ──────────────────────────────────────────────────────
JWT_SECRET_KEY=your-256-bit-base64-secret-minimum-32-chars
FRONTEND_URL=https://staynest.arclite.site
CORS_ALLOWED_ORIGINS=https://staynest.arclite.site,http://localhost:5173

# ─── Stripe ──────────────────────────────────────────────────────────────
STRIPE_SECRET_KEY=sk_live_...           # or sk_test_... for development
STRIPE_WEBHOOK_SECRET=whsec_...         # from Stripe Dashboard → Webhooks

# ─── Booking Policy ──────────────────────────────────────────────────────
BOOKING_EXPIRY_MINUTES=10               # How long to hold inventory before auto-release
CANCELLATION_FULL_REFUND_DAYS=7         # Days before check-in for 100% refund
CANCELLATION_PARTIAL_REFUND_DAYS=3      # Days before check-in for partial refund
CANCELLATION_PARTIAL_REFUND_PERCENT=50  # Percentage for partial refund

# ─── Email (Gmail SMTP) ──────────────────────────────────────────────────
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password   # Use an App Password, not your main password
MAIL_FROM=noreply@staynest.com
SPRING_MAIL_SMTP_AUTH=true
SPRING_MAIL_SMTP_STARTTLS_ENABLE=true

# ─── Cloudinary ──────────────────────────────────────────────────────────
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret

# ─── AI / OpenRouter ─────────────────────────────────────────────────────
OPENROUTER_API_KEY=sk-or-v1-...         # From openrouter.ai
SPRING_AI_OPENAI_BASE_URL=https://openrouter.ai/api
AI_PRICING_MODEL=openai/gpt-oss-20b    # Model for hotel Q&A chat
OPENAI_API_KEY=sk-...                   # For text-embedding-3-small

# ─── Pricing Rules ───────────────────────────────────────────────────────
PRICING_RULES_OCCUPANCY_THRESHOLD=0.8
PRICING_RULES_OCCUPANCY_MULTIPLIER=1.2
PRICING_RULES_URGENCY_WINDOW_DAYS=7
PRICING_RULES_URGENCY_MULTIPLIER=1.15
PRICING_RULES_HOLIDAY_MULTIPLIER=1.25
PRICING_RULES_HOLIDAY_DATES=2026-01-01,2026-08-15,2026-10-02,2026-12-25

# ─── AI Dynamic Pricing ──────────────────────────────────────────────────
AI_PRICING_ENABLED=false                # Set true to enable AI override
PRICING_AI_MIN_MULTIPLIER=0.80
PRICING_AI_MAX_MULTIPLIER=1.30
PRICING_AI_VELOCITY_LOOKBACK_DAYS=7    # Window for booking velocity calculation

# ─── Feature Flags ───────────────────────────────────────────────────────
REVIEW_QA_ENABLED=true
NL_SEARCH_ENABLED=true
```

---

## 🐳 Docker & Containerisation

### Build & Run Production Container

```bash
# Build the Docker image
docker build -t staynest-api:latest .

# Run the container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/hotel_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres123 \
  -e JWT_SECRET_KEY=your-jwt-secret \
  -e STRIPE_SECRET_KEY=sk_test_... \
  staynest-api:latest
```

### Dockerfile Explained

```dockerfile
# ── Stage 1: Build ──────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B      # Cache dependencies in a separate layer
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# ── Stage 2: Run (slim JRE only) ────────────────────────────────
FROM eclipse-temurin:21-jre-jammy    # ~200 MB vs ~600 MB full JDK
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> The multi-stage build ensures the final image only contains the JRE runtime — no Maven, no source code, no build tools. This keeps the image lean and secure.

### Local Development with Docker Compose

```bash
docker compose up -d          # Start Postgres 16 + pgvector
docker compose ps             # Check status
docker compose logs postgres  # View Postgres logs
docker compose down           # Stop services (keep data)
docker compose down -v        # Stop + wipe all data
```

---

## 🗄 Database Schema

```sql
-- Core tables
hotel       (id, name, city, photos TEXT[], amenities TEXT[], active, owner_id, avg_rating, review_count)
room        (id, hotel_id, type, price, total_count, photos TEXT[], amenities TEXT[])
inventory   (id, hotel_id, room_id, date, total_count, booked_count, reserved_count,
             surge_factor, price, city, closed)
             -- UNIQUE constraint on (hotel_id, room_id, date)
hotel_min_price (id, hotel_id, date, price)
             -- Pre-computed minimum nightly price per hotel per date

-- Users & Access
app_user    (id, email, password, name, created_at)
user_roles  (user_id, roles)    -- stored as separate rows, e.g. 'ROLE_HOTEL_MANAGER'
user_wishlist (user_id, hotel_id)

-- Bookings
booking     (id, hotel_id, room_id, user_id, rooms_count,
             check_in_date, check_out_date, booking_status,
             amount, payment_session_id, refund_amount,
             created_at, updated_at)
guest       (id, user_id, name, email, phone)
booking_guest (booking_id, guest_id)  -- many-to-many join

-- Reviews
review      (id, hotel_id, user_id, rating, comment, created_at)

-- AI Vector Store (managed by Spring AI)
vector_store (id, content TEXT, metadata JSONB, embedding VECTOR(1536))
             -- HNSW index on embedding column
```

### Key Indexes

```sql
-- Fast inventory lookup for booking availability check
CREATE INDEX ON inventory (room_id, date);

-- Min-price cache lookup for search filtering
CREATE INDEX ON hotel_min_price (hotel_id, date);

-- JWT authentication
CREATE UNIQUE INDEX ON app_user (email);

-- Stripe webhook deduplication
CREATE UNIQUE INDEX ON booking (payment_session_id);
```

---

## ⚠️ Error Handling & Response Format

All responses — success or error — use a unified envelope format:

### Success Response

```json
{
  "timeStamp": "2026-07-28T10:35:22.123",
  "data": {
    "id": 42,
    "bookingStatus": "CONFIRMED",
    "checkInDate": "2026-08-10",
    "checkOutDate": "2026-08-13",
    "amount": 22500.00
  },
  "error": null
}
```

### Error Response

```json
{
  "timeStamp": "2026-07-28T10:35:22.123",
  "data": null,
  "error": {
    "message": "Room is not available for all selected dates",
    "status": 409
  }
}
```

### HTTP Status Codes

| Status | Trigger | Example |
|---|---|---|
| `200 OK` | Successful GET/POST/PUT/PATCH | Hotel fetched, booking created |
| `201 Created` | Resource created successfully | Hotel or room created |
| `204 No Content` | Delete successful | Hotel photo removed |
| `400 Bad Request` | Bean validation failure | Missing required fields |
| `401 Unauthorized` | Missing or expired JWT | Token not provided |
| `403 Forbidden` | Insufficient role | Guest tries to access `/admin/**` |
| `404 Not Found` | Entity not in database | Hotel/booking ID doesn't exist |
| `409 Conflict` | Business rule violation | Room fully booked for those dates |
| `410 Gone` | Resource no longer available | Booking has expired |
| `422 Unprocessable` | Semantic validation failure | Activate hotel with 0 photos |
| `500 Internal Error` | Unhandled exception | Stripe API timeout |

---

## 🤝 Contributing

### Development Workflow

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Implement and test your change
4. Commit with a conventional message: `git commit -m "feat: add refund webhook handling"`
5. Push and open a Pull Request

### Commit Convention

```
feat:     New feature
fix:      Bug fix
docs:     Documentation
refactor: Code restructure (no behaviour change)
test:     Adding/updating tests
chore:    Build, config, dependency updates
```

### Code Standards

- **No business logic in controllers** — delegate to `@Service` classes
- **Transactional boundaries** — `@Transactional` on service methods that write data
- **DTOs at the boundary** — never expose JPA entities directly in responses
- **`@Valid` on all request bodies** — validate inputs before they reach service layer
- **Lombok everywhere** — `@RequiredArgsConstructor` over field injection, `@Slf4j` for logging
- **Custom exceptions** — `ResourceNotFoundException`, `UnAuthorisedException` for semantic errors

---

<div align="center">

Made with ❤️ &nbsp;·&nbsp; **Spring Boot 3.5** · **Java 21** · **PostgreSQL 16** · **pgvector** · **Stripe** · **Spring AI**

[⬆ Back to top](#-staynest--backend-api)

</div>
