package com.url.shortener.service;

import com.url.shortener.dto.AnalyticsDTO;

public interface AnalyticsService {

	AnalyticsDTO getStats(String shortCode);

	void saveAnalytics(String shortCode, String referrer, String userAgent);

}
