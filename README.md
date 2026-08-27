# URL Shortener Service - SOLID Principles Refactoring

## Overview

This document provides a comprehensive guide to the refactored URL Shortener application, which implements SOLID principles for improved code quality, maintainability, and scalability.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [SOLID Principles Implementation](#solid-principles-implementation)
3. [Service Components](#service-components)
4. [API Endpoints](#api-endpoints)
5. [Error Handling](#error-handling)
6. [Logging Strategy](#logging-strategy)
7. [Best Practices](#best-practices)

---

## Architecture Overview

The application follows a layered architecture pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    REST Controllers                          │
│            (HTTP Request/Response Handling)                  │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer                             │
│           (Business Logic & Orchestration)                   │
├─────────────────────────────────────────────────────────────┤
│               Repository/Data Access Layer                   │
│              (Database Operations & Caching)                 │
├─────────────────────────────────────────────────────────────┤
│                    Utility & Support                         │
│         (Exceptions, Context, Generators, DTOs)              │
└─────────────────────────────────────────────────────────────┘
```

---

## SOLID Principles Implementation

### 1. Single Responsibility Principle (SRP)

**Definition**: Each class should have only one reason to change.

**Implementation**:

- **UrlController**: Handles HTTP requests/responses only
- **AnalyticsController**: Manages analytics endpoints exclusively
- **UrlService**: Orchestrates URL shortening and expansion logic
- **AnalyticsService**: Focuses solely on analytics data
- **ShortCodeGenerator**: Responsible only for code generation
- **Custom Exceptions**: Separated by concern (UrlNotFoundException, InvalidUrlException)

**Benefits**:
- Easier to test and maintain
- Changes to one concern don't affect others
- Code is more readable and focused

**Example**:
```java
// Before: UrlController handled validation, shortening, analytics, and HTTP concerns
// After: Each component has a single responsibility

@RestController
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;           // Business logic
    private final AnalyticsService analyticsService; // Analytics only
    private final RequestContext requestContext;     // Context management
}
```

---

### 2. Open/Closed Principle (OCP)

**Definition**: Classes should be open for extension but closed for modification.

**Implementation**:

- **ShortCodeGenerator**: Provides multiple generation strategies (Hex and Base62)
- **Strategy Pattern**: Alternative implementations can be added without modifying existing code
- **Service Interfaces**: Defined to allow multiple implementations

**Benefits**:
- New features can be added through extension
- Existing code doesn't need modification
- Reduces risk of breaking changes

**Example**:
```java
public class ShortCodeGenerator {
    // Default implementation
    public String generate(String originalUrl) {
        return generateHexCode(originalUrl);
    }
    
    // Alternative implementation
    public String generateBase62(String originalUrl) {
        return encodeBase62(originalUrl.hashCode());
    }
}
```

---

### 3. Liskov Substitution Principle (LSP)

**Definition**: Derived classes must be substitutable for their base classes.

**Implementation**:

- **Exception Hierarchy**: All custom exceptions extend RuntimeException
- **Service Implementations**: Can be swapped without breaking contracts
- **Repository Pattern**: Different persistence implementations can be used interchangeably

**Benefits**:
- Polymorphism works as expected
- Code is more flexible and resilient
- Easy to implement different storage strategies

**Example**:
```java
// All exceptions can be caught as RuntimeException
try {
    urlService.expandUrl(shortCode);
} catch (UrlNotFoundException e) {
    // Specific handling
} catch (RuntimeException e) {
    // General handling
}
```

---

### 4. Interface Segregation Principle (ISP)

**Definition**: Clients should not depend on interfaces they don't use.

**Implementation**:

- **Focused Services**: Services expose only necessary methods
- **Context Interface**: RequestContext provides only needed HTTP context methods
- **DTO Pattern**: Data transfer objects are tailored to specific endpoints

**Benefits**:
- Reduced coupling between components
- Cleaner, more maintainable interfaces
- Easier to mock and test

**Example**:
```java
// RequestContext exposes only relevant methods
public interface RequestContext {
    String getClientIp();
    String getReferer();
    String getUserAgent();
    // Not exposing everything from HttpServletRequest
}
```

---

### 5. Dependency Inversion Principle (DIP)

**Definition**: Depend on abstractions, not concretions.

**Implementation**:

- **Constructor Injection**: Dependencies injected via constructor
- **@RequiredArgsConstructor**: Lombok annotation for dependency injection
- **Service Abstraction**: Controllers depend on service interfaces, not implementations
- **Spring IoC Container**: Manages dependency resolution

**Benefits**:
- Loose coupling between components
- Easy to test with mock dependencies
- Flexible and extensible architecture

**Example**:
```java
@RestController
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;           // Injected dependency
    private final AnalyticsService analyticsService; // Injected dependency
    private final RequestContext requestContext;     // Injected dependency
}
```

---

## Service Components

### 1. UrlService

**Responsibility**: Core URL shortening and expansion operations

**Key Methods**:
- `shortenUrl(String originalUrl)`: Creates a short code for the given URL
- `expandUrl(String shortCode)`: Retrieves the original URL from short code

**Features**:
- Input validation
- Duplicate handling (caching/deduplication)
- Error handling with custom exceptions

**Implementation Details**:
```java
UrlService urlService = new UrlService(
    urlRepository,
    shortCodeGenerator,
    cacheService
);

// Shortens URL with automatic deduplication
UrlDTO result = urlService.shortenUrl("https://github.com/example");

// Expands short code to original URL
String originalUrl = urlService.expandUrl(result.getShortCode());
```

---

### 2. AnalyticsService

**Responsibility**: Tracking and aggregating analytics data

**Key Methods**:
- `saveAnalytics(String shortCode, String referrer, String userAgent)`: Records a click event
- `getStats(String shortCode)`: Retrieves aggregated analytics for a short code

**Features**:
- Referrer tracking
- User agent capture
- Click count aggregation
- Geographic data (if extended)

**Implementation Details**:
```java
AnalyticsService analyticsService = new AnalyticsService(analyticsRepository);

// Record a click event
analyticsService.saveAnalytics("abc123", "google.com", "Mozilla/5.0...");

// Get statistics
AnalyticsDTO stats = analyticsService.getStats("abc123");
// Returns: {
//   "shortCode": "abc123",
//   "totalClicks": 42,
//   "referrers": {"google.com": 30, "twitter.com": 12},
//   "userAgents": [...]
// }
```

---

### 3. ShortCodeGenerator

**Responsibility**: Generating unique short codes from URLs

**Key Methods**:
- `generate(String originalUrl)`: Generates hex-based short code
- `generateBase62(String originalUrl)`: Generates Base62-encoded short code

**Algorithms**:

**Hex Generation**:
```
1. Hash the URL using hashCode()
2. Convert to hexadecimal
3. Pad or truncate to fixed length (8 characters)
Example: "https://github.com" → "a1b2c3d4"
```

**Base62 Generation**:
```
1. Hash the URL using hashCode()
2. Encode using Base62 (0-9, a-z, A-Z)
3. More compact than hex (62 possible characters vs 16)
Example: "https://github.com" → "4fK9xQ2"
```

**Features**:
- Multiple generation strategies
- Configurable output length
- Collision handling (ready for database uniqueness checks)

**Production Considerations**:
```java
// Current implementation uses hash-based generation
// For production, consider:

1. Database Sequences:
   // Use auto-incrementing IDs with Base62 encoding
   // Guarantees uniqueness
   
2. Collision Detection:
   // Check if short code exists
   // If collision detected, retry with timestamp variation
   
3. Custom Alphabet:
   // Exclude confusing characters (0/O, l/1, etc.)
   // Use custom Base alphabet for better readability
```

---

### 4. CacheService (Recommended Implementation)

**Responsibility**: Caching short code mappings for performance

**Key Methods**:
- `get(String key)`: Retrieve cached value
- `put(String key, Object value)`: Store value in cache
- `evict(String key)`: Remove from cache
- `clear()`: Clear entire cache

**Implementation Strategies**:

```java
// Option 1: In-memory Cache (Spring Cache)
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("urls", "analytics");
    }
}

// Option 2: Redis Cache (Distributed)
@Configuration
public class RedisConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }
}

// Option 3: Custom Cache Interface
public interface CacheService {
    <T> T get(String key);
    <T> void put(String key, T value);
    void evict(String key);
    void clear();
}
```

---

### 5. RequestContext

**Responsibility**: Encapsulating HTTP request information

**Key Methods**:
- `getClientIp()`: Client's IP address
- `getReferer()`: HTTP Referer header
- `getUserAgent()`: User agent string
- `getSessionId()`: Session identifier

**Implementation**:
```java
@Component
@RequiredArgsConstructor
public class RequestContext {
    private final HttpServletRequest request;
    
    public String getClientIp() {
        // Extract client IP (handles proxies)
    }
    
    public String getReferer() {
        return request.getHeader("Referer");
    }
    
    public String getUserAgent() {
        return request.getHeader("User-Agent");
    }
}
```

---

## API Endpoints

### UrlController Endpoints

#### 1. Shorten URL

**Endpoint**: `POST /api/urls/shorten`

**Request**:
```json
{
  "originalUrl": "https://github.com/example/repository/blob/main/README.md"
}
```

**Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalUrl": "https://github.com/example/repository/blob/main/README.md",
  "shortCode": "a1b2c3d4",
  "shortUrl": "https://short.url/a1b2c3d4",
  "createdAt": "2024-01-15T10:30:00Z",
  "expiresAt": null
}
```

**Error Responses**:
- `400 Bad Request`: Invalid or empty URL
- `500 Internal Server Error`: Server processing error

---

#### 2. Expand URL

**Endpoint**: `GET /api/urls/{shortCode}`

**Response** (200 OK):
```json
{
  "originalUrl": "https://github.com/example/repository/blob/main/README.md"
}
```

**Error Responses**:
- `404 Not Found`: Short code doesn't exist
- `500 Internal Server Error`: Server processing error

---

#### 3. Get URL Statistics

**Endpoint**: `GET /api/urls/{shortCode}/stats`

**Response** (200 OK):
```json
{
  "shortCode": "a1b2c3d4",
  "originalUrl": "https://github.com/example/repository",
  "totalClicks": 156,
  "uniqueClicks": 89,
  "referrers": {
    "google.com": 45,
    "twitter.com": 32,
    "direct": 79
  },
  "createdAt": "2024-01-15T10:30:00Z",
  "lastAccessedAt": "2024-01-27T15:45:30Z"
}
```

**Error Responses**:
- `404 Not Found`: Short code doesn't exist
- `500 Internal Server Error`: Server processing error

---

### AnalyticsController Endpoints

#### 1. Get Analytics

**Endpoint**: `GET /api/analytics/{shortCode}`

**Response** (200 OK):
```json
{
  "shortCode": "a1b2c3d4",
  "totalClicks": 156,
  "uniqueClicks": 89,
  "topReferrers": [
    {
      "referrer": "google.com",
      "count": 45
    },
    {
      "referrer": "twitter.com",
      "count": 32
    }
  ],
  "timeSeriesData": [...]
}
```

**Error Responses**:
- `404 Not Found`: Short code doesn't exist
- `500 Internal Server Error`: Server processing error

---

#### 2. Expand with Analytics Capture

**Endpoint**: `GET /api/analytics/expand/{shortCode}`

**Response** (200 OK):
```json
{
  "originalUrl": "https://github.com/example/repository"
}
```

**Features**:
- Expands URL
- Automatically captures referrer and user agent
- Records click analytics

**Error Responses**:
- `404 Not Found`: Short code doesn't exist
- `500 Internal Server Error`: Server processing error

---

## Error Handling

### Custom Exception Hierarchy

```
RuntimeException
├── UrlNotFoundException
│   └── Thrown when a short code doesn't exist
│   └── HTTP Status: 404
│
└── InvalidUrlException
    └── Thrown when URL validation fails
    └── HTTP Status: 400
```

### Exception Examples

```java
// UrlNotFoundException
try {
    urlService.expandUrl("invalid123");
} catch (UrlNotFoundException e) {
    // Handle: Short code doesn't exist
    // Return: 404 Not Found
}

// InvalidUrlException
try {
    urlService.shortenUrl("");
} catch (InvalidUrlException e) {
    // Handle: URL is empty or invalid
    // Return: 400 Bad Request
}
```

### Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotFound(UrlNotFoundException ex) {
        log.warn("URL not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("URL_NOT_FOUND", ex.getMessage()));
    }
    
    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrl(InvalidUrlException ex) {
        log.warn("Invalid URL: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("INVALID_URL", ex.getMessage()));
    }
}
```

---

## Logging Strategy

### Log Levels

- **DEBUG**: Detailed operational information (request received, processing steps)
- **INFO**: Significant events (successful operations, statistics retrieved)
- **WARN**: Warning conditions (URL not found, validation failures)
- **ERROR**: Error events (exceptions, processing failures)

### Logging Implementation

```java
@Slf4j
public class UrlService {
    
    public UrlDTO shortenUrl(String originalUrl) {
        log.debug("Starting URL shortening for: {}", originalUrl);
        
        if (!isValidUrl(originalUrl)) {
            log.warn("Invalid URL provided: {}", originalUrl);
            throw new InvalidUrlException("URL is invalid");
        }
        
        String shortCode = shortCodeGenerator.generate(originalUrl);
        log.info("Successfully created short code: {} for URL: {}", shortCode, originalUrl);
        
        return new UrlDTO(shortCode, originalUrl);
    }
}
```

### Log Output Format

```
[INFO] [2024-01-27 14:30:15] Successfully shortened URL. Short code: a1b2c3d4
[DEBUG] [2024-01-27 14:30:16] Received request to expand short code: a1b2c3d4 from IP: 192.168.1.1
[WARN] [2024-01-27 14:30:17] Short code not found: invalid123
[ERROR] [2024-01-27 14:30:18] Error retrieving analytics - Database connection failed
```

---

## Best Practices

### 1. URL Validation

```java
private boolean isValidUrl(String url) {
    try {
        new URL(url);
        return true;
    } catch (MalformedURLException e) {
        return false;
    }
}
```

### 2. Short Code Generation

```java
// Use deterministic but unpredictable hash function
public String generate(String originalUrl) {
    if (originalUrl == null || originalUrl.trim().isEmpty()) {
        throw new InvalidUrlException("URL cannot be null or empty");
    }
    return generateHexCode(originalUrl);
}
```

### 3. Caching Strategy

```java
// Cache hit reduces database queries
public String expandUrl(String shortCode) {
    // Try cache first
    String cached = cacheService.get(shortCode);
    if (cached != null) {
        log.debug("Cache hit for: {}", shortCode);
        return cached;
    }
    
    // Fall back to database
    String originalUrl = urlRepository.findByShortCode(shortCode)
        .orElseThrow(() -> new UrlNotFoundException("Short code not found"));
    
    // Update cache
    cacheService.put(shortCode, originalUrl);
    return originalUrl;
}
```

### 4. Analytics Async Processing

```java
// Process analytics asynchronously to avoid blocking
@Async
public void saveAnalytics(String shortCode, String referrer, String userAgent) {
    try {
        Analytics analytics = new Analytics(
            shortCode, referrer, userAgent, LocalDateTime.now()
        );
        analyticsRepository.save(analytics);
        log.debug("Analytics saved for: {}", shortCode);
    } catch (Exception e) {
        log.error("Failed to save analytics", e);
        // Don't throw - analytics failure shouldn't break the main flow
    }
}
```

### 5. Security Considerations

```java
// Input sanitization
public String sanitizeInput(String input) {
    if (input == null) return null;
    return input.trim()
        .replaceAll("[^a-zA-Z0-9-._~:/?#\\[\\]@!$&'()*+,;=%]", "");
}

// Rate limiting
@RateLimiter(limit = 100, window = 60) // 100 requests per minute
@PostMapping("/shorten")
public ResponseEntity<UrlDTO> shortenUrl(@RequestBody ShortenUrlRequest request) {
    // Implementation
}

// HTTPS enforcement
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.requiresChannel()
            .anyRequest()
            .requiresSecure(); // Enforce HTTPS
        return http.build();
    }
}
```

---

## Development Guide

### Building the Project

```bash
# Build with Maven
mvn clean package

# Build with Gradle
gradle build
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UrlServiceTest

# Run with coverage
mvn test jacoco:report
```

### Running the Application

```bash
# Run with Maven
mvn spring-boot:run

# Run the JAR
java -jar target/url-shortener.jar

# Run with specific profile
java -jar target/url-shortener.jar --spring.profiles.active=production
```

### Configuration

```properties
# application.properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener
spring.datasource.username=root
spring.datasource.password=password
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
logging.level.root=INFO
logging.level.com.url.shortener=DEBUG
```

---

## Future Enhancements

1. **URL Expiration**: Implement TTL for short codes
2. **Custom Short Codes**: Allow users to specify custom aliases
3. **QR Code Generation**: Generate QR codes for shortened URLs
4. **Detailed Analytics**: Geographic data, device types, browser info
5. **URL Previews**: Show preview of target page before redirect
6. **API Authentication**: OAuth2 for secure API access
7. **Rate Limiting**: Prevent abuse of shortening endpoint
8. **Bulk Operations**: Batch URL shortening and expansion
9. **URL Management Dashboard**: Web UI for managing shortened URLs
10. **Webhook Support**: Notify clients when URLs are accessed

---

## Contributing

When adding new features, ensure:

1. **Follow SOLID Principles**: Single responsibility, proper abstraction
2. **Add Logging**: Include appropriate log statements
3. **Write Tests**: Maintain high test coverage
4. **Document Changes**: Update this README
5. **Error Handling**: Use custom exceptions appropriately
6. **Code Review**: Get approval before merging

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.
