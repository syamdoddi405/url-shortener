package com.url.shortener.entity;

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
 * JPA Entity for Analytics.
 * Represents the analytics table in the database.
 * Separated from DTO to follow Single Responsibility Principle.
 */
@Entity
@Table(name = "analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEntity {

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
				+ ", totalClicks=" + totalClicks + ", createdAt=" + createdAt + ", lastAccessed=" + lastAccessed
				+ ", lastReferrer=" + lastReferrer + ", lastUserAgent=" + lastUserAgent + "]";
	}
}
