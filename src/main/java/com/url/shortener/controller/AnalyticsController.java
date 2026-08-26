package com.url.shortener.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.shortener.dto.AnalyticsDTO;
import com.url.shortener.service.AnalyticsService;
import com.url.shortener.service.UrlService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    
    private final UrlService urlService;
    
    
    private final HttpServletRequest request;


    public AnalyticsController(AnalyticsService analyticsService, UrlService urlService, HttpServletRequest request) {
        this.analyticsService = analyticsService;
		this.urlService = urlService;
		this.request = request;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<AnalyticsDTO> getStats(@PathVariable String shortCode) {
        return ResponseEntity.ok(analyticsService.getStats(shortCode));
    }
    
    @GetMapping("/expand/{shortCode}")
    public ResponseEntity<String> expand(@PathVariable String shortCode) {
        String originalUrl = urlService.expandUrl(shortCode);

        // Capture analytics
        String referrer = request.getHeader("Referer");
        String userAgent = request.getHeader("User-Agent");
        analyticsService.saveAnalytics(shortCode, referrer, userAgent);

        return ResponseEntity.ok(originalUrl);
    }

}

