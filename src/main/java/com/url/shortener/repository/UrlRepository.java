package com.url.shortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.url.shortener.dto.UrlDTO;

/**
 * Repository for UrlDTO persistence.
 * Updated to use the correct entity class (UrlDTO from entity package).
 */
@Repository
public interface UrlRepository extends JpaRepository<UrlDTO, Long> {
    
    /**
     * Find a URL by its short code.
     *
     * @param shortCode the short code
     * @return Optional containing the URL entity if found
     */
    Optional<UrlDTO> findByShortCode(String shortCode);
}
