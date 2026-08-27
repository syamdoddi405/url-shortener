package com.url.shortener.service;

import com.url.shortener.dto.AnalyticsDTO;

/**
 * Service interface for analytics operations.
 * Follows the Interface Segregation Principle.
 * Separated from URL service to handle analytics concerns independently.
 */
public interface AnalyticsService {

    /**
     * Retrieves analytics statistics for a given short code.
     *
     * @param shortCode the short code
     * @return AnalyticsDTO containing the statistics
     * @throws com.url.shortener.exceptions.UrlNotFoundException if short code not found
     */
    AnalyticsDTO getStats(String shortCode);

    /**
     * Saves or updates analytics data for a URL access.
     *
     * @param shortCode the short code
     * @param referrer the referrer header value
     * @param userAgent the user agent header value
     */
    void saveAnalytics(String shortCode, String referrer, String userAgent);
}
