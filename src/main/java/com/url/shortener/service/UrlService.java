package com.url.shortener.service;

import java.util.List;

import com.url.shortener.entity.UrlEntity;

/**
 * Service interface for URL operations.
 * Follows the Interface Segregation Principle.
 * Handles URL shortening and expansion logic.
 */
public interface UrlService {

    /**
     * Shortens a given URL.
     *
     * @param originalUrl the original URL to shorten
     * @return UrlEntity containing the shortened URL details
     * @throws IllegalArgumentException if URL is invalid
     */
    UrlEntity shortenUrl(String originalUrl);

    /**
     * Expands a shortened URL back to the original.
     *
     * @param shortCode the short code
     * @return the original URL
     * @throws com.url.shortener.exceptions.UrlNotFoundException if short code not found
     */
    String expandUrl(String shortCode);
    
    
    /**
     * fetch all URL details
     *
     * @return List<UrlEntity> containing the shortened URL details
     */
	List<UrlEntity> getUrls();
}
