package com.url.shortener.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analytics")
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

	// Constructors
	public AnalyticsDTO() {
	}

	public AnalyticsDTO(String shortCode, String originalUrl, long totalClicks, LocalDateTime createdAt,
			LocalDateTime lastAccessed, String lastReferrer, String lastUserAgent) {
		this.shortCode = shortCode;
		this.originalUrl = originalUrl;
		this.totalClicks = totalClicks;
		this.createdAt = createdAt;
		this.lastAccessed = lastAccessed;
		this.lastReferrer = lastReferrer;
		this.lastUserAgent = lastUserAgent;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public long getTotalClicks() {
		return totalClicks;
	}

	public void setTotalClicks(long totalClicks) {
		this.totalClicks = totalClicks;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getLastAccessed() {
		return lastAccessed;
	}

	public void setLastAccessed(LocalDateTime lastAccessed) {
		this.lastAccessed = lastAccessed;
	}

	public String getLastReferrer() {
		return lastReferrer;
	}

	public void setLastReferrer(String lastReferrer) {
		this.lastReferrer = lastReferrer;
	}

	public String getLastUserAgent() {
		return lastUserAgent;
	}

	public void setLastUserAgent(String lastUserAgent) {
		this.lastUserAgent = lastUserAgent;
	}

	@Override
	public String toString() {
		return "AnalyticsDTO [id=" + id + ", shortCode=" + shortCode + ", originalUrl=" + originalUrl + ", totalClicks="
				+ totalClicks + ", createdAt=" + createdAt + ", lastAccessed=" + lastAccessed + ", lastReferrer="
				+ lastReferrer + ", lastUserAgent=" + lastUserAgent + "]";
	}
	
	
}
