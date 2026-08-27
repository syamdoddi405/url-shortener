package com.url.shortener.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEntity {

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
