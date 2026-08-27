package com.url.shortener.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.url.shortener.entity.UrlEntity;

/**
 * Repository for UrlEntity persistence.
 * Updated to use the correct entity class (UrlEntity from entity package).
 */
@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    
    /**
     * Find a URL by its short code.
     *
     * @param shortCode the short code
     * @return Optional containing the URL entity if found
     */
    Optional<UrlEntity> findByShortCode(String shortCode);
}
