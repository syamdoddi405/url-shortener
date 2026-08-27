package com.url.shortener.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Analytics endpoints.
 * Separated from Entity to follow Single Responsibility Principle.
 * Used for API responses only.
 */
@Entity
@Table(name = "analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsDTO {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shortCode;

    private String originalUrl;

    private long totalClicks;

    private LocalDateTime createdAt;

    private LocalDateTime lastAccessed;

    private String lastReferrer;

    private String lastUserAgent;

    @Override
    public String toString() {
        return "AnalyticsEntity [id=" + id + ", shortCode=" + shortCode + ", originalUrl=" + originalUrl
                + ", totalClicks=" + totalClicks + ", createdAt=" + createdAt + ", lastAccessed="
                + lastAccessed + ", lastReferrer=" + lastReferrer + ", lastUserAgent=" + lastUserAgent + "]";
    }
}
