package com.url.shortener.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.url.shortener.dto.AnalyticsDTO;
import com.url.shortener.dto.UrlDTO;
import com.url.shortener.entity.AnalyticsEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.mapper.AnalyticsMapper;
import com.url.shortener.repository.AnalyticsRepository;
import com.url.shortener.repository.UrlRepository;
import com.url.shortener.service.cache.CacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for analytics operations.
 * Handles analytics data persistence and retrieval.
 * Uses proper transaction management and logging.
 * Follows the Single Responsibility Principle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final UrlRepository urlRepository;
    private final AnalyticsMapper analyticsMapper;
    private final CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsEntity getStats(String shortCode) {
        log.debug("Fetching analytics stats for short code: {}", shortCode);
        
        // Verify URL exists
        UrlDTO urlEntity = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("URL not found for short code: {}", shortCode);
                    return new UrlNotFoundException("URL not found for short code: " + shortCode);
                });

        // Fetch or create analytics record
        AnalyticsDTO analyticsEntity = analyticsRepository.findByShortCode(shortCode)
                .orElse(AnalyticsDTO.builder()
                        .shortCode(shortCode)
                        .originalUrl(urlEntity.getOriginalUrl())
                        .totalClicks(0)
                        .createdAt(LocalDateTime.now())
                        .build());

        log.debug("Retrieved analytics for short code: {} with {} clicks", shortCode, analyticsEntity.getTotalClicks());
        return analyticsMapper.toEntity(analyticsEntity);
    }

    @Override
    @Transactional
    public void saveAnalytics(String shortCode, String referrer, String userAgent) {
        log.debug("Saving analytics for short code: {}", shortCode);
        
        try {
        	AnalyticsDTO analytics = analyticsRepository.findByShortCode(shortCode)
                    .orElseGet(() -> {
                        log.debug("Creating new analytics record for short code: {}", shortCode);
                        return AnalyticsDTO.builder()
                                .shortCode(shortCode)
                                .totalClicks(0)
                                .createdAt(LocalDateTime.now())
                                .build();
                    });
        	if(analytics.getId() !=null) {
        		analytics.setId(analytics.getId());
        	}
            analytics.setTotalClicks(analytics.getTotalClicks() + 1);
            analytics.setLastAccessed(LocalDateTime.now());
            analytics.setLastReferrer(referrer);
            analytics.setLastUserAgent(userAgent);
            var cachedUrl = cacheService.get(shortCode);
            if (cachedUrl.isPresent()) {
                log.debug("Retrieved URL from cache for short code: {}", shortCode);
                analytics.setOriginalUrl(cachedUrl.get());
            }

            
            analyticsRepository.save(analytics);
            log.debug("Analytics saved successfully for short code: {} (total clicks: {})", 
                    shortCode, analytics.getTotalClicks());
        } catch (Exception e) {
            log.error("Error saving analytics for short code: {}", shortCode, e);
            throw new RuntimeException("Failed to save analytics data", e);
        }
    }
}
