package com.url.shortener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.url.shortener.dto.UrlDTO;
import com.url.shortener.dto.AnalyticsDTO;
import com.url.shortener.service.UrlService;
import com.url.shortener.service.AnalyticsService;
import com.url.shortener.service.context.RequestContext;
import com.url.shortener.exceptions.UrlNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for URL shortening operations.
 * Follows the Single Responsibility Principle - handles HTTP concerns only.
 * Delegates business logic to service layer.
 * Uses dependency injection for loose coupling.
 * Implements proper error handling and logging.
 */
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final UrlService urlService;
    private final AnalyticsService analyticsService;
    private final RequestContext requestContext;

    /**
     * Shortens a URL.
     * POST /api/urls/shorten
     *
     * @param request object containing the original URL
     * @return ResponseEntity with shortened URL details
     * @throws IllegalArgumentException if URL is invalid
     */
    @PostMapping("/shorten")
    public ResponseEntity<UrlDTO> shortenUrl(@RequestBody ShortenUrlRequest request) {
        log.info("Received request to shorten URL from IP: {}", requestContext.getClientIp());
        
        try {
            UrlDTO result = urlService.shortenUrl(request.getOriginalUrl());
            log.info("Successfully shortened URL. Short code: {}", result.getShortCode());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid URL provided: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error shortening URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Expands a shortened URL to the original.
     * GET /api/urls/{shortCode}
     *
     * @param shortCode the short code
     * @return ResponseEntity with the original URL
     * @throws UrlNotFoundException if short code not found
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<ExpandUrlResponse> expandUrl(@PathVariable String shortCode) {
        log.debug("Received request to expand short code: {} from IP: {}", 
                shortCode, requestContext.getClientIp());
        
        try {
            String originalUrl = urlService.expandUrl(shortCode);
            
            // Save analytics asynchronously in a real application
            String referrer = requestContext.getReferer();
            String userAgent = requestContext.getUserAgent();
            analyticsService.saveAnalytics(shortCode, referrer, userAgent);
            
            log.info("Successfully expanded short code: {}", shortCode);
            return ResponseEntity.ok(new ExpandUrlResponse(originalUrl));
        } catch (UrlNotFoundException e) {
            log.warn("Short code not found: {}", shortCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error expanding URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves analytics for a shortened URL.
     * GET /api/urls/{shortCode}/stats
     *
     * @param shortCode the short code
     * @return ResponseEntity with analytics data
     * @throws UrlNotFoundException if short code not found
     */
    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<AnalyticsDTO> getStats(@PathVariable String shortCode) {
        log.debug("Received request for analytics of short code: {} from IP: {}", 
                shortCode, requestContext.getClientIp());
        
        try {
            AnalyticsDTO stats = analyticsService.getStats(shortCode);
            log.info("Successfully retrieved analytics for short code: {}", shortCode);
            return ResponseEntity.ok(stats);
        } catch (UrlNotFoundException e) {
            log.warn("Short code not found for analytics: {}", shortCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error retrieving analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Request DTO for shortening a URL.
     */
    public static class ShortenUrlRequest {
        private String originalUrl;

        public ShortenUrlRequest() {}
        public ShortenUrlRequest(String originalUrl) {
            this.originalUrl = originalUrl;
        }

        public String getOriginalUrl() {
            return originalUrl;
        }

        public void setOriginalUrl(String originalUrl) {
            this.originalUrl = originalUrl;
        }
    }

    /**
     * Response DTO for expanding a URL.
     */
    public static class ExpandUrlResponse {
        private String originalUrl;

        public ExpandUrlResponse(String originalUrl) {
            this.originalUrl = originalUrl;
        }

        public String getOriginalUrl() {
            return originalUrl;
        }

        public void setOriginalUrl(String originalUrl) {
            this.originalUrl = originalUrl;
        }
    }
}
