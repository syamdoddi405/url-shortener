package com.url.shortener.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.url.shortener.entity.AnalyticsEntity;

/**
 * Repository for AnalyticsEntity persistence.
 * Updated to use the correct entity class (AnalyticsEntity).
 */
@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsEntity, Long> {
    
    /**
     * Find analytics data by short code.
     *
     * @param shortCode the short code
     * @return Optional containing the analytics entity if found
     */
    Optional<AnalyticsEntity> findByShortCode(String shortCode);
}
