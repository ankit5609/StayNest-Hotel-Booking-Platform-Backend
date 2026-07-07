# 🏨 StayNest — Hotel Booking System Backend

StayNest is a production-grade, high-performance, and feature-rich hotel booking platform backend built with Spring Boot 3.x and Java 21. It provides robust capabilities for guests looking to browse and book accommodations, and hotel managers looking to control inventories, configure dynamic rates, and analyze reports.

With native integrations for Stripe (payments/refunds), Cloudinary (ephemeral file handling), and SMTP/Brevo (transactional mail notifications), StayNest delivers a modern booking pipeline alongside cutting-edge AI features (conversational search, dynamic dynamic-pricing adjustments, and retrieval-augmented reviews).

---

## 🌟 Key Features

### 🔑 Secure & Stateless Authentication
*   **JWT-Based Auth**: Secure stateless filter chains authenticate users through HTTP-Only, secure cookies.
*   **Role-Based Security**: Restricts operations dynamically between `GUEST` and `HOTEL_MANAGER` roles.

### 💰 Transactional Payments & Webhooks (Stripe)
*   **Seamless Checkout**: Automated generation of Stripe checkout session redirects.
*   **Signature Verification Webhook**: Reconciles payment events (`checkout.session.completed`, `checkout.session.expired`, `payment_intent.payment_failed`) securely.
*   **Graceful Recovery**: Stores failed Stripe sessions in a `REFUND_PENDING` database state to support manual recovery and audits.

### 📈 Intelligent Dynamic Pricing (Decorator Pattern)
*   Computes dynamic rates based on cumulative pricing strategies:
    *   **Base Pricing**: Base rate of the room type.
    *   **Surge Factor**: Configurable date-specific multipliers.
    *   **Occupancy Markup**: Multiplies price by `1.2x` if total bookings exceed 80% capacity.
    *   **Urgency Markup**: Multiplies price by `1.15x` if check-in is less than 7 days away.
    *   **Holiday Markup**: Applies holiday premiums.
    *   **AI-Driven Rates**: Integrates with OpenRouter (using Gemini) to adjust rates by `[0.8x - 1.3x]` based on real-time market metrics, falling back safely to static multipliers upon timeout or error.

### 🧠 Advanced AI & RAG Orchestration
*   **Conversational Natural Language Search**: Extracts search queries (city, dates, pricing, ratings) strictly from natural language inputs using structured JSON models (powered by OpenRouter/Spring AI).
*   **Review-Grounded Q&A (RAG)**: Indexes hotel review comments into a Postgres pgvector database (`vector_store`). Users can ask specific questions about a hotel, and the model answers grounded only in those review texts.

### 📧 Transactional Notifications
*   **SMTP Mail Pipeline**: Integration with Brevo SMTP relay to automatically email verification, confirmation, reset tokens, and billing details to users.

### 🛡️ Double-Booking Prevention
*   **Pessimistic Locking**: Locks inventory dates in the database (`PESSIMISTIC_WRITE`) during initiate and checkout phases to avoid race conditions.

---

## 🏗️ Architecture & Folder Structure

```
com.cybernode.projects.HotelBookingApp
├── advice       # Global controllers mapping standardization & exceptions
├── config       # ModelMapper, Stripe, Cloudinary, Security configurations
├── controller   # REST Endpoints (Admin, Guest, Auth, Webhooks, Diagnostics)
├── dto          # Unified request and response payload models
├── entity       # Database mapping entities (JPA/Hibernate)
├── enums        # Domain state definitions (BookingStatus, Gender, Role)
├── exception    # Custom runtime errors
├── repository   # JPA/Postgres data access abstraction
├── security     # Token logic, JWT Filters, and Authorization limits
├── service      # Core business interfaces (implementors in impl subfolder)
└── strategy     # Dynamic pricing decorator patterns
```

---

## 🛠️ Technology Stack
*   **Java Version**: 21
*   **Framework**: Spring Boot 3.5.15
*   **Database**: PostgreSQL 16+ with `vector` extension (pgvector)
*   **Build System**: Maven (via `./mvnw`)
*   **Integrations**: Stripe SDK (v32.1.0), Cloudinary SDK (v2.4.0), JavaMailSender (Angus Mail), Spring AI OpenAI Adapter, pgvector Vector Store

---

## 🚀 Getting Started

### 📋 Prerequisites
*   JDK 21 or higher installed.
*   Docker installed (for local pgvector database).

### 1. Spin up Postgres + pgvector via Docker
Run the following command to boot a PostgreSQL container equipped with the `pgvector` extension:
```bash
docker compose up -d
```
This starts PostgreSQL on `localhost:5432` and initializes the required schemas.

### 2. Configure Environment Variables
Create a `.env` file at the root of the project directory with your credentials:
```env
JWT_SECRET_KEY=your_jwt_signing_key_here
CORS_ALLOWED_ORIGINS=https://staynest.arclite.site,http://localhost:5173

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/neondb?sslmode=disable
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Stripe Credentials
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Cloudinary Storage
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...

# Transactional Mail (Brevo / SMTP)
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=your_brevo_smtp_username
MAIL_PASSWORD=your_brevo_smtp_password
MAIL_FROM=staynest@arclite.site

# AI / OpenRouter
OPENROUTER_API_KEY=sk-or-v1-...
AI_PRICING_ENABLED=false
REVIEW_QA_ENABLED=false
OPENAI_API_KEY=sk-proj-... # Required for review embeddings if REVIEW_QA_ENABLED=true
```

### 3. Build & Launch the Application
Compile the code and run the Spring Boot application locally:
```bash
./mvnw clean package
./mvnw spring-boot:run
```
The application starts on port `8080` under the servlet context `/api/v1`.

---

## 📡 API Endpoints Summary

### Authentication (`/api/v1/auth`)
*   `POST /signup` - Registers a new guest or manager.
*   `POST /login` - Issues authentication JWTs via HttpOnly cookies.
*   `POST /logout` - Clears active session tokens.

### Hotels & Search (`/api/v1/hotels`)
*   `GET /search` - Paginated hotel search filterable by price, rating, city, dates, and sort criteria.
*   `POST /search/nl` - Natural language query parsing (requires LLM credentials).
*   `GET /{hotelId}/info` - Fetch public hotel rates and metadata.

### Bookings (`/api/v1/bookings`)
*   `POST /initiate` - Reserves dates and creates temporary pending states.
*   `POST /{bookingId}/guests` - Associates guest names with the booking.
*   `POST /{bookingId}/payments` - Returns a Stripe Checkout redirect session URL.
*   `POST /{bookingId}/cancel` - Initiates cancellation and schedules proximity-based refunds.

### Reviews & Q&A (`/api/v1/reviews`)
*   `POST /reviews` - Submits verified guest stays reviews.
*   `GET /hotels/{hotelId}/ask?question=...` - Queries reviews using RAG embeddings.

### Testing (`/api/v1/test-email`)
*   `GET /test-email?to=...` - Sends a diagnostic password-reset mail to the given email address.

---

## 🧪 Running Tests
Execute unit and integration suites using Maven:
```bash
./mvnw test
```

---

## 🛡️ Cancellation Policy
*   **Refund Breakdown**:
    *   `>= 7 days` before check-in: **100%** refund.
    *   `3 to 7 days` before check-in: **50%** refund.
    *   `< 3 days` before check-in: **0%** refund.
*   **Guard Rule**: Bookings cannot be cancelled on or after their check-in date. Attempts throw an `IllegalStateException` on the backend.
