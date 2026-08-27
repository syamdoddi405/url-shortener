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
 * Data Transfer Object for URL endpoints.
 * Separated from Entity to follow Single Responsibility Principle.
 * Used for API requests and responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "urls")
public class UrlDTO {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String originalUrl;
    private String shortCode;
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "UrlDTO [id=" + id + ", originalUrl=" + originalUrl + ", shortCode=" + shortCode
                + ", createdAt=" + createdAt + "]";
    }
}
