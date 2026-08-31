package com.url.shortener.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.shortener.entity.ExpandUrlResponse;
import com.url.shortener.entity.ShortenUrlRequest;
import com.url.shortener.entity.UrlEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.kafka.AnalyticsEventProducer;
import com.url.shortener.service.UrlService;
import com.url.shortener.service.context.RequestContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for URL shortening operations.
 * Follows the Single Responsibility Principle - handles HTTP concerns only.
 * Delegates business logic to service layer.
 * Uses dependency injection for loose coupling.
 * Implements proper error handling and logging.
 */
@Tag(
	    name = "URL Management",
	    description = "APIs for creating and retrieving shortened URLs"
	)
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final UrlService urlService;
    private final RequestContext requestContext;
    private final AnalyticsEventProducer analyticsEventProducer;


    /**
     * Shortens a URL.
     * POST /api/urls/shorten
     *
     * @param request object containing the original URL
     * @return ResponseEntity with shortened URL details
     * @throws IllegalArgumentException if URL is invalid
     */
    @Operation(
    	    summary = "Shorten a URL",
    	    description = "Creates a short code for the supplied original URL."
    	)
    	@ApiResponses({
    	    @ApiResponse(
    	        responseCode = "201",
    	        description = "URL successfully shortened",
    	        content = @Content(
    	            schema = @Schema(implementation = UrlEntity.class)
    	        )
    	    ),
    	    @ApiResponse(
    	        responseCode = "400",
    	        description = "Invalid URL"
    	    ),
    	    @ApiResponse(
    	        responseCode = "500",
    	        description = "Internal server error"
    	    )
    	})
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
     * Expands a shortened URL and captures analytics data.
     * GET /api/urls/expand/{shortCode}
     * 
     * This endpoint serves as an alternative expansion endpoint that automatically
     * captures analytics without requiring separate API calls.
     *
     * @param shortCode the short code to expand
     * @return ResponseEntity with the original URL
     * @throws UrlNotFoundException if short code not found
     */
    @Operation(
    	    summary = "Expand shortened URL",
    	    description = """
    	        Expands a short code into the original URL and
    	        publishes an analytics event to Kafka.
    	        """
    	)
    	@ApiResponses({
    	    @ApiResponse(
    	        responseCode = "200",
    	        description = "URL successfully expanded"
    	    ),
    	    @ApiResponse(
    	        responseCode = "404",
    	        description = "Short code not found"
    	    ),
    	    @ApiResponse(
    	        responseCode = "500",
    	        description = "Internal server error"
    	    )
    	})
    	@GetMapping("/expand/{shortCode}/url")
    	public ResponseEntity<ExpandUrlResponse> expandUrl(
    	        @Parameter(
    	            description = "Short code of the URL",
    	            example = "abc12345",
    	            required = true
    	        )
    	        @PathVariable String shortCode) {
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



    /**
     * Retrieves all urls.
     * GET /
     *
     * @param shortCode the short code
     * @return ResponseEntity with analytics data
     */
    @Operation(
    	    summary = "Retrieve all URLs",
    	    description = "Returns all shortened URLs stored in the system."
    	)
    	@ApiResponses({
    	    @ApiResponse(
    	        responseCode = "200",
    	        description = "URLs successfully retrieved"
    	    ),
    	    @ApiResponse(
    	        responseCode = "500",
    	        description = "Internal server error"
    	    )
    	})
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
