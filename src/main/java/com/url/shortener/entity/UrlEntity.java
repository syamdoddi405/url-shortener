package com.url.shortener.entity;

import java.time.LocalDateTime;


public class UrlEntity {
	private Long id;
	private String originalUrl;
	private String shortCode;
	private LocalDateTime createdAt;

	public UrlEntity() {
	}

	public UrlEntity(Long id, String originalUrl, String shortCode, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.originalUrl = originalUrl;
		this.shortCode = shortCode;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "UrlEntity [id=" + id + ", originalUrl=" + originalUrl + ", shortCode=" + shortCode + ", createdAt="
				+ createdAt + "]";
	}

}
