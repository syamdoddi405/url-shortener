package com.url.shortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.url.shortener.dto.AnalyticsDTO;

/**
 * Repository for AnalyticsDTO persistence.
 * Updated to use the correct entity class (AnalyticsDTO).
 */
@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsDTO, Long> {
    
    /**
     * Find analytics data by short code.
     *
     * @param shortCode the short code
     * @return Optional containing the analytics entity if found
     */
    Optional<AnalyticsDTO> findByShortCode(String shortCode);
}
