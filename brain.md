# StayNest Hotel Booking Backend System Documentation (brain.md)

This document serves as the single source of truth for the codebase architecture, data flows, systems integration, and business logic rules.

---

## 1. Project Purpose
StayNest is a robust, production-grade hotel booking platform backend. It supports multiple users, including guests searching and booking accommodations, and hotel managers (admins) who control hotels, rooms, pricing, and view reports.

---

## 2. High-Level Architecture
The system is built on a modular Spring Boot architecture:
*   **Web Layer:** REST controllers handling HTTP requests and returning unified JSON response envelopes.
*   **Security Layer:** JWT-based stateless authentication filter chain restricting endpoints by user roles.
*   **Service Layer:** Business logic implementation, orchestrating payments, dynamic pricing calculations, and transaction states.
*   **Data Access Layer:** Spring Data JPA repositories with database interactions mapped to PostgreSQL.
*   **Integrations:** Stripe for payments/refunds, Cloudinary for image storage, and SMTP/JavaMailSender for transactional emails.

---

## 3. Folder Responsibilities
The codebase is structured under `com.cybernode.projects.HotelBookingApp`:
*   `advice`: Contains global response wrappers and global exception handlers to standardize API responses.
*   `config`: Application configuration configurations (ModelMapper setup, Stripe initialization, Cloudinary client setup).
*   `controller`: Exposes REST endpoints grouped by audience (admin panel, guest browsing, users, and Stripe webhooks).
*   `dto`: Request and response data transfer objects.
*   `entity`: Database mapping models (Hibernate / JPA annotations).
*   `enums`: Domain enumerations (BookingStatus, Gender, Role).
*   `exception`: Custom runtime exceptions.
*   `repository`: Spring Data interfaces containing raw database query methods.
*   `security`: Security configurations, filter chain, JWT authentication logic, and custom permission checks.
*   `service`: Core business interfaces, with implementors housed in the `impl` subfolder.
*   `strategy`: Dynamic pricing calculation strategy implementations based on the Decorator Pattern.

---

## 4. Technology Stack
*   **Language:** Java 21
*   **Framework:** Spring Boot 3.5.15
*   **Database:** PostgreSQL
*   **Security:** Spring Security (Stateless filter chain, JWT, BCrypt password hashing)
*   **Build Tool:** Maven (mvnw wrapper)
*   **Third-Party Integrations:** Stripe Java SDK (v32.1.0), Cloudinary Java SDK (v2.4.0), Jakarta Validation API, ModelMapper (v3.2.6), Spring AI OpenAI Adapter (v1.1.8)

---

## 5. Dependency Graph
```
[Client App]
     │ (HTTP Rest Requests)
     ▼
[JWT Authentication Filter] (Validates tokens)
     │
     ▼
[Controllers] (REST Mappings)
     │
     ▼
[Services] (Orchestrates Business Logic, Transactions, Stripe, Cloudinary, Mail)
     │
     ▼
[Repositories] (Spring Data JPA)
     │
     ▼
[PostgreSQL Database]
```

---

## 6. Execution Flow
1.  **Bootstrap:** Entry point is `HotelBookingAppApplication.java`.
2.  **Configuration Load:** Config classes initialize configurations such as mapping rules, Stripe API keys, and Cloudinary properties.
3.  **Database Migration:** Hibernate automatically applies updates (`ddl-auto=update`).
4.  **Scheduler Start:** Spring task scheduler starts tracking scheduled tasks, such as the stale bookings sweep scheduler (`expireStaleBookings`) running every 5 minutes.

---

## 7. Request Lifecycle
1.  **Request Arrival:** HTTP Request lands on `/api/v1/*`.
2.  **Security Filtering:** `JWTAuthFilter` intercepts the request:
    *   Extracts the JWT from cookies/headers.
    *   Authenticates principal against database details.
    *   Populates `SecurityContextHolder`.
3.  **Routing:** Spring DispatcherServlet forwards the request to the matching controller endpoint.
4.  **Validation:** Requests annotated with `@Valid` check fields against constraint annotations.
5.  **Service Processing:** Controller delegates logic to the service layer.
6.  **Persistence:** Services access repository interfaces under transactional contexts (`@Transactional`).
7.  **Response/Exception Mapping:** `GlobalResponseHandler` intercepts successful returns. If an exception occurs, `GlobalExceptionHandler` converts it to a standard `ApiError` wrapper.

---

## 8. Database Design

### Schemas & Relationships
*   **User:** Contains identification credentials and a list of roles (`HOTEL_MANAGER`, `GUEST`).
*   **Hotel:** Owned by a `User` (role: `HOTEL_MANAGER`). Has contact information, an array of photo URLs, and denormalized rating fields (`averageRating`, `reviewCount`).
*   **Room:** Linked to a `Hotel` (One-to-Many). Holds price and capacity configurations.
*   **Inventory:** Linked to a `Room` (One-to-Many). Tracks `bookedCount`, `reservedCount`, and `surgeFactor` for a specific calendar date to prevent overbookings.
*   **Booking:** Stores dates, guest records, paid amount, Stripe session ID, status state, and `refundAmount` (BigDecimal). Linked to `User`, `Hotel`, and `Room`.
*   **Guest:** Extra passenger details linked to a booking (Many-to-Many).
*   **Review:** Holds user-submitted rating (1-5) and feedback comments. Linked One-to-One with a verified `Booking` and denormalizes `Hotel` and `User` for fast query indexing.

### Custom HQL/SQL Rules
*   **Pessimistic Locking:** `InventoryRepository` uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` for inventory checks to avoid race conditions during concurrent bookings.

---

## 9. API Contracts

### Authentication (`/api/v1/auth`)
*   `POST /signup` - Registers a new guest or admin.
*   `POST /login` - Returns JWT authorization token.
*   `POST /logout` - Invalidates jwt cookie.

### Hotels & Browsing (`/api/v1/hotels`)
*   `GET /search` - Query available hotels by city, date range, and room count. Supports optional filters: `minPrice`, `maxPrice` (applied to the average min-price over the stay), `minRating` (denormalized `averageRating` >=), and `sortBy` (`PRICE_ASC` (default), `PRICE_DESC`, `RATING_DESC`). Paginated via `page`/`size` query params (size capped at 100).
*   `POST /search/nl` - Conversational natural language hotel search. Uses LLM structured extraction to map user prompt to a `HotelSearchRequest`. Returns interpreted request, a list of missing required fields (if any), and search results.
*   `GET /{hotelId}/info` - Fetch single hotel pricing information.

### Admin Operations (`/api/v1/admin/hotels`)
*   `POST /` - Register a new hotel.
*   `GET /` - List all hotels owned by the admin. Paginated (`@PageableDefault` size 20, sorted `createdAt` desc).
*   `PUT /{hotelId}` - Update hotel details.
*   `PATCH /{hotelId}/activate` - Mark hotel active and initialize future inventories.
*   `GET /{hotelId}/bookings` - List all bookings for a hotel. Paginated (`@PageableDefault` size 20, sorted `createdAt` desc).
*   `POST /{hotelId}/photos` - Upload a hotel photo to Cloudinary and append to the photos array.
*   `GET /bookings/refund-pending` - Retrieve all bookings in `REFUND_PENDING` status for reconciliation. Requires `HOTEL_MANAGER` role.

### Profile & Guests (`/api/v1/users`)
*   `GET /myBookings` - List the current user's bookings. Paginated (`@PageableDefault` size 20, sorted `createdAt` desc).
*   `GET /guests` - List the current user's saved guests. Paginated (`@PageableDefault` size 20, sorted `id` desc).

### Booking lifecycle (`/api/v1/bookings`)
*   `POST /initiate` - Reserve inventory and start booking flow.
*   `POST /{bookingId}/guests` - Associate guest details to reservation.
*   `POST /{bookingId}/payments` - Retrieve Stripe session checkout URL.
*   `POST /{bookingId}/cancel` - Cancel a confirmed booking and initiate stripe refund.

### Diagnostics & Utility Endpoints
*   `GET /test-email?to=...` - Sends a test password-reset email to the specified recipient using JavaMailSender/Brevo configurations. permitAll endpoint.

### Reviews & Ratings (`/api/v1/reviews`)
*   `POST /reviews` - Submit a verified stay review. Requies authentication.
*   `GET /hotels/{hotelId}/reviews` - Get paginated reviews for a hotel. Public.
*   `PUT /reviews/{reviewId}` - Update an existing review. Requires authentication.
*   `DELETE /reviews/{reviewId}` - Delete a review. Requires authentication.
*   `GET /hotels/{hotelId}/ask?question=...` - Ask a natural-language question about a hotel, answered by an LLM grounded only in that hotel's review text (RAG). Public. Returns `{answer, sourceReviewIds}`. Opt-in via `REVIEW_QA_ENABLED`; returns a safe placeholder answer when disabled or on failure.


---

## 10. Key Algorithms & Business Logic

### Dynamic Pricing Calculations (Decorator Pattern)
Pricing computes dynamically for every date in the reservation range:
1.  **Base Price:** Starting room base cost.
2.  **Surge Factor:** Custom date-specific multiplier.
3.  **Occupancy Markup:** Multiplies price by `1.2` if booked occupancy exceeds `80%`.
4.  **Urgency Markup:** Multiplies price by `1.15` if target check-in is within `7 days`.
5.  **Holiday Markup:** Multiplies price by `1.25` during active holidays (currently hardcoded as `true`).
6.  **AI Adjustment (opt-in):** When `pricing.ai.enabled=true`, the outermost decorator calls the AI model (via OpenRouter using Spring AI's OpenAI adapter) with a structured snapshot of market signals (occupancy, days-to-check-in, surge factor, weekend flag, recent booking velocity, hotel rating). The AI returns a multiplier clamped to `[0.80, 1.30]`. Any failure (timeout, bad API key, malformed response, markdown-wrapped JSON) falls back silently to `1.0x`. Default is disabled (`false`).

```java
pricingStrategy = new SurgePricingStrategy(
                  new OccupancyPricingStrategy(
                  new UrgencyPricingStrategy(
                  new HolidayPricingStrategy(
                  new BasePricingStrategy()))));
// When AI enabled (outermost):
pricingStrategy = new AiDynamicPricingStrategy(pricingStrategy, aiPricingService, velocity);
```

**N+1 prevention:** `PricingUpdateService` computes booking velocity once per hotel before iterating its inventory rows and passes it into `PricingService.calculateDynamicPricing(inventory, velocity)`. The no-arg overload (used during booking initiation) defaults velocity to `0`.

### Double-Booking Prevention
*   Inventory records are queried and locked with `PESSIMISTIC_WRITE` mode before changing states.
*   The check-in to check-out inventory dates must be fully available before transitioning booking status from initialized to payment pending.

### Stale Booking Sweep Scheduler
*   Runs every 5 minutes (`0 */5 * * * *`).
*   Checks for pending bookings exceeding the timeout (default: `10 minutes`).
*   Releases reserved count in `Inventory` and changes status to `EXPIRED`.

### Cancellation Policy and Proximity Schedule
*   **Proximity Refund Percentages:**
    *   `>= 7 days` before check-in -> `100%` refund.
    *   `3 to 7 days` before check-in -> `50%` refund.
    *   `< 3 days` before check-in -> `0%` refund.
*   **Check-in Safeguard validation:**
    *   Bookings cannot be cancelled on or after their check-in date. The backend throws an `IllegalStateException` for these attempts.
*   **Stripe Refund Exception Protection:**
    *   Updates the local database status to `REFUND_PENDING` and saves the computed `refundAmount` first.
    *   Fires the Stripe Refund request.
    *   Updates the local status to `CANCELLED` only after Stripe returns successfully.
    *   Stuck transactions (Stripe errors) leave the booking in `REFUND_PENDING` for manual/job reconciliation.

### Pagination (List Endpoints)
*   All previously-unbounded list endpoints (`getAllHotels`, `getMyBookings`, `getAllGuests`, `getAllBookingsByHotelId`) return Spring Data `Page<T>` and accept a `Pageable`.
*   Controllers declare `@PageableDefault` explicitly (size 20) so the API contract does not silently shift if a global default changes. Sort defaults: `createdAt` desc for hotels/bookings, `id` desc for guests (Guest has no timestamp field).
*   `Pageable` is auto-resolved from `?page=&size=&sort=` query params.
*   **Page-size clamping:** Service layer caps page size at `100` via a private `clampPageSize()` helper (rebuilds the `Pageable` with `PageRequest.of` if exceeded), so an oversized `size` cannot defeat the purpose of pagination. The hotel search path clamps separately with `Math.min(size, 100)`.

### Search Filters & Sorting
*   `HotelSearchRequest` carries optional `minPrice`, `maxPrice`, `minRating`, and a `sortBy` enum (`SortOption`: `PRICE_ASC`, `PRICE_DESC`, `RATING_DESC`).
*   Filters are applied directly on the denormalized `HotelMinPrice` table (not via a join back to `Hotel` per request) to preserve the performance the table exists to provide. `minRating` filters on the denormalized `hotel.averageRating`; price bounds are applied to the per-stay average price in a `HAVING` clause (since price is an aggregate `AVG(i.price)`).
*   `HotelMinPriceRepository` exposes three JPQL queries (one per sort option). Price sorts are baked into JPQL `ORDER BY AVG(i.price)` because you cannot sort by an aggregate alias through `Pageable`; `RATING_DESC` is a non-aggregate column and is passed through `Pageable`'s `Sort`. All three share identical optional-filter predicates using the `(:param IS NULL OR ...)` pattern.

### Review-Grounded Q&A (RAG over reviews)
*   **Feature flag:** `review.qa.enabled` (`REVIEW_QA_ENABLED`, default `false`). All feature code no-ops when disabled.
*   **Indexing:** `ReviewEmbeddingService` mirrors each review's text into a pgvector `vector_store` table. It is called best-effort (like `NotificationService`) at the end of `createReview`/`updateReview` (upsert by review id) and `deleteReview` (delete by id). Each `Document` carries `reviewId`, `hotelId`, and `rating` metadata and is keyed by the review id, so re-indexing an edited review overwrites cleanly and source ids round-trip back to `Long`.
*   **Embeddings provider split:** Chat goes through OpenRouter, but OpenRouter has no embeddings endpoint — so `spring.ai.openai.embedding.base-url` is explicitly pointed back at `https://api.openai.com` with its own `OPENAI_API_KEY` and `text-embedding-3-small` (1536 dims).
*   **Query path (`HotelQaService.ask`):** metadata-filters the vector store by `hotelId`, runs a top-K similarity search (`review.qa.top-k`, `review.qa.similarity-threshold`), stuffs the matched review snippets into a system-constrained prompt ("answer ONLY from these reviews"), and calls the shared `ChatClient`. Returns `{answer, sourceReviewIds}`.
*   **Safety:** every branch degrades gracefully — disabled flag, no matches, or any exception each return a safe placeholder answer with an empty source list; the Q&A path never throws to the caller.
*   **Operational caveat:** the pgvector starter's autoconfiguration + `initialize-schema=true` run at startup **regardless of the feature flag**, so the app now requires the Postgres `vector` extension to be installed to boot. See Known Limitations.

### Conversational Search (Structured DTO Extraction)
A conversational search interface that parses free-text user queries into structured search requests.
*   **Feature flag:** `search.nl.enabled` (`NL_SEARCH_ENABLED`, default `false`).
*   **Extraction:** Uses Spring AI's `.entity(HotelSearchRequest.class)` chat mapping. It extracts search filters (city, date range, capacity, price/rating bounds, sorting) and resolves relative expressions (e.g. "next weekend") using a system prompt anchored to the current system date.
*   **Strict Validation:** The LLM is instructed not to make assumptions. The service validates the presence of required search inputs (`city`, `startDate`, `endDate`, `roomsCount`). If any are missing, it short-circuits to return a list of missing fields, prompting the user for clarification instead of guessing.

### Review Q&A (RAG over Reviews)
A retrieval-augmented Q&A station layered on top of the reviews feature, gated behind the `review.qa.enabled` flag (default off).
*   **Indexing (`ReviewEmbeddingService`):** On review create/update, the comment text is embedded and upserted into a pgvector store (Spring AI's auto-managed `vector_store` table), keyed by the review id, with `hotelId`/`rating` metadata. On delete, the vector is removed. Best-effort like `NotificationService` — wrapped in try/catch and a no-op when disabled or the comment is blank, so it **never breaks the review transaction**.
*   **Answering (`HotelQaService`):** `similaritySearch` retrieves the top-K (`review.qa.top-k`, default 5) review chunks for that hotel above `review.qa.similarity-threshold` (default 0.5), filtered by `hotelId` metadata. The excerpts are passed to the OpenRouter chat model with a system prompt constraining it to answer ONLY from the provided reviews. Returns the answer plus the source review ids.
*   **Safety / never-break-the-flow:** Disabled flag, empty matches, and any exception each return a graceful canned `HotelQaResponseDto` rather than throwing — mirroring the `AiPricingService` fallback discipline.
*   **Embeddings go direct to OpenAI, not OpenRouter:** OpenRouter has no embeddings endpoint, so `spring.ai.openai.embedding.base-url` is explicitly overridden back to `https://api.openai.com` (model `text-embedding-3-small`, 1536 dims) while chat stays pointed at OpenRouter. Requires the separate `OPENAI_API_KEY`.
*   **Infra dependency:** The pgvector store auto-configures at startup regardless of the feature flag (the flag only gates StayNest's own code, not Spring AI autoconfig). With `spring.ai.vectorstore.pgvector.initialize-schema=true`, the app now requires the Postgres `vector` extension to be present to boot — provided directly by the official `pgvector/pgvector:pg16` image (see Deployment).

### Verified-Stay Reviews & Average Rating Recalculation
*   **Submission Guards:** Submitting a review requires:
    *   Authenticated user is the creator of the booking.
    *   Booking status must be `CONFIRMED`.
    *   Checkout date must be in the past.
    *   Review does not already exist (guarded by exists checks and DB unique constraint on `booking_id`).
*   **Average Rating Recalculation:** Triggered in the same transaction after a review is saved. Updates `averageRating` and `reviewCount` in the `Hotel` entity dynamically.
*   **Privacy Protection:** Mask passenger name (first name only) in public API review outputs.

---

## 11. Configuration
*   **StripeConfig:** Standard static key binding.
*   **CloudinaryConfig:** Instantiates connection client mapping details.
*   **MapperConfig:** Configures ModelMapper instance.
*   **WebSecurityConfig:** Declares stateless filter authorization limits.

---

## 12. Environment Variables
*   `JWT_SECRET_KEY` - Token encryption key.
*   `STRIPE_SECRET_KEY` - Stripe secret key.
*   `STRIPE_WEBHOOK_SECRET` - Stripe webhook secret.
*   `CLOUDINARY_CLOUD_NAME` - Cloudinary cloud identifier.
*   `CLOUDINARY_API_KEY` - Cloudinary client key.
*   `CLOUDINARY_API_SECRET` - Cloudinary API secret.
*   `MAIL_HOST` - SMTP host server (default: `smtp.mailtrap.io`).
*   `MAIL_PORT` - SMTP port (default: `2525`).
*   `MAIL_USERNAME` - SMTP authentication username.
*   `MAIL_PASSWORD` - SMTP authentication password.
*   `MAIL_FROM` - Sender address (default: `noreply@staynest.com`).
*   `OPENROUTER_API_KEY` - OpenRouter API key for the chat model (AI dynamic pricing + review Q&A answering).
*   `OPENROUTER_BASE_URL` - OpenRouter API base URL (default: `https://openrouter.ai/api/v1`).
*   `AI_PRICING_ENABLED` - Toggles the AI dynamic-pricing strategy (default: `false`).
*   `AI_PRICING_MODEL` - Chat model id for pricing (default: `google/gemini-2.0-flash`).
*   `OPENAI_API_KEY` - OpenAI API key used *only* for review embeddings (OpenRouter has no embeddings endpoint). Required when `REVIEW_QA_ENABLED=true`.
*   `REVIEW_QA_ENABLED` - Toggles the review-embedding indexing + Q&A endpoint (default: `false`).

---

## 13. Coding Standards
*   Clean encapsulation with interfaces and implementations.
*   Zero logic in controllers.
*   Environment variables are preferred over hardcoded secrets.
*   Standard HTTP status codes are returned (e.g., 201 Created, 204 No Content, 401 Unauthorized, 403 Forbidden).

---

## 14. Naming Conventions
*   **REST Paths:** Plural nouns, lowercase, kebab-case (e.g. `/admin/hotels/{hotelId}/photos`).
*   **Repositories:** Named after entity (e.g. `BookingRepository`).
*   **DTOs:** End with `Dto` or `Request`/`Response` naming details.

---

## 15. Reusable Patterns
*   **Decorator:** Utilized in dynamic pricing computations.
*   **Global Response Wrapper:** Standardizes all API returns under a generic response envelope.

---

## 16. Error Handling
Unified JSON error wrapper formatting:
```json
{
  "status": "NOT_FOUND",
  "message": "Resource not found with ID: 1",
  "errors": ["Resource not found"]
}
```
Mapped through `GlobalExceptionHandler.java`.

---

## 17. Security Practices
*   Passwords are encoded using `BCryptPasswordEncoder`.
*   JWT values are placed inside HTTP-only secure cookies.
*   Endpoint security mappings are restricted based on user role definitions (e.g., `HOTEL_MANAGER`).

---

## 18. Performance Considerations
*   Pessimistic write locks limit database updates to sequential execution for matching inventory slots.
*   Eager loading is applied to user roles, while associations like hotel rooms use lazy loading to keep payloads small.

---

## 19. External Integrations
*   **Stripe SDK:** Facilitates customer setup, session redirection, payment collection, and refund operations.
*   **Cloudinary SDK:** Hosts media images and returns secure HTTPS links.
*   **JavaMailSender:** Delivers emails to keep users informed about payments, holds, and booking status updates.
*   **OpenRouter (via Spring AI OpenAI adapter):** Chat completions backing AI dynamic pricing and review Q&A.
*   **OpenAI Embeddings (direct):** `text-embedding-3-small` for review vectors; called directly at `api.openai.com` because OpenRouter has no embeddings endpoint.
*   **pgvector (PostgreSQL extension):** Vector store for review embeddings, exposed as a Spring AI `VectorStore` bean; the `vector_store` table is auto-created via `initialize-schema=true`.
*   **OpenRouter via Spring AI:** Optional AI pricing strategy. Spring AI's OpenAI adapter (`spring-ai-starter-model-openai`) points its base URL at `https://openrouter.ai/api/v1` to access any OpenRouter-hosted model. Only activated when `AI_PRICING_ENABLED=true`.
*   **pgvector + OpenAI embeddings via Spring AI:** Powers review-grounded Q&A. `spring-ai-starter-vector-store-pgvector` provides an auto-managed `vector_store` table + `VectorStore` bean; embeddings are generated by OpenAI directly (`text-embedding-3-small`), while chat generation reuses the OpenRouter `ChatClient`. Only exercised when `REVIEW_QA_ENABLED=true`, but the vector store autoconfig loads at startup regardless (requires the `vector` extension).

---

## 20. Testing Strategy
*   Integration tests in `HotelBookingAppApplicationTests` verify correct application contexts load.
*   Unit tests in `ValidationTest` evaluate validation constraints for signup requests.

---

## 21. CI/CD Pipeline
*   Managed via Maven.
*   Automated compilation and test steps using `./mvnw clean test`.

---

## 22. Deployment
*   Fully cloud-compatible.
*   Builds into a single runnable JAR via the `spring-boot-maven-plugin`.
*   External credentials bind at launch through system environment variables.
*   **Local Postgres + pgvector:** `docker compose up -d` pulls the official `pgvector/pgvector:pg16` image and starts it on `localhost:5432` with the same credentials as `application.properties`. Spring AI's autoconfiguration runs `CREATE EXTENSION IF NOT EXISTS vector;` (along with `hstore` and `uuid-ossp`) automatically on startup since we connect as the `postgres` superuser. Required for the review Q&A feature (and for the app to boot once the pgvector store is on the classpath). `docker compose down -v` wipes the volume.

---

## 23. Common Commands
*   **Compile:** `./mvnw compile`
*   **Run Tests:** `./mvnw test`
*   **Local Run:** `./mvnw spring-boot:run`

---

## 24. Important Files
*   `pom.xml` - Project dependencies.
*   `application.properties` - Framework configurations.
*   `.env` - Local development environment secrets.

---

## 25. Known Limitations
*   The Holiday pricing check is mock-implemented and always evaluates to `true`.
*   Dynamic currency adjustments are fixed to `inr`.
*   AI dynamic pricing is opt-in and requires a valid `OPENROUTER_API_KEY`. When disabled (default), the pricing pipeline is identical to before the feature was added.
*   Review Q&A is opt-in (`REVIEW_QA_ENABLED`, default off) and requires `OPENAI_API_KEY` for embeddings. **However**, unlike the pricing flag, the pgvector starter's autoconfiguration and `initialize-schema=true` run at application startup *irrespective* of the flag — so the app now requires the PostgreSQL `vector` extension (`CREATE EXTENSION vector`) to be installed to boot at all. To make the flag gate infrastructure too, tie `spring.ai.vectorstore.pgvector.initialize-schema` to the flag and/or conditionally exclude the pgvector autoconfiguration when disabled.
*   Reviews written *before* the feature was enabled are not retroactively embedded; only reviews created/updated while `REVIEW_QA_ENABLED=true` enter the vector store (no backfill job yet).

---

## 26. Assumptions & Unknowns
*   Assumes a local PostgreSQL instance is running on port 5432 during testing unless overrides are supplied.
*   Assumes third-party APIs (Stripe, Cloudinary) remain active.

---

## 27. Data Flow Overview
```
[Guest Client] ──(Init Booking Request)──► [Booking Controller]
                                                  │
                                                  ▼
[Inventory Repo] ◄──(Pessimistic Write Lock)── [Booking Service] ──(Stripe Checkout Session)──► [Stripe API]
                                                  │
                                                  ▼
[Stripe Webhook] ──(Checkout Completed Event)──► [Webhook Controller]
                                                  │
                                                  ▼
[Booking Service] ──(Confirm State & Save)──► [Inventory Repo] (Updates bookedCount)
        │
        ▼
[Notification Service] ──(Send Confirmation Email)──► [Guest SMTP Inbox]
```

---

## 28. Maintenance Guidelines
*   **Adding API Endpoints:** Ensure requests use validation constraints, return results using response wrappers, and restrict paths within security configurations.
*   **Updating DB Schemas:** Keep entity properties mapped to matching schema updates and update matching repository methods.
*   **Adding Pricing Strategies:** Implement `PricingStrategy` and register it inside `PricingService`.
