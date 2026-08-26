package com.url.shortener.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.url.shortener.dto.AnalyticsDTO;
import com.url.shortener.dto.UrlEntity;
import com.url.shortener.repository.AnalyticsRepository;
import com.url.shortener.repository.UrlRepository;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

	private final AnalyticsRepository analyticsRepository;
	private final UrlRepository urlRepository;

	public AnalyticsServiceImpl(AnalyticsRepository analyticsRepository, UrlRepository urlRepository) {
		this.analyticsRepository = analyticsRepository;
		this.urlRepository = urlRepository;
	}

	@Override
	public AnalyticsDTO getStats(String shortCode) {
		// Find the URL entity
		UrlEntity urlEntity = urlRepository.findByShortCode(shortCode)
				.orElseThrow(() -> new RuntimeException("URL not found"));

		// Find analytics record
		AnalyticsDTO analyticsEntity = analyticsRepository.findByShortCode(shortCode)
				.orElseThrow(() -> new RuntimeException("Analytics not found"));

		// Map to DTO
		return new AnalyticsDTO(shortCode, urlEntity.getOriginalUrl(), analyticsEntity.getTotalClicks(),
				urlEntity.getCreatedAt(), analyticsEntity.getLastAccessed(), analyticsEntity.getLastReferrer(),
				analyticsEntity.getLastUserAgent());
	}

	@Override
	public void saveAnalytics(String shortCode, String referrer, String userAgent) {
		AnalyticsDTO analytics = analyticsRepository.findByShortCode(shortCode).orElseGet(() -> {
			AnalyticsDTO newEntity = new AnalyticsDTO();
			newEntity.setShortCode(shortCode);
			newEntity.setTotalClicks(0);
			return newEntity;
		});

		analytics.setTotalClicks(analytics.getTotalClicks() + 1);
		analytics.setLastAccessed(LocalDateTime.now());
		analytics.setLastReferrer(referrer);
		analytics.setLastUserAgent(userAgent);
		try {
			analyticsRepository.save(analytics);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
