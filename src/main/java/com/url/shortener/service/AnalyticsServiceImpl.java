package com.url.shortener.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.url.shortener.dto.AnalyticsDTO;
import com.url.shortener.entity.AnalyticsEntity;
import com.url.shortener.entity.UrlEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.mapper.AnalyticsMapper;
import com.url.shortener.repository.AnalyticsRepository;
import com.url.shortener.repository.UrlRepository;
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

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDTO getStats(String shortCode) {
        log.debug("Fetching analytics stats for short code: {}", shortCode);
        
        // Verify URL exists
        UrlEntity urlEntity = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("URL not found for short code: {}", shortCode);
                    return new UrlNotFoundException("URL not found for short code: " + shortCode);
                });

        // Fetch or create analytics record
        AnalyticsEntity analyticsEntity = analyticsRepository.findByShortCode(shortCode)
                .orElse(AnalyticsEntity.builder()
                        .shortCode(shortCode)
                        .originalUrl(urlEntity.getOriginalUrl())
                        .totalClicks(0)
                        .createdAt(LocalDateTime.now())
                        .build());

        log.debug("Retrieved analytics for short code: {} with {} clicks", shortCode, analyticsEntity.getTotalClicks());
        return analyticsMapper.toDTO(analyticsEntity);
    }

    @Override
    @Transactional
    public void saveAnalytics(String shortCode, String referrer, String userAgent) {
        log.debug("Saving analytics for short code: {}", shortCode);
        
        try {
            AnalyticsEntity analytics = analyticsRepository.findByShortCode(shortCode)
                    .orElseGet(() -> {
                        log.debug("Creating new analytics record for short code: {}", shortCode);
                        return AnalyticsEntity.builder()
                                .shortCode(shortCode)
                                .totalClicks(0)
                                .createdAt(LocalDateTime.now())
                                .build();
                    });

            analytics.setTotalClicks(analytics.getTotalClicks() + 1);
            analytics.setLastAccessed(LocalDateTime.now());
            analytics.setLastReferrer(referrer);
            analytics.setLastUserAgent(userAgent);
            
            analyticsRepository.save(analytics);
            log.debug("Analytics saved successfully for short code: {} (total clicks: {})", 
                    shortCode, analytics.getTotalClicks());
        } catch (Exception e) {
            log.error("Error saving analytics for short code: {}", shortCode, e);
            throw new RuntimeException("Failed to save analytics data", e);
        }
    }
}
