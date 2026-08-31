package com.url.shortener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.shortener.entity.AnalyticsEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.service.AnalyticsService;
import com.url.shortener.service.context.RequestContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for analytics operations.
 * Follows the Single Responsibility Principle - handles HTTP concerns only.
 * Delegates business logic to service layer.
 * Uses dependency injection for loose coupling.
 * Implements proper error handling and logging.
 */
@Tag(
	    name = "Analytics",
	    description = "URL analytics and expansion APIs"
	)
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final RequestContext requestContext;


    /**
     * Retrieves analytics statistics for a given short code.
     * GET /api/analytics/{shortCode}
     *
     * @param shortCode the short code to retrieve analytics for
     * @return ResponseEntity with analytics data
     * @throws UrlNotFoundException if short code not found
     */
    @Operation(
    	    summary = "Get URL statistics",
    	    description = "Returns click and access statistics for a shortened URL."
    	)
    	@ApiResponses({
    	    @ApiResponse(
    	        responseCode = "200",
    	        description = "Analytics successfully retrieved"
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
    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<AnalyticsEntity> getStats( @Parameter(
            description = "Short code of the URL",
            example = "abc12345",
            required = true
        )@PathVariable String shortCode) {
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

    
}
