# URL Shortener

A production-oriented URL Shortener REST API built with **Java, Spring Boot, PostgreSQL, Redis, and Apache Kafka**.

The application provides URL shortening, URL expansion, Redis cache-aside lookup, click analytics, request-context capture, and asynchronous analytics event publishing through Kafka.

> **Branch:** `integrate-kafka`

---

## Table of Contents

* [Overview](#overview)
* [Features](#features)
* [Architecture](#architecture)
* [Technology Stack](#technology-stack)
* [Project Structure](#project-structure)
* [API Endpoints](#api-endpoints)
* [URL Shortening Flow](#url-shortening-flow)
* [URL Expansion Flow](#url-expansion-flow)
* [Analytics Flow](#analytics-flow)
* [Kafka Integration](#kafka-integration)
* [Redis Caching](#redis-caching)
* [Short Code Generation](#short-code-generation)
* [Database](#database)
* [Error Handling](#error-handling)
* [Configuration](#configuration)
* [Prerequisites](#prerequisites)
* [Running the Application](#running-the-application)
* [Running Kafka](#running-kafka)
* [Testing](#testing)
* [Unit Testing Strategy](#unit-testing-strategy)
* [SOLID Principles](#solid-principles)
* [Production Considerations](#production-considerations)
* [Known Limitations](#known-limitations)
* [Future Improvements](#future-improvements)

---

# Overview

The URL Shortener converts long URLs into short, easy-to-share codes.

Example:

```text
Original URL:
https://www.example.com/products/software-engineering/url-shortener

Short URL:
http://localhost:9000/api/urls/abc12345
```

The application uses:

* **PostgreSQL** for persistent URL and analytics data
* **Redis** for low-latency URL lookups
* **Apache Kafka** for asynchronous analytics events
* **Spring Data JPA** for persistence
* **Spring Boot** for REST APIs and application configuration
* **MapStruct** for DTO/entity mapping
* **JUnit 5 + Mockito** for unit testing

The service follows a layered architecture with clear separation between controllers, services, repositories, caching, request context, Kafka messaging, mapping, and utilities.

---

# Features

## URL Management

* Shorten long URLs
* Generate an 8-character hexadecimal short code
* Expand a short code to the original URL
* Retrieve all stored URLs
* Detect missing short codes
* Persist URL mappings in PostgreSQL

## Redis Caching

* Cache short-code → original-URL mappings
* Cache-aside pattern
* Cache hit avoids database access
* Cache miss falls back to PostgreSQL
* Database results are written back to Redis
* Redis failures do not prevent database lookup

## Analytics

* Track URL clicks
* Track referrer
* Track user-agent
* Track client IP
* Track last access time
* Maintain total click count
* Retrieve analytics by short code

## Kafka

* Publish URL click events to Kafka
* JSON-based analytics event payload
* Kafka topic: `url-click-events`
* Three Kafka partitions
* Short code used as the Kafka message key
* Kafka consumer delegates persistence to `AnalyticsService`

## Error Handling

* URL-not-found handling
* Invalid URL handling
* HTTP 404 responses
* HTTP 400 responses
* HTTP 500 responses
* Logging at DEBUG, INFO, WARN and ERROR levels

## Testing

Unit tests cover:

* Controllers
* Services
* Mappers
* Redis cache
* HTTP request context
* Short-code generator
* Kafka components

---

# Architecture

```text
                         ┌──────────────────────┐
                         │       Client         │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │    REST Controllers  │
                         │                      │
                         │  UrlController       │
                         │  AnalyticsController│
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     Service Layer    │
                         │                      │
                         │  UrlService          │
                         │  AnalyticsService    │
                         └───────┬───────┬──────┘
                                 │       │
                   ┌─────────────┘       └─────────────┐
                   ▼                                   ▼
          ┌─────────────────┐                  ┌─────────────────┐
          │      Redis      │                  │   PostgreSQL    │
          │                 │                  │                 │
          │ URL Cache       │                  │ URL Data        │
          │ Cache-Aside     │                  │ Analytics Data  │
          └─────────────────┘                  └─────────────────┘

                         Analytics Events
                                │
                                ▼
                         ┌─────────────────┐
                         │      Kafka      │
                         │                 │
                         │ url-click-events│
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Kafka Consumer   │
                         │                 │
                         │ AnalyticsEvent   │
                         │ Consumer         │
                         └────────┬────────┘
                                  │
                                  ▼
                         AnalyticsService
                                  │
                                  ▼
                             PostgreSQL
```

---

# Technology Stack

| Component        | Technology                               |
| ---------------- | ---------------------------------------- |
| Language         | Java 17                                  |
| Framework        | Spring Boot 4.1.1                        |
| Web              | Spring MVC                               |
| Persistence      | Spring Data JPA                          |
| Database         | PostgreSQL                               |
| Cache            | Redis                                    |
| Messaging        | Apache Kafka                             |
| Mapping          | MapStruct                                |
| Validation       | Jakarta Validation / Hibernate Validator |
| Logging          | SLF4J / Logback                          |
| Testing          | JUnit 5 / Mockito                        |
| Build            | Maven                                    |
| Containerization | Docker                                   |

---

# Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/url/shortener/
│   │       │
│   │       ├── config/
│   │       │   ├── KafkaConfig.java
│   │       │   └── RedisConfig.java
│   │       │
│   │       ├── constants/
│   │       │
│   │       ├── controller/
│   │       │   ├── UrlController.java
│   │       │   └── AnalyticsController.java
│   │       │
│   │       ├── dto/
│   │       │   ├── UrlDTO.java
│   │       │   ├── AnalyticsDTO.java
│   │       │   ├── AnalyticsEvent.java
│   │       │   └── ShortenUrlRequest.java
│   │       │
│   │       ├── entity/
│   │       │   ├── UrlEntity.java
│   │       │   ├── AnalyticsEntity.java
│   │       │   └── ExpandUrlResponse.java
│   │       │
│   │       ├── exceptionhandler/
│   │       │
│   │       ├── exceptions/
│   │       │   ├── UrlNotFoundException.java
│   │       │   └── InvalidUrlException.java
│   │       │
│   │       ├── kafka/
│   │       │   ├── AnalyticsEventProducer.java
│   │       │   └── AnalyticsEventConsumer.java
│   │       │
│   │       ├── mapper/
│   │       │   ├── UrlMapper.java
│   │       │   └── AnalyticsMapper.java
│   │       │
│   │       ├── repository/
│   │       │   ├── UrlRepository.java
│   │       │   └── AnalyticsRepository.java
│   │       │
│   │       ├── service/
│   │       │   ├── UrlService.java
│   │       │   ├── UrlServiceImpl.java
│   │       │   ├── AnalyticsService.java
│   │       │   ├── AnalyticsServiceImpl.java
│   │       │   │
│   │       │   ├── cache/
│   │       │   │   ├── CacheService.java
│   │       │   │   └── RedisCacheService.java
│   │       │   │
│   │       │   └── context/
│   │       │       ├── RequestContext.java
│   │       │       └── HttpRequestContext.java
│   │       │
│   │       └── util/
│   │           └── ShortCodeGenerator.java
│   │
│   └── resources/
│       └── application.properties
│
└── test/
    └── java/
        └── com/url/shortener/
            ├── controller/
            ├── service/
            ├── mapper/
            ├── cache/
            ├── context/
            └── util/
```

---

# API Endpoints

Base URL:

```text
http://localhost:9000
```

## 1. Shorten URL

```http
POST /api/urls/shorten
Content-Type: application/json
```

Request:

```json
{
  "originalUrl": "https://www.example.com/very/long/url"
}
```

Successful response:

```http
201 Created
```

Example:

```json
{
  "id": 1,
  "originalUrl": "https://www.example.com/very/long/url",
  "shortCode": "abc12345",
  "createdAt": "2026-08-28T10:30:00"
}
```

Possible responses:

```text
201 Created
400 Bad Request
500 Internal Server Error
```

---

## 2. Expand URL

```http
GET /api/urls/{shortCode}
```

Example:

```http
GET /api/urls/abc12345
```

Response:

```http
200 OK
```

```json
{
  "originalUrl": "https://www.example.com/very/long/url"
}
```

Possible responses:

```text
200 OK
404 Not Found
500 Internal Server Error
```

---

## 3. Get URL Statistics

```http
GET /api/urls/{shortCode}/stats
```

Example:

```http
GET /api/urls/abc12345/stats
```

Response:

```json
{
  "shortCode": "abc12345",
  "originalUrl": "https://www.example.com/very/long/url",
  "totalClicks": 25,
  "lastAccessed": "2026-08-28T10:45:00",
  "lastReferrer": "https://google.com",
  "lastUserAgent": "Mozilla/5.0"
}
```

Possible responses:

```text
200 OK
404 Not Found
500 Internal Server Error
```

---

## 4. Get All URLs

```http
GET /api/urls
```

Response:

```json
[
  {
    "id": 1,
    "originalUrl": "https://google.com",
    "shortCode": "abc12345"
  },
  {
    "id": 2,
    "originalUrl": "https://amazon.com",
    "shortCode": "xyz98765"
  }
]
```

---

## 5. Analytics Expansion Endpoint

```http
GET /api/analytics/expand/{shortCode}/url
```

Example:

```http
GET /api/analytics/expand/abc12345/url
```

This endpoint:

1. Expands the short URL
2. Reads request metadata
3. Publishes an analytics event to Kafka
4. Returns the original URL

Response:

```json
{
  "originalUrl": "https://www.example.com/very/long/url"
}
```

---

# URL Shortening Flow

```text
POST /api/urls/shorten
          │
          ▼
   UrlController
          │
          ▼
   UrlServiceImpl
          │
          ├── Validate URL
          │
          ├── Generate short code
          │
          ├── Check existing short code
          │
          ├── Save URL
          │
          ├── Cache mapping in Redis
          │
          └── Map DTO → Entity
                  │
                  ▼
             HTTP 201
```

The URL service validates the input, generates the short code, persists the URL and stores the mapping in Redis.

---

# URL Expansion Flow

The application uses the **Cache-Aside Pattern**.

```text
GET /api/urls/{shortCode}
            │
            ▼
      UrlServiceImpl
            │
            ▼
       Redis Cache
         /      \
       HIT      MISS
        │         │
        │         ▼
        │    PostgreSQL
        │         │
        │         ▼
        │    Update Redis
        │         │
        └────┬────┘
             ▼
       Original URL
```

The service first checks Redis. If the value isn't available, it queries PostgreSQL and populates Redis for subsequent requests.

This reduces database traffic for frequently accessed URLs.

---

# Analytics Flow

Analytics contains:

* short code
* referrer
* user agent
* client IP
* event ID
* event timestamp
* total clicks
* last access time

The Kafka event object is represented by `AnalyticsEvent`.

```text
Client
  │
  ▼
AnalyticsController
  │
  ├── Expand URL
  │
  ├── Read Referer
  ├── Read User-Agent
  ├── Read Client IP
  │
  ▼
AnalyticsEventProducer
  │
  ▼
Kafka
  │
  │  url-click-events
  ▼
AnalyticsEventConsumer
  │
  ▼
AnalyticsService
  │
  ▼
PostgreSQL
```

---

# Kafka Integration

## Topic

```text
url-click-events
```

The current Kafka configuration creates the topic with:

```text
Partitions: 3
Replicas:   1
```

The topic configuration is implemented through a Spring `NewTopic` bean.

## Producer

`AnalyticsEventProducer` creates an event containing:

```json
{
  "eventId": "uuid",
  "shortCode": "abc12345",
  "referrer": "https://google.com",
  "userAgent": "Mozilla/5.0",
  "clientIp": "127.0.0.1",
  "eventTime": "2026-08-28T10:30:00"
}
```

The short code is used as the Kafka message key:

```text
Kafka key = shortCode
```

The producer publishes the event using `KafkaTemplate`.

## Consumer

`AnalyticsEventConsumer` listens to:

```text
url-click-events
```

and delegates the event to:

```text
AnalyticsService.saveAnalytics(...)
```

This keeps Kafka-specific processing separate from analytics persistence logic.

---

# Redis Caching

Redis is used as a distributed cache for URL mappings.

The current implementation uses:

```text
RedisTemplate<String, String>
```

with string serializers for both keys and values.

### Cache operations

```text
get()
put()
invalidate()
exists()
```

Redis failures are handled gracefully by `RedisCacheService`.

For example, if Redis is unavailable during an URL expansion:

```text
Redis
  │
  X unavailable
  │
  ▼
PostgreSQL fallback
```

This prevents Redis availability from becoming a hard dependency for URL retrieval.

---

# Short Code Generation

The default implementation generates an **8-character hexadecimal short code**.

Algorithm:

```text
Original URL
     │
     ▼
String.hashCode()
     │
     ▼
Integer.toHexString()
     │
     ▼
Pad / truncate
     │
     ▼
8-character short code
```

Example:

```text
https://google.com
        ↓
hashCode()
        ↓
hexadecimal representation
        ↓
a1b2c3d4
```

The application also contains an alternative Base62 implementation using:

```text
0-9
a-z
A-Z
```

The current implementation is hash-based and therefore should not be considered collision-proof.

---

# Database

PostgreSQL is used as the persistent data store.

## URL Repository

```text
UrlRepository
      │
      └── JpaRepository<UrlDTO, Long>
```

It provides:

```java
Optional<UrlDTO> findByShortCode(String shortCode);
```

## Analytics Repository

```text
AnalyticsRepository
      │
      └── JpaRepository<AnalyticsDTO, Long>
```

It provides:

```java
Optional<AnalyticsDTO> findByShortCode(String shortCode);
```

---

# Analytics Persistence

When an analytics event is consumed:

```text
AnalyticsEvent
      │
      ▼
AnalyticsService
      │
      ├── Find existing analytics
      │
      ├── Create record if required
      │
      ├── Increment totalClicks
      │
      ├── Update lastAccessed
      │
      ├── Update referrer
      │
      ├── Update user-agent
      │
      ├── Read cached original URL
      │
      └── Save analytics
```

`AnalyticsServiceImpl` performs these operations inside a transaction.

---

# Request Context

HTTP request information is abstracted behind `RequestContext`.

The current implementation captures:

```text
Client IP
Referer
User-Agent
```

For client IP:

```text
X-Forwarded-For
       │
       ├── Present → first IP
       │
       └── Missing → request.getRemoteAddr()
```

This abstraction is request-scoped and wraps `HttpServletRequest`.

---

# Error Handling

The application uses custom exceptions including:

```text
UrlNotFoundException
InvalidUrlException
```

Typical HTTP mapping:

| Exception                    | HTTP Status |
| ---------------------------- | ----------: |
| Invalid URL                  |         400 |
| URL not found                |         404 |
| Unexpected application error |         500 |

Controllers log failures and return appropriate `ResponseEntity` responses.

---

# Configuration

Current default application configuration:

```properties
spring.application.name=url-shortener

server.port=9000

spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortener
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.timeout=60000
```

The current repository configuration uses PostgreSQL on port `5432`, Redis on port `6379`, and the application on port `9000`.

For Kafka, configure:

```properties
spring.kafka.bootstrap-servers=localhost:9092

spring.kafka.consumer.group-id=url-shortener-analytics
spring.kafka.consumer.auto-offset-reset=earliest

kafka.topic.url-click-events=url-click-events
```

For JSON serialization/deserialization, use the serializer/deserializer versions compatible with the Spring Kafka version used by the application.

---

# Prerequisites

Install:

* Java 17+
* Maven 3.9+
* PostgreSQL
* Redis
* Apache Kafka

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

# Running the Application

## 1. Start PostgreSQL

Create the database:

```sql
CREATE DATABASE urlshortener;
```

Default credentials:

```text
Username: postgres
Password: postgres
Port: 5432
```

---

## 2. Start Redis

Default:

```text
localhost:6379
```

Verify Redis:

```bash
redis-cli ping
```

Expected:

```text
PONG
```

---

## 3. Start Kafka

Make sure Kafka is running on:

```text
localhost:9092
```

Create the topic if required:

```bash
docker exec -it kafka \
/opt/kafka/bin/kafka-topics.sh \
--create \
--topic url-click-events \
--bootstrap-server localhost:9092 \
--partitions 3 \
--replication-factor 1
```

If the topic already exists, Kafka will report that it already exists.

---

## 4. Run the Spring Boot application

Using Maven:

```bash
mvn clean spring-boot:run
```

Or:

```bash
./mvnw clean spring-boot:run
```

Application:

```text
http://localhost:9000
```

---

# Testing the API

## Shorten a URL

```bash
curl -X POST \
  http://localhost:9000/api/urls/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://www.google.com"}'
```

Example response:

```json
{
  "id": 1,
  "originalUrl": "https://www.google.com",
  "shortCode": "abc12345"
}
```

---

## Expand the URL

```bash
curl \
  http://localhost:9000/api/urls/abc12345
```

---

## Retrieve analytics

```bash
curl \
  http://localhost:9000/api/urls/abc12345/stats
```

---

## Retrieve all URLs

```bash
curl \
  http://localhost:9000/api/urls
```

---

# Kafka Verification

Start a Kafka consumer:

```bash
docker exec -it kafka \
/opt/kafka/bin/kafka-console-consumer.sh \
--bootstrap-server localhost:9092 \
--topic url-click-events \
--from-beginning
```

After an analytics-enabled URL expansion, an event should appear:

```json
{
  "eventId": "7c5f...",
  "shortCode": "abc12345",
  "referrer": "https://google.com",
  "userAgent": "Mozilla/5.0",
  "clientIp": "127.0.0.1",
  "eventTime": "2026-08-28T10:30:00"
}
```

---

# Testing

Run all tests:

```bash
mvn test
```

or:

```bash
./mvnw test
```

Run a specific test:

```bash
mvn test -Dtest=UrlServiceImplTest
```

---

# Unit Testing Strategy

The application follows a layered unit-testing approach.

## Controller Tests

Test:

* HTTP status codes
* JSON responses
* service delegation
* exception handling
* empty responses
* request context interaction

## Service Tests

Test:

* URL validation
* short-code generation
* repository interactions
* cache hit
* cache miss
* database fallback
* URL-not-found behavior
* analytics creation
* analytics updates

## Mapper Tests

Test:

* DTO → Entity
* Entity → DTO
* null handling
* field-by-field mapping

## Redis Cache Tests

Test:

* cache hit
* cache miss
* put
* invalidate
* exists
* Redis exceptions

## Request Context Tests

Test:

* Referer
* User-Agent
* X-Forwarded-For
* Remote address fallback

## Utility Tests

Test:

* 8-character code generation
* deterministic generation
* invalid input
* Base62 generation
* valid character set

## Kafka Tests

Test:

* event creation
* topic/key/value passed to Kafka producer
* consumer delegation
* analytics service invocation

---

# SOLID Principles

## Single Responsibility Principle

Responsibilities are separated:

```text
Controller
    → HTTP handling

Service
    → Business logic

Repository
    → Persistence

RedisCacheService
    → Cache management

AnalyticsEventProducer
    → Kafka publishing

AnalyticsEventConsumer
    → Kafka consumption

ShortCodeGenerator
    → Short-code generation

HttpRequestContext
    → HTTP request metadata
```

---

## Open/Closed Principle

The cache is represented through the `CacheService` abstraction.

```text
CacheService
    │
    └── RedisCacheService
```

Another cache implementation can be introduced without changing `UrlServiceImpl`.

---

## Liskov Substitution Principle

Service and cache implementations are accessed through interfaces, allowing alternative implementations without changing consumers.

---

## Interface Segregation Principle

The `RequestContext` interface exposes only the request information required by the application:

```text
getClientIp()
getReferer()
getUserAgent()
```

---

## Dependency Inversion Principle

The application uses constructor injection and service/repository interfaces.

Example:

```text
UrlController
      │
      ▼
UrlService
      │
      ▼
UrlServiceImpl
```

Dependencies are supplied by Spring rather than created directly inside business classes.

---

# Transaction Management

The service layer uses Spring transactions.

URL persistence:

```java
@Transactional
```

Read-only operations:

```java
@Transactional(readOnly = true)
```

Analytics persistence:

```java
@Transactional
```

This provides transaction boundaries around database operations.

---

# Caching Strategy

The application uses a Cache-Aside strategy:

```text
              Request
                 │
                 ▼
              Redis
             /     \
          HIT       MISS
           │          │
           │          ▼
           │       PostgreSQL
           │          │
           │          ▼
           │        Redis
           │          │
           └────┬─────┘
                ▼
          Return URL
```

Benefits:

* Lower database load
* Faster repeated lookups
* Redis can scale independently
* Cache failures can fall back to PostgreSQL

---

# Kafka Design

Kafka introduces an asynchronous boundary between click capture and analytics persistence.

```text
HTTP Request
     │
     ▼
Capture metadata
     │
     ▼
Kafka Producer
     │
     ▼
Kafka Topic
     │
     ▼
Kafka Consumer
     │
     ▼
Analytics Service
     │
     ▼
PostgreSQL
```

Benefits:

* Decouples analytics processing
* Allows consumer scaling
* Absorbs traffic spikes
* Keeps analytics processing independent from the REST layer
* Enables future analytics consumers

---

# Production Considerations

The current implementation is a strong demonstration of the core architecture, but several areas should be enhanced before production deployment.

## Short-code collision handling

The current generator uses:

```java
String.hashCode()
```

and converts the result to hexadecimal.

Java hash codes are not globally unique.

Production alternatives include:

* Database sequence + Base62
* Collision detection and retry
* Random cryptographically strong identifiers
* Snowflake-style IDs + Base62

---

## Kafka Idempotency

Kafka consumers can process a message more than once.

The `AnalyticsEvent` contains an `eventId`, which can be used as an idempotency key.

Recommended production design:

```text
Kafka Event
     │
     ▼
Check eventId
     │
 ┌───┴────┐
 │        │
Exists   New
 │        │
Ignore   Process
          │
          ▼
       Persist
```

A unique constraint on `event_id` should be used to prevent duplicate analytics processing.

---

## Kafka Retry and Dead Letter Topic

Production deployment should configure:

* Consumer retries
* Exponential backoff
* Dead Letter Topic
* Poison-message handling
* Monitoring and alerting

Example:

```text
url-click-events
       │
       ▼
    Consumer
       │
       ├── Success
       │
       └── Failure
             │
             ▼
        Retry / Backoff
             │
             ▼
       Dead Letter Topic
```

---

## Kafka Availability

The REST API should define an explicit policy for Kafka failures.

Possible policies:

### Availability-first

If analytics publishing fails:

```text
Log failure
Return URL response
```

### Durability-first

If analytics is mandatory:

```text
Kafka unavailable
      ↓
Retry
      ↓
Return failure if publishing cannot be guaranteed
```

For a URL-shortening system, availability-first is generally preferable because analytics should not prevent URL expansion.

---

# Known Limitations

The following limitations are intentionally documented so the implementation and README remain aligned.

### 1. Kafka integration is currently partial

`AnalyticsController` publishes analytics events through `AnalyticsEventProducer`, while the current `UrlController.expandUrl()` still calls `AnalyticsService.saveAnalytics()` directly.

Therefore, the complete production flow should eventually standardize on:

```text
URL expansion
      ↓
Kafka Producer
      ↓
Kafka
      ↓
Kafka Consumer
      ↓
AnalyticsService
```

rather than maintaining two analytics paths.

### 2. Kafka dependency/configuration must remain aligned

The repository's Maven configuration currently declares Spring Boot 4.1.1 and Java 17, but the compiler plugin explicitly contains Java 11 source/target settings. These should be aligned.

Recommended:

```xml
<source>17</source>
<target>17</target>
```

or use the configured Maven release property consistently.

### 3. Redis does not currently define a TTL

The current Redis implementation stores URL mappings without an explicit expiration time.

Production systems should consider:

```text
TTL = configurable
```

to prevent unbounded cache growth.

### 4. Hash-based short codes can collide

The current implementation explicitly uses `String.hashCode()`.

Collision detection should be added before production deployment.

### 5. Kafka event idempotency is not yet enforced

Although `AnalyticsEvent` contains an `eventId`, the current consumer delegates directly to `saveAnalytics()` without checking whether that event has already been processed.

---

# Future Improvements

Recommended enhancements:

* [ ] Complete Kafka integration for the primary URL expansion endpoint
* [ ] Add Kafka retry policy
* [ ] Add Dead Letter Topic
* [ ] Add analytics event idempotency
* [ ] Add Redis TTL
* [ ] Replace hash-based short-code generation
* [ ] Add collision detection
* [ ] Add URL expiration
* [ ] Add rate limiting
* [ ] Add authentication/authorization
* [ ] Add OpenAPI/Swagger documentation
* [ ] Add integration tests with Testcontainers
* [ ] Add Kafka integration tests
* [ ] Add PostgreSQL integration tests
* [ ] Add Redis integration tests
* [ ] Add Micrometer metrics
* [ ] Add health checks for PostgreSQL, Redis and Kafka
* [ ] Add distributed tracing
* [ ] Add CI/CD pipeline
* [ ] Externalize credentials using environment variables/secrets
* [ ] Add structured JSON logging
* [ ] Add production Docker Compose/Kubernetes configuration

---

# Performance Considerations

The architecture is designed to reduce database pressure for frequently accessed URLs.

### Without Redis

```text
Request
   ↓
PostgreSQL
   ↓
Response
```

### With Redis

```text
Request
   ↓
Redis
   ↓
Response
```

Only cache misses require a database lookup.

Kafka also allows analytics processing to be decoupled from the request-processing path once the primary expansion endpoint is fully migrated to the Kafka flow.

---

# Security Considerations

For production:

* Use HTTPS
* Validate and normalize URLs
* Protect against malicious/open redirects
* Apply request rate limiting
* Sanitize logging data
* Do not log sensitive headers
* Externalize database credentials
* Externalize Kafka credentials
* Use Kafka authentication and TLS where required
* Restrict Redis network access
* Restrict PostgreSQL network access
* Apply least-privilege database users

---

# Example End-to-End Flow

```text
1. Client
      │
      │ POST /api/urls/shorten
      ▼
2. UrlController
      │
      ▼
3. UrlService
      │
      ├── Generate short code
      ├── Save PostgreSQL
      └── Cache Redis
      │
      ▼
4. Return short URL
```

Then:

```text
5. Client
      │
      │ GET /api/urls/abc12345
      ▼
6. UrlService
      │
      ▼
7. Redis
      │
      ├── HIT → Return URL
      │
      └── MISS
             │
             ▼
         PostgreSQL
             │
             ▼
           Redis
             │
             ▼
        Return URL
```

Analytics-enabled flow:

```text
8. Capture request metadata
          │
          ▼
9. AnalyticsEventProducer
          │
          ▼
10. Kafka: url-click-events
          │
          ▼
11. AnalyticsEventConsumer
          │
          ▼
12. AnalyticsService
          │
          ▼
13. PostgreSQL
```

---

# Summary

This project demonstrates a layered Spring Boot URL Shortener with:

```text
Spring Boot
     │
     ├── REST APIs
     │
     ├── Service Layer
     │
     ├── PostgreSQL
     │
     ├── Redis Cache
     │
     ├── Apache Kafka
     │
     ├── Analytics
     │
     ├── Request Context
     │
     └── Unit Tests
```

The architecture separates synchronous URL operations from analytics processing and provides a foundation for horizontal scaling and further production hardening.

The `integrate-kafka` branch is available here:

https://github.com/syamdoddi405/url-shortener/tree/integrate-kafka
