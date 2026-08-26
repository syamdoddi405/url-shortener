package com.url.shortener.service;

import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.url.shortener.dto.UrlEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.repository.UrlRepository;

@Service
public class UrlServiceImpl implements UrlService{
	private final UrlRepository urlRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public UrlServiceImpl(UrlRepository urlRepository, RedisTemplate<String, String> redisTemplate) {
        this.urlRepository = urlRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public com.url.shortener.entity.UrlEntity shortenUrl(String originalUrl) {
        String shortCode = Integer.toHexString(originalUrl.hashCode()).substring(0, 8);
        UrlEntity entity = new UrlEntity();
        entity.setOriginalUrl(originalUrl);
        entity.setShortCode(shortCode);
        entity.setCreatedAt(LocalDateTime.now());
        entity =  urlRepository.save(entity);
        com.url.shortener.entity.UrlEntity urlEntityApi= new com.url.shortener.entity.UrlEntity(entity.getId(), entity.getOriginalUrl(), entity.getShortCode(),entity.getCreatedAt());

        redisTemplate.opsForValue().set(shortCode, originalUrl);
        return urlEntityApi;
    }

    @Override
    public String expandUrl(String shortCode) {
        String cached = redisTemplate.opsForValue().get(shortCode);
        if (cached != null) return cached;

        return urlRepository.findByShortCode(shortCode)
                .map(UrlEntity::getOriginalUrl)
                .orElseThrow(() -> new UrlNotFoundException(
                        "URL not found for short code: " + shortCode
                ));
    }
}
