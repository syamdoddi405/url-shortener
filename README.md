# URL Shortener

A URL Shortener REST API built with **Java 17, Spring Boot, PostgreSQL, Redis, and Apache Kafka**.

The service provides URL shortening, URL retrieval, URL expansion, Redis-based caching, click analytics, HTTP request-context capture, and asynchronous analytics event publishing through Kafka.

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
* [Validation and Error Handling](#validation-and-error-handling)
* [Configuration](#configuration)
* [Prerequisites](#prerequisites)
* [Running the Application](#running-the-application)
* [API Documentation](#api-documentation)
* [Testing](#testing)
* [Design Principles](#design-principles)
* [Known Limitations](#known-limitations)

---

## Overview

The URL Shortener converts a long URL into a short code that can subsequently be used to retrieve the original URL.

Example:

```text
Original URL:
https://www.example.com/products/software-engineering/url-shortener

Short Code:
abc12345
```

The application uses:

* **PostgreSQL** for persistent data
* **Redis** for URL caching
* **Apache Kafka** for asynchronous analytics events
* **Spring Data JPA** for database access
* **Spring Boot** for REST APIs and application configuration
* **MapStruct** for DTO/entity mapping
* **Jakarta Validation** for request validation
* **Springdoc OpenAPI** for API documentation

The application follows a layered structure separating controllers, services, repositories, caching, request context, Kafka messaging, mapping, and utility components.

---

# Features

## URL Management

* Shorten URLs
* Generate short codes
* Persist URL mappings
* Retrieve all stored URLs
* Expand a short code into the original URL
* Handle URLs that cannot be found

## Redis Caching

The application uses Redis for URL lookup caching.

The cache implementation provides:

* Cache lookup
* Cache insertion
* Cache invalidation
* Cache existence checks
* Database fallback when a cache lookup does not return a value
* Redis failure handling

The URL expansion flow follows a cache-aside approach:

```text
Request
   |
   v
Redis
   |
   +---- Cache Hit ------> Original URL
   |
   +---- Cache Miss -----> PostgreSQL
                              |
                              v
                           Redis
                              |
                              v
                         Original URL
```

## Analytics

The application captures analytics information when a shortened URL is expanded.

The captured request information includes:

* Short code
* Referrer
* User-Agent
* Client IP
* Event ID
* Event timestamp

Analytics statistics include click and access information.

## Apache Kafka

Kafka is used to publish URL click/expansion analytics asynchronously.

The configured Kafka topic is:

```text
url-click-events
```

The topic is configured with:

```text
Partitions: 3
Replicas:   1
```

The short code is used as the Kafka message key.

## API Documentation

The application includes OpenAPI/Swagger UI through Springdoc.

Swagger UI is configured at:

```text
/swagger-ui.html
```

OpenAPI JSON is configured at:

```text
/v3/api-docs
```

---

# Architecture

```text
                         +----------------------+
                         |        Client        |
                         +----------+-----------+
                                    |
                                    v
                         +----------------------+
                         |   REST Controllers   |
                         |                      |
                         |  UrlController       |
                         |  AnalyticsController |
                         +----------+-----------+
                                    |
                                    v
                         +----------------------+
                         |    Service Layer     |
                         |                      |
                         |  UrlService          |
                         |  AnalyticsService    |
                         +----+------------+----+
                              |            |
                              |            |
                              v            v
                       +----------+   +-----------+
                       |  Redis   |   | PostgreSQL |
                       |  Cache   |   | Database   |
                       +----------+   +-----------+
                                            ^
                                            |
                                            |
                         +------------------+---+
                         |                      |
                         |     Kafka Consumer    |
                         |                      |
                         +----------^-----------+
                                    |
                                    |
                         +----------+-----------+
                         |        Kafka         |
                         |                      |
                         | url-click-events     |
                         +----------^-----------+
                                    |
                                    |
                         +----------+-----------+
                         | Kafka Producer       |
                         | AnalyticsEventProducer|
                         +----------------------+
```

---

# Technology Stack

| Component         | Technology                               |
| ----------------- | ---------------------------------------- |
| Language          | Java 17                                  |
| Framework         | Spring Boot 4.1.1                        |
| Web               | Spring MVC                               |
| Persistence       | Spring Data JPA                          |
| Database          | PostgreSQL                               |
| Cache             | Redis                                    |
| Messaging         | Apache Kafka                             |
| Object Mapping    | MapStruct 1.5.5.Final                    |
| Validation        | Jakarta Validation / Hibernate Validator |
| API Documentation | Springdoc OpenAPI                        |
| Logging           | SLF4J / Logback                          |
| Testing           | JUnit / Spring Boot Test dependencies    |
| Build Tool        | Maven                                    |
| Code Generation   | Lombok                                   |

The versions and dependencies above are taken from the project's Maven configuration.

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
│   │       │
│   │       ├── entity/
│   │       │
│   │       ├── exceptionhandler/
│   │       │
│   │       ├── exceptions/
│   │       │
│   │       ├── kafka/
│   │       │   ├── AnalyticsEventProducer.java
│   │       │   └── AnalyticsEventConsumer.java
│   │       │
│   │       ├── mapper/
│   │       │
│   │       ├── repository/
│   │       │
│   │       ├── service/
│   │       │   ├── UrlService.java
│   │       │   ├── UrlServiceImpl.java
│   │       │   ├── AnalyticsService.java
│   │       │   ├── AnalyticsServiceImpl.java
│   │       │   ├── cache/
│   │       │   │   ├── CacheService.java
│   │       │   │   └── RedisCacheService.java
│   │       │   └── context/
│   │       │       ├── RequestContext.java
│   │       │       └── HttpRequestContext.java
│   │       │
│   │       └── util/
│   │
│   └── resources/
│       └── application.properties
│
└── test/
    └── java/
        └── com/url/shortener/
```

---

# API Endpoints

The configured application port is:

```text
9000
```

Therefore, the base URL is:

```text
http://localhost:9000
```

The endpoint mappings below are based on the current controller implementation.

---

## 1. Shorten URL

### Request

```http
POST /api/urls/shorten
Content-Type: application/json
```

Request body:

```json
{
  "originalUrl": "https://www.example.com/very/long/url"
}
```

### Successful Response

```text
HTTP 201 Created
```

The controller returns the created `UrlEntity`.

### Error Responses

```text
400 Bad Request
500 Internal Server Error
```

---

## 2. Retrieve All URLs

### Request

```http
GET /api/urls/
```

### Successful Response

```text
HTTP 200 OK
```

The endpoint returns the list of stored `UrlEntity` objects.

### Error Response

```text
500 Internal Server Error
```

---

## 3. Retrieve URL Analytics

### Request

```http
GET /api/analytics/{shortCode}/stats
```

Example:

```http
GET /api/analytics/abc12345/stats
```

### Successful Response

```text
HTTP 200 OK
```

The endpoint returns the analytics information for the specified short code.

### Error Responses

```text
404 Not Found
500 Internal Server Error
```

---

## 4. Expand Short URL

### Request

```http
GET /api/analytics/expand/{shortCode}/url
```

Example:

```http
GET /api/analytics/expand/abc12345/url
```

### Successful Response

```text
HTTP 200 OK
```

Example:

```json
{
  "originalUrl": "https://www.example.com/very/long/url"
}
```

During expansion, the application:

1. Retrieves the original URL.
2. Captures request metadata.
3. Publishes an analytics event to Kafka.
4. Returns the original URL.

These behaviors are implemented in `AnalyticsController`.

### Error Responses

```text
404 Not Found
500 Internal Server Error
```

---

# URL Shortening Flow

```text
POST /api/urls/shorten
          |
          v
   UrlController
          |
          v
   UrlService
          |
          +---- Validate URL
          |
          +---- Generate short code
          |
          +---- Persist URL
          |
          +---- Cache URL mapping
          |
          v
     HTTP 201
```

The controller delegates URL shortening to `UrlService` and returns the resulting `UrlEntity`.

---

# URL Expansion Flow

URL expansion uses Redis as the cache layer.

```text
GET /api/analytics/expand/{shortCode}/url
                    |
                    v
              UrlService
                    |
                    v
                 Redis
                /     \
              HIT     MISS
               |        |
               |        v
               |    PostgreSQL
               |        |
               |        v
               |      Redis
               |        |
               +----+---+
                    |
                    v
              Original URL
```

After successful expansion, request information is captured and an analytics event is published to Kafka.

---

# Analytics Flow

```text
Client
   |
   v
AnalyticsController
   |
   +---- Expand URL
   |
   +---- Get Referrer
   |
   +---- Get User-Agent
   |
   +---- Get Client IP
   |
   v
AnalyticsEventProducer
   |
   v
Kafka
   |
   |  url-click-events
   |
   v
AnalyticsEventConsumer
   |
   v
AnalyticsService
   |
   v
PostgreSQL
```

The expansion endpoint obtains the referrer, User-Agent, and client IP from `RequestContext`, then passes them to `AnalyticsEventProducer`.

---

# Kafka Integration

## Kafka Topic

The application uses:

```text
url-click-events
```

The topic is configured by:

```properties
kafka.topic.url-click-events=url-click-events
```

The `KafkaConfig` creates the topic with three partitions and one replica.

## Producer

The application uses `AnalyticsEventProducer` to publish analytics events.

The Kafka message key is the URL short code.

The configured producer serializers are:

```properties
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JacksonJsonSerializer
```

## Consumer

The configured consumer group is:

```text
url-shortener-analytics
```

The consumer uses:

```properties
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer
```

The default analytics event type is:

```text
com.url.shortener.dto.AnalyticsEvent
```

The consumer delegates analytics processing to `AnalyticsService`.

---

# Redis Caching

Redis is configured as the cache for URL lookups.

The application uses:

```text
RedisTemplate<String, String>
```

The configured Redis server is:

```text
localhost:6379
```

Configuration:

```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=60000
```

The Redis implementation supports:

```text
get()
put()
invalidate()
exists()
```

Redis is used as a cache layer while PostgreSQL remains the persistent data store.

The Redis configuration and connection properties are defined in `application.properties`.

---

# Short Code Generation

The current implementation generates an **8-character hexadecimal short code**.

The repository also contains a Base62 implementation using:

```text
0-9
a-z
A-Z
```

The current hash-based implementation is not collision-proof.

Therefore, different URLs can potentially generate the same short code.

---

# Database

PostgreSQL is configured as the application's persistent database.

The configured database connection is:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortener
spring.datasource.username=postgres
spring.datasource.password=postgres
```

JPA/Hibernate configuration:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

The application uses Spring Data JPA repositories for persistence.

---

# Validation and Error Handling

The application includes:

* Jakarta Validation API
* Hibernate Validator
* Spring Boot Validation
* URL validation
* URL-not-found handling
* HTTP 400 responses
* HTTP 404 responses
* HTTP 500 responses
* Application logging

The controllers explicitly handle invalid URL input and URL-not-found scenarios.

---

# Configuration

The main application configuration is located at:

```text
src/main/resources/application.properties
```

The application runs on:

```text
server.port=9000
```

## PostgreSQL

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortener
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## Redis

```properties
spring.redis.host=localhost
spring.redis.port=6379
```

## Kafka

```properties
spring.kafka.bootstrap-servers=localhost:9092
```

## Swagger / OpenAPI

```properties
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```

These values are taken from the current `application.properties`.

---

# Prerequisites

The application requires the following components:

* Java 17
* Maven
* PostgreSQL
* Redis
* Apache Kafka

The Java version and application dependencies are defined in `pom.xml`.

---

# Running the Application

## 1. Clone the repository

```bash
git clone https://github.com/syamdoddi405/url-shortener.git
```

```bash
cd url-shortener
```

## 2. Configure PostgreSQL

Create/configure the PostgreSQL database according to the values in:

```text
src/main/resources/application.properties
```

The configured database is:

```text
urlshortener
```

## 3. Start Redis

Redis is expected on:

```text
localhost:6379
```

## 4. Start Kafka

Kafka is expected on:

```text
localhost:9092
```

## 5. Build the application

```bash
mvn clean install
```

## 6. Run the application

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:9000
```

---

# API Documentation

Swagger UI:

```text
http://localhost:9000/swagger-ui.html
```

OpenAPI specification:

```text
http://localhost:9000/v3/api-docs
```

Swagger/OpenAPI is configured through the Springdoc dependency and the application's Springdoc properties.

---

# Testing

The project includes Spring Boot testing dependencies together with JPA and Web MVC test support.

Testing dependencies are defined in `pom.xml`.

Run the test suite with:

```bash
mvn test
```

---

# Design Principles

The application separates responsibilities across multiple layers.

### Controller Layer

Responsible for:

* HTTP request handling
* HTTP response creation
* Request/response documentation
* Delegating business operations to services

### Service Layer

Responsible for:

* URL business logic
* Analytics business logic
* Coordinating persistence and caching

### Repository Layer

Responsible for:

* Database access through Spring Data JPA

### Cache Layer

Responsible for:

* Redis operations
* URL cache management

### Kafka Layer

Responsible for:

* Publishing analytics events
* Consuming analytics events

### Request Context Layer

Responsible for retrieving HTTP request information such as:

* Client IP
* Referrer
* User-Agent

The controllers use dependency injection and delegate business operations to dedicated service components.

---

# Known Limitations

## Short Code Collision

The current hash-based short-code implementation is not collision-proof.

A collision-handling strategy would be required if the same generated short code occurs for different URLs.

## Kafka Replication

The configured Kafka topic currently uses:

```text
replicas = 1
```

Therefore, the repository's current configuration does not provide Kafka topic replication beyond a single replica.

## Database Schema Management

The application currently uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

for Hibernate schema management.

---

# Repository

GitHub:

https://github.com/syamdoddi405/url-shortener

---

## License

No license information is currently documented in this README.
