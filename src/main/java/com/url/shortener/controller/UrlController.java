package com.url.shortener.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.shortener.entity.ShortenUrlRequest;
import com.url.shortener.entity.UrlEntity;
import com.url.shortener.service.UrlService;
import com.url.shortener.service.context.RequestContext;

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
    public ResponseEntity<UrlEntity> shortenUrl(@RequestBody ShortenUrlRequest request) {
        log.info("Received request to shorten URL from IP: {}", requestContext.getClientIp());
        
        try {
        	UrlEntity result = urlService.shortenUrl(request.getOriginalUrl());
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
     * Retrieves all urls.
     * GET /
     *
     * @param shortCode the short code
     * @return ResponseEntity with analytics data
     */
    @GetMapping("/")
    public ResponseEntity<List<UrlEntity>> getUrls() {
        log.debug("Fetch all urls details from IP: {}", 
                requestContext.getClientIp());
        
        try {
        	List<UrlEntity> urls = urlService.getUrls();
            log.info("Successfully retrieved all urls {}", urls.size());
            return ResponseEntity.ok(urls);
        } catch (Exception e) {
            log.error("Error retrieving urls", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
