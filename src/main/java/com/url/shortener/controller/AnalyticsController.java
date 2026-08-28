package com.url.shortener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.shortener.entity.AnalyticsEntity;
import com.url.shortener.entity.ExpandUrlResponse;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.kafka.AnalyticsEventProducer;
import com.url.shortener.service.AnalyticsService;
import com.url.shortener.service.UrlService;
import com.url.shortener.service.context.RequestContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for analytics operations.
 * Follows the Single Responsibility Principle - handles HTTP concerns only.
 * Delegates business logic to service layer.
 * Uses dependency injection for loose coupling.
 * Implements proper error handling and logging.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UrlService urlService;
    private final RequestContext requestContext;
    private final AnalyticsEventProducer analyticsEventProducer;


    /**
     * Retrieves analytics statistics for a given short code.
     * GET /api/analytics/{shortCode}
     *
     * @param shortCode the short code to retrieve analytics for
     * @return ResponseEntity with analytics data
     * @throws UrlNotFoundException if short code not found
     */
    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<AnalyticsEntity> getStats(@PathVariable String shortCode) {
        log.debug("Received request for analytics of short code: {} from IP: {}", 
                shortCode, requestContext.getClientIp());
        
        try {
        	AnalyticsEntity stats = analyticsService.getStats(shortCode);
            log.info("Successfully retrieved analytics for short code: {}", shortCode);
            return ResponseEntity.ok(stats);
        } catch (UrlNotFoundException e) {
            log.warn("Short code not found: {}", shortCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error retrieving analytics for short code: {}", shortCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Expands a shortened URL and captures analytics data.
     * GET /api/analytics/expand/{shortCode}
     * 
     * This endpoint serves as an alternative expansion endpoint that automatically
     * captures analytics without requiring separate API calls.
     *
     * @param shortCode the short code to expand
     * @return ResponseEntity with the original URL
     * @throws UrlNotFoundException if short code not found
     */
    @GetMapping("/expand/{shortCode}/url")
    public ResponseEntity<ExpandUrlResponse> expandUrl(@PathVariable String shortCode) {
        log.debug("Received request to expand short code: {} from IP: {}", 
                shortCode, requestContext.getClientIp());
        
        try {
            // Expand the URL
            String originalUrl = urlService.expandUrl(shortCode);
            log.info("Successfully expanded short code: {}", shortCode);

            // Capture analytics data
            String referrer = requestContext.getReferer();
            String userAgent = requestContext.getUserAgent();
            String clientIp = requestContext.getClientIp();
            analyticsEventProducer.publish(
                    shortCode,
                    referrer,
                    userAgent,
                    clientIp
            );    
            log.debug("Analytics captured for short code: {}", shortCode);

            return ResponseEntity.ok(new ExpandUrlResponse(originalUrl));
        } catch (UrlNotFoundException e) {
            log.warn("Short code not found for expansion: {}", shortCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error expanding short code: {}", shortCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
