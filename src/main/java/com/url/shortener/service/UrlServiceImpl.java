package com.url.shortener.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.url.shortener.dto.UrlDTO;
import com.url.shortener.entity.UrlEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.mapper.UrlMapper;
import com.url.shortener.repository.UrlRepository;
import com.url.shortener.service.cache.CacheService;
import com.url.shortener.util.ShortCodeGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for URL operations.
 * Handles URL shortening and expansion with caching support.
 * Uses proper transaction management and logging.
 * Follows the Single Responsibility Principle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final CacheService cacheService;
    private final UrlMapper urlMapper;
    private final ShortCodeGenerator shortCodeGenerator;

    @Override
    @Transactional
    public UrlEntity shortenUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            log.warn("Attempted to shorten empty URL");
            throw new IllegalArgumentException("URL cannot be blank");
        }

        log.debug("Shortening URL: {}", originalUrl);
        
        String shortCode = shortCodeGenerator.generate(originalUrl);
        log.debug("Generated short code: {} for URL: {}", shortCode, originalUrl);
        
        UrlDTO urlDTO = urlRepository.findByShortCode(shortCode)
        	    .map(existing -> UrlDTO.builder()
        	        .id(existing.getId())
        	        .originalUrl(originalUrl)
        	        .shortCode(shortCode)
        	        .createdAt(LocalDateTime.now())
        	        .build())
        	    .orElseGet(() -> UrlDTO.builder()
        	        .originalUrl(originalUrl)
        	        .shortCode(shortCode)
        	        .createdAt(LocalDateTime.now())
        	        .build());

        	urlDTO = urlRepository.save(urlDTO);
        	
        log.info("URL shortened successfully. Short code: {}", shortCode);

        // Cache the URL mapping
        cacheService.put(shortCode, originalUrl);
        log.debug("Cached URL mapping for short code: {}", shortCode);

        return urlMapper.toDTO(urlDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public String expandUrl(String shortCode) {
        log.debug("Expanding short code: {}", shortCode);
        
        // Try cache first (Cache-Aside pattern)
        var cachedUrl = cacheService.get(shortCode);
        if (cachedUrl.isPresent()) {
            log.debug("Retrieved URL from cache for short code: {}", shortCode);
            return cachedUrl.get();
        }

        // Fallback to database
        log.debug("Cache miss for short code: {}, querying database", shortCode);
        String originalUrl = urlRepository.findByShortCode(shortCode)
                .map(UrlDTO::getOriginalUrl)
                .orElseThrow(() -> {
                    log.warn("URL not found for short code: {}", shortCode);
                    return new UrlNotFoundException("URL not found for short code: " + shortCode);
                });

        // Update cache for future requests
        cacheService.put(shortCode, originalUrl);
        log.debug("Updated cache for short code: {}", shortCode);

        return originalUrl;
    }
}
