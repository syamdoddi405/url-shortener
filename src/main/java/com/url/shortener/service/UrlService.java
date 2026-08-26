package com.url.shortener.service;

import com.url.shortener.entity.UrlEntity;

public interface UrlService {

	UrlEntity shortenUrl(String originalUrl);

	String expandUrl(String shortCode);

}
